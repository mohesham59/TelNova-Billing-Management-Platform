-- ============================================================
-- FUNCTION: rate_cdrs()
-- ============================================================
-- Flow per CDR:
--   a) Verify contract is active
--   b) Determine ROR (voice / sms / data) from rateplan
--   c) Find ALL remaining bundles for that service type
--      in the current billing cycle, ordered by priority:
--        Priority 1 (Free Units)   → consumed FIRST
--        Priority 2 (Bundles)      → consumed SECOND
--   d) Deduct from packages in priority order
--      → charge remainder at ROR
--   e) Cap total cost at available_credit
--   f) Deduct from contract credit
--   g) Track ROR usage in ror_contract
--   h) Mark CDR rated, store cost in rated_cost
--   i) Flag orphan CDRs with no matching contract
-- ============================================================

DROP FUNCTION IF EXISTS rate_cdrs();

CREATE OR REPLACE FUNCTION rate_cdrs()
RETURNS TABLE (
    cdr_id        INTEGER,   
    caller        VARCHAR,   
    service       service_type,
    duration      INTEGER,  
    bundle_used   NUMERIC,  
    extra_usage   NUMERIC,   
    cost          NUMERIC,   
    credit_before NUMERIC,   
    credit_after  NUMERIC   
) AS $func$
DECLARE
    rec             RECORD;
    pkg             RECORD;
    v_remaining     NUMERIC;
    v_cost          NUMERIC;
    v_extra         NUMERIC;
    v_ror           NUMERIC;
    v_bundle_used   NUMERIC;
    v_credit_before NUMERIC;
    v_ror_units     INTEGER;
    v_deduct        NUMERIC;
    v_billing_units NUMERIC;  -- duration converted to billing units (minutes / MB / count)
BEGIN
    -- ==========================================================
    -- PHASE 1: Rate all CDRs that have matching active contracts
    -- ==========================================================
    FOR rec IN
        SELECT
            c.*,                        -- all CDR columns (c.id, c.caller_id, c.service_type, c.duration …)
            ct.id               AS contract_id,      -- contract PK (separate alias to avoid clash with c.id)
            ct.available_credit AS available_credit,  -- starting credit snapshot from the join
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
        --   Data:  bytes/KB → MB (CEIL for partial MB)
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
        -- Step B: Initialize counters
        -- --------------------------------------------------
        v_cost          := 0;
        v_bundle_used   := 0;
        v_extra         := v_billing_units;   -- use converted billing units instead of raw duration
        v_credit_before := rec.available_credit;

        -- --------------------------------------------------
        -- Step C: Loop through ALL packages by priority
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
        -- Step D: Charge ROR on any remaining extra usage
        -- --------------------------------------------------
        v_cost := v_extra * v_ror;

        -- --------------------------------------------------
        -- Step E: Cap cost at available credit
        -- --------------------------------------------------
        IF v_cost > rec.available_credit THEN
            v_cost := rec.available_credit;
        END IF;

        -- --------------------------------------------------
        -- Step F: Deduct cost from contract credit
        -- --------------------------------------------------
        IF v_cost > 0 THEN
            UPDATE contract
            SET    available_credit = available_credit - v_cost
            WHERE  id = rec.contract_id;
        END IF;

        -- --------------------------------------------------
        -- Step G: Track ROR usage via upsert
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
        -- Step H: Mark CDR as rated
        -- --------------------------------------------------
        UPDATE cdr
        SET rated_flag       = TRUE,
            rated_cost = v_cost,
            rating_error     = NULL
        WHERE id = rec.id;

        -- --------------------------------------------------
        -- Step I: Return result row
        -- --------------------------------------------------
        cdr_id        := rec.id;
        caller        := rec.caller_id;
        service       := rec.service_type;
        duration      := rec.duration;
        bundle_used   := v_bundle_used;
        extra_usage   := v_extra;
        cost          := v_cost;
        credit_before := v_credit_before;
        credit_after  := v_credit_before - v_cost;
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
