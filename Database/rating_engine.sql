-- ============================================================
-- FUNCTION: rate_cdrs()
-- ============================================================
-- Flow per CDR:
--   a) Verify contract is active
--   b) Determine ROR (voice / sms / data) from rateplan
--   c) Convert raw duration to billing units:
--      - Voice: seconds → CEIL to minutes (e.g. 140s = 3 min)
--      - SMS:   message count (no conversion)
--      - Data:  CEIL to MB
--   d) Calculate effective usage with on-net/cross-net:
--      - Voice on-net  → 1 unit per minute
--      - Voice cross-net → 5 units per minute
--      - SMS/Data → same as billing units
--   e) Find ALL remaining bundles for that service type
--      in the current billing cycle, ordered by priority:
--        Priority 1 (Free Units)   → consumed FIRST
--        Priority 2 (Bundles)      → consumed SECOND
--   f) Deduct from packages in priority order
--      → charge remainder at ROR
--   g) Cap total cost at available_credit
--   h) Deduct from contract credit
--   i) Track ROR usage in ror_contract
--   j) Mark CDR rated, store cost in rated_cost
--   k) Flag orphan CDRs with no matching contract
-- ============================================================

DROP FUNCTION IF EXISTS rate_cdrs();

CREATE OR REPLACE FUNCTION rate_cdrs()
RETURNS TABLE (
    cdr_id          INTEGER,
    caller          VARCHAR,
    service         service_type,
    duration        INTEGER,
    effective_usage NUMERIC,
    bundle_used     NUMERIC,
    extra_usage     NUMERIC,
    cost            NUMERIC,
    credit_before   NUMERIC,
    credit_after    NUMERIC,
    call_type       TEXT
) AS $func$
DECLARE
    rec               RECORD;
    pkg               RECORD;
    v_remaining       NUMERIC;
    v_cost            NUMERIC;
    v_extra           NUMERIC;
    v_ror             NUMERIC;
    v_bundle_used     NUMERIC;
    v_credit_before   NUMERIC;
    v_ror_units       INTEGER;
    v_deduct          NUMERIC;
    v_billing_units   NUMERIC;   -- duration converted to billing units (minutes / MB / count)
    v_effective_usage NUMERIC;   -- billing units × on-net/cross-net multiplier
    v_call_type       TEXT;
    v_company_prefix  VARCHAR := '013';
    v_unit_multiplier NUMERIC;
BEGIN
    -- ==========================================================
    -- PHASE 1: Rate all CDRs that have matching active contracts
    -- ==========================================================
    FOR rec IN
        SELECT
            cdr.*,
            ct.id               AS contract_id,
            ct.available_credit,
            ct.rateplan_id,
            rp.ror_voice,
            rp.ror_sms,
            rp.ror_data
        FROM   cdr
        JOIN   contract ct ON cdr.caller_id = ct.msisdn
                           AND ct.status = 'active'
        JOIN   rateplan rp ON ct.rateplan_id = rp.id
        WHERE  cdr.rated_flag = FALSE
          AND  cdr.rating_error IS NULL
        ORDER  BY cdr.start_time
    LOOP
        -- --------------------------------------------------
        -- Step A: Determine the ROR for this service type
        -- --------------------------------------------------
        IF rec.service_type = 'voice' THEN
            v_ror := COALESCE(rec.ror_voice, 0);
        ELSIF rec.service_type = 'sms' THEN
            v_ror := COALESCE(rec.ror_sms, 0);
        ELSE
            v_ror := COALESCE(rec.ror_data, 0);
        END IF;

        -- --------------------------------------------------
        -- Step A2: Convert raw duration to billing units
        --   Voice: seconds → minutes (CEIL, e.g. 140s = 3 min)
        --   SMS:   message count (no conversion needed)
        --   Data:  CEIL to MB (for partial MB)
        --   Bundles are defined in these same units
        -- --------------------------------------------------
        IF rec.service_type = 'voice' THEN
            v_billing_units := CEIL(rec.duration / 60.0);
        ELSIF rec.service_type = 'sms' THEN
            v_billing_units := rec.duration;
        ELSE
            v_billing_units := CEIL(rec.duration);
        END IF;

        -- --------------------------------------------------
        -- Step B: Calculate effective usage (on-net / cross-net)
        --   Voice on-net (same company prefix): 1 unit/min
        --   Voice cross-net (other operator):   5 units/min
        --   SMS / Data: no multiplier
        -- --------------------------------------------------
        IF rec.service_type = 'voice' THEN
            IF LEFT(rec.receiver_id, LENGTH(v_company_prefix)) = v_company_prefix THEN
                v_unit_multiplier := 1;
                v_call_type       := 'on-net';
            ELSE
                v_unit_multiplier := 5;
                v_call_type       := 'cross-net';
            END IF;
            v_effective_usage := v_billing_units * v_unit_multiplier;
        ELSIF rec.service_type = 'sms' THEN
            v_effective_usage := v_billing_units;
            v_call_type       := 'sms';
        ELSE
            v_effective_usage := v_billing_units;
            v_call_type       := 'data';
        END IF;

        -- --------------------------------------------------
        -- Step C: Initialize counters
        -- --------------------------------------------------
        v_cost          := 0;
        v_bundle_used   := 0;
        v_extra         := v_effective_usage;
        v_credit_before := rec.available_credit;

        -- --------------------------------------------------
        -- Step D: Loop through ALL packages by priority
        -- --------------------------------------------------
        FOR pkg IN
            SELECT
                cont_cons.id    AS cc_id,
                sp.id           AS pkg_id,
                sp.type         AS pkg_type,
                sp.priority     AS pkg_priority,
                sp.name         AS pkg_name,
                sp.amount       AS pkg_amount,
                cont_cons.consumption AS current_consumption,
                (sp.amount - cont_cons.consumption) AS remaining
            FROM contract_consumption cont_cons
            JOIN service_package sp ON cont_cons.service_package_id = sp.id
            WHERE cont_cons.contract_id = rec.contract_id
              AND sp.type = rec.service_type
              AND cont_cons.consumption < sp.amount
              AND cont_cons.starting_date = date_trunc('month', rec.start_time)::DATE
            ORDER BY sp.priority ASC, sp.id ASC
        LOOP
            -- Exit early if all usage is covered
            IF v_extra <= 0 THEN
                EXIT;
            END IF;

            v_remaining := COALESCE(pkg.remaining, 0);

            IF v_remaining <= 0 THEN
                CONTINUE;
            END IF;

            -- Calculate how much to deduct from this package
            IF v_extra <= v_remaining THEN
                v_deduct := v_extra;
            ELSE
                v_deduct := v_remaining;
            END IF;

            -- Update consumption (single column tracks total)
            UPDATE contract_consumption
            SET consumption = consumption + v_deduct
            WHERE id = pkg.cc_id;

            v_bundle_used := v_bundle_used + v_deduct;
            v_extra       := v_extra - v_deduct;

        END LOOP;

        -- --------------------------------------------------
        -- Step E: Charge ROR on any remaining extra usage
        -- --------------------------------------------------
        v_cost := v_extra * v_ror;

        -- --------------------------------------------------
        -- Step F: Cap cost at available credit
        -- --------------------------------------------------
        IF v_cost > v_credit_before THEN
            v_cost := v_credit_before;
        END IF;

        -- --------------------------------------------------
        -- Step G: Deduct cost from contract credit
        -- --------------------------------------------------
        IF v_cost > 0 THEN
            UPDATE contract
            SET available_credit = available_credit - v_cost
            WHERE id = rec.contract_id;
        END IF;

        -- --------------------------------------------------
        -- Step H: Track ROR usage via upsert
        -- --------------------------------------------------
        IF v_cost > 0 AND v_ror > 0 THEN
            v_ror_units := CEIL(v_cost / v_ror)::INTEGER;

            INSERT INTO ror_contract (contract_id, rateplan_id, data, voice, sms)
            VALUES (
                rec.contract_id,
                rec.rateplan_id,
                CASE WHEN rec.service_type = 'data'  THEN v_ror_units ELSE 0 END,
                CASE WHEN rec.service_type = 'voice' THEN v_ror_units ELSE 0 END,
                CASE WHEN rec.service_type = 'sms'   THEN v_ror_units ELSE 0 END
            )
            ON CONFLICT (contract_id, rateplan_id) DO UPDATE
            SET
                voice = ror_contract.voice + EXCLUDED.voice,
                data  = ror_contract.data  + EXCLUDED.data,
                sms   = ror_contract.sms   + EXCLUDED.sms;
        END IF;

        -- --------------------------------------------------
        -- Step I: Mark CDR as rated
        -- --------------------------------------------------
        UPDATE cdr
        SET rated_flag   = TRUE,
            rated_cost   = v_cost,
            rating_error = NULL
        WHERE id = rec.id;

        -- --------------------------------------------------
        -- Step J: Return result row
        -- --------------------------------------------------
        cdr_id          := rec.id;
        caller          := rec.caller_id;
        service         := rec.service_type;
        duration        := rec.duration;
        effective_usage := v_effective_usage;
        bundle_used     := v_bundle_used;
        extra_usage     := v_extra;
        cost            := v_cost;
        credit_before   := v_credit_before;
        credit_after    := v_credit_before - v_cost;
        call_type       := v_call_type;
        RETURN NEXT;

    END LOOP;

    -- ==========================================================
    -- PHASE 2: Flag orphan CDRs (no matching active contract)
    -- ==========================================================
    UPDATE cdr
    SET rating_error = CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM contract WHERE msisdn = cdr.caller_id
        ) THEN 'No matching contract for caller_id'
        WHEN EXISTS (
            SELECT 1 FROM contract
            WHERE msisdn = cdr.caller_id
              AND status != 'active'
        ) THEN 'Contract exists but is not active (status: ' || (
            SELECT status FROM contract WHERE msisdn = cdr.caller_id LIMIT 1
        ) || ')'
        ELSE 'Unknown rating error'
    END
    WHERE rated_flag = FALSE
      AND rating_error IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM contract
          WHERE msisdn = cdr.caller_id
            AND status = 'active'
      );

END;
$func$ LANGUAGE plpgsql;
