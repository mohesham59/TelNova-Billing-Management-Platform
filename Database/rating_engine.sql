-- ============================================================
-- TELECOM RATING SYSTEM (FINAL)
-- ============================================================
-- Flow per CDR:
--   a) Verify contract is active
--   b) Determine ROR (voice / sms / data) from rateplan
--   c) Calculate effective usage:
<<<<<<< HEAD
--      - Voice on-net -> 1 unit/min
--      - Voice off-net -> 5 units/min
--      - SMS  -> 1 unit per message
--      - Data -> 1 unit per MB (parser converts KB→MB)
=======
--      - Voice on-net (receiver starts with 013) → 1 unit/min
--      - Voice cross-net (receiver other network) → 5 units/min
--      - SMS → 1 unit per message
--      - Data → 1 unit per MB (parser converts KB→MB)
>>>>>>> feature/rating
--   d) Find ALL remaining bundles for that service type
--      in the current billing cycle, ordered by priority:
--        Priority 1 (Free Units)   → consumed FIRST
--        Priority 2 (Bundles)      → consumed SECOND
--   e) Deduct from packages in priority order
--      and charge remainder at ROR
--   f) Cap total cost at available_credit
--   g) Deduct from contract credit
<<<<<<< HEAD
--   h) Track ROR usage in ror_contract (upsert)
=======
--   h) Track ROR usage in ror_contract
--      ROR tracks units paid for (capped), not raw usage
>>>>>>> feature/rating
--   i) Mark CDR rated, store cost in rated_cost
--   j) Flag orphan CDRs with no matching contract
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
    v_effective_usage NUMERIC;
    v_call_type       TEXT;
    v_company_prefix  VARCHAR := '013';
    v_unit_multiplier NUMERIC;
BEGIN
    -- ==========================================================
<<<<<<< HEAD
    --  Rate all CDRs that have matching active contracts
=======
    --  Rate all CDRs with matching active contracts
>>>>>>> feature/rating
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
        -- A: Determine ROR for this service type
        -- --------------------------------------------------
        IF rec.service_type = 'voice' THEN
            v_ror := COALESCE(rec.ror_voice, 0);
        ELSIF rec.service_type = 'sms' THEN
            v_ror := COALESCE(rec.ror_sms, 0);
        ELSE
            v_ror := COALESCE(rec.ror_data, 0);
        END IF;

        -- --------------------------------------------------
<<<<<<< HEAD
        -- B: Calculate effective usage based on
        --    on-net vs off-net for voice calls
        -- --------------------------------------------------
        IF rec.service_type = 'voice' THEN
            IF LEFT(rec.receiver_id, LENGTH(v_company_prefix)) = v_company_prefix THEN
                v_unit_multiplier := 1;
                v_call_type       := 'on-net';
            ELSE
                v_unit_multiplier := 5;
                v_call_type       := 'off-net';
            END IF;
            v_effective_usage := rec.duration * v_unit_multiplier;
        ELSIF rec.service_type = 'sms' THEN
            v_effective_usage := rec.duration;
            v_call_type       := 'sms';
        ELSE
            v_effective_usage := rec.duration;
            v_call_type       := 'data';
        END IF;

        -- --------------------------------------------------
        -- C: Initialize counters
        -- --------------------------------------------------
        v_cost          := 0;
        v_bundle_used   := 0;
        v_extra         := v_effective_usage;
        v_credit_before := rec.available_credit;

        -- --------------------------------------------------
        -- D: Loop through ALL packages by priority
=======
        -- B: Calculate effective usage
        -- --------------------------------------------------
        IF rec.service_type = 'voice' THEN
            IF LEFT(rec.receiver_id, LENGTH(v_company_prefix)) = v_company_prefix THEN
                v_unit_multiplier := 1;
                v_call_type       := 'on-net';
            ELSE
                v_unit_multiplier := 5;
                v_call_type       := 'cross-net';
            END IF;
            v_effective_usage := rec.duration * v_unit_multiplier;
        ELSIF rec.service_type = 'sms' THEN
            v_effective_usage := rec.duration;
            v_call_type       := 'sms';
        ELSE
            v_effective_usage := rec.duration;
            v_call_type       := 'data';
        END IF;

        -- --------------------------------------------------
        -- C: Re-read current credit and initialize counters
        -- --------------------------------------------------
        SELECT available_credit INTO v_credit_before
        FROM contract WHERE id = rec.contract_id;

        v_cost          := 0;
        v_bundle_used   := 0;
        v_extra         := v_effective_usage;

        -- --------------------------------------------------
        -- D: Loop through packages by priority
>>>>>>> feature/rating
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
            IF v_extra <= 0 THEN
                EXIT;
            END IF;

            v_remaining := COALESCE(pkg.remaining, 0);

            IF v_remaining <= 0 THEN
                CONTINUE;
            END IF;

            IF v_extra <= v_remaining THEN
                v_deduct := v_extra;
            ELSE
                v_deduct := v_remaining;
            END IF;

            UPDATE contract_consumption
            SET consumption = consumption + v_deduct
            WHERE id = pkg.cc_id;

            v_bundle_used := v_bundle_used + v_deduct;
            v_extra       := v_extra - v_deduct;
        END LOOP;

        -- --------------------------------------------------
<<<<<<< HEAD
        -- E: Charge ROR on any remaining extra usage
=======
        -- E: Charge ROR on remaining extra usage
>>>>>>> feature/rating
        -- --------------------------------------------------
        v_cost := v_extra * v_ror;

        -- --------------------------------------------------
        -- F: Cap cost at available credit
        -- --------------------------------------------------
        IF v_cost > v_credit_before THEN
            v_cost := v_credit_before;
        END IF;

        -- --------------------------------------------------
        -- G: Deduct cost from contract credit
        -- --------------------------------------------------
        IF v_cost > 0 THEN
            UPDATE contract
            SET available_credit = available_credit - v_cost
            WHERE id = rec.contract_id;
        END IF;

        -- --------------------------------------------------
        -- H: Track ROR usage
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
        -- I: Mark CDR as rated
        -- --------------------------------------------------
        UPDATE cdr
        SET rated_flag   = TRUE,
            rated_cost   = v_cost,
            rating_error = NULL
        WHERE id = rec.id;

        -- --------------------------------------------------
        -- Return result row
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

<<<<<<< HEAD
    -- ----------------------------------------------------------
    -- J: Flag orphan CDRs
    -- ----------------------------------------------------------
=======
    -- ---------------------------------------------------
    -- J: Flag orphan CDRs
    -- --------------------------------------------------
>>>>>>> feature/rating
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