-- ============================================================
-- TELECOM RATING SYSTEM
-- ============================================================
-- Flow per CDR:
--   a) Determine ROR (voice / sms / data) from rateplan
--   b) Find remaining bundle for that service type
--   c) Deduct from bundle first → charge extra at ROR
--   d) Cap total cost at available_credit
--   e) Deduct from contract credit
--   f) Track ROR usage in ror_contract
--   g) Mark CDR rated, store cost in external_charges
-- ============================================================

CREATE OR REPLACE FUNCTION rate_cdrs()
RETURNS VOID AS $$
DECLARE
    rec         RECORD;
    v_remaining NUMERIC;
    v_cost      NUMERIC;
    v_extra     NUMERIC;
    v_ror       NUMERIC;
    v_pkg_id    INTEGER;
    v_cc_id     INTEGER;
BEGIN

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
        JOIN   contract ct ON cdr.caller_id    = ct.msisdn
        JOIN   rateplan  rp ON ct.rateplan_id = rp.id
        WHERE  cdr.rated_flag = FALSE
        ORDER  BY cdr.start_time   -- process chronologically
    LOOP
        -- ── 1. Pick ROR for this service type ──────────────────
        IF rec.service_type = 'voice' THEN
            v_ror := rec.ror_voice;
        ELSIF rec.service_type = 'sms' THEN
            v_ror := rec.ror_sms;
        ELSE
            v_ror := rec.ror_data;
        END IF;

        v_cost := 0;

        -- ── 2. Find matching bundle (lowest priority first) ─────
        -- Matches service_type and belongs to this contract's rateplan
        SELECT
            cont_cons.id,
            sp.id,
            (sp.amount - cont_cons.consumption)   -- remaining = total quota - consumed
        INTO
            v_cc_id,
            v_pkg_id,
            v_remaining
        FROM   contract_consumption cont_cons
        JOIN   service_package       sp ON cont_cons.service_package_id = sp.id
        WHERE  cont_cons.contract_id = rec.contract_id
          AND  sp.type        = rec.service_type
        ORDER  BY sp.priority ASC   -- consume lowest-priority bundle first
        LIMIT  1;

        -- NULL means no bundle row found → treat as exhausted
        v_remaining := COALESCE(v_remaining, 0);

        -- ── 3. Bundle-first billing logic ──────────────────────
        IF v_remaining > 0 THEN
            IF rec.duration <= v_remaining THEN
                -- Fully covered by bundle — zero extra charge
                UPDATE contract_consumption
                SET    consumption = consumption + rec.duration
                WHERE  id = v_cc_id;

                v_cost := 0;

            ELSE
                -- Partially covered: exhaust bundle, charge remainder at ROR
                v_extra := rec.duration - v_remaining;

                UPDATE contract_consumption
                SET    consumption = consumption + v_remaining
                WHERE  id = v_cc_id;

                v_cost := v_extra * v_ror;
            END IF;

        ELSE
            -- No bundle remaining → full ROR charge
            v_cost := rec.duration * v_ror;
        END IF;

        -- ── 4. Cap cost at available_credit ────────────────────
        IF v_cost > rec.available_credit THEN
            v_cost := rec.available_credit;
        END IF;

        -- ── 5. Deduct from contract credit ─────────────────────
        IF v_cost > 0 THEN
            UPDATE contract
            SET    available_credit = available_credit - v_cost
            WHERE  id = rec.contract_id;
        END IF;

        -- ── 6. Track ROR (extra-usage) in ror_contract ─────────
        IF v_cost > 0 AND v_ror > 0 THEN
            IF rec.service_type = 'voice' THEN
                UPDATE ror_contract
                SET    voice = COALESCE(voice, 0) + CEIL(v_cost / v_ror)::INTEGER
                WHERE  contract_id = rec.contract_id
                  AND  rateplan_id = rec.rateplan_id;

            ELSIF rec.service_type = 'sms' THEN
                UPDATE ror_contract
                SET    sms = COALESCE(sms, 0) + CEIL(v_cost / v_ror)::INTEGER
                WHERE  contract_id = rec.contract_id
                  AND  rateplan_id = rec.rateplan_id;

            ELSE
                UPDATE ror_contract
                SET    data = COALESCE(data, 0) + CEIL(v_cost / v_ror)::INTEGER
                WHERE  contract_id = rec.contract_id
                  AND  rateplan_id = rec.rateplan_id;
            END IF;
        END IF;

        -- ── 7. Mark CDR as rated; store computed charge ─────────
        UPDATE cdr
        SET    rated_flag       = TRUE,
               external_charges = v_cost
        WHERE  id = rec.id;

    END LOOP;

END;
$$ LANGUAGE plpgsql;