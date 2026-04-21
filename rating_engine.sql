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

-- Drop if exists
DROP FUNCTION IF EXISTS rate_cdrs();

-- Create function
CREATE OR REPLACE FUNCTION rate_cdrs()
RETURNS TABLE (
    cdr_id          INTEGER,
    caller          VARCHAR,
    service         service_type,
    duration        INTEGER,
    bundle_used     NUMERIC,
    extra_usage     NUMERIC,
    cost            NUMERIC,
    credit_before   NUMERIC,
    credit_after    NUMERIC
) AS $func$
DECLARE
    rec             RECORD;
    v_remaining     NUMERIC;
    v_cost          NUMERIC;
    v_extra         NUMERIC;
    v_ror           NUMERIC;
    v_pkg_id        INTEGER;
    v_cc_id         INTEGER;
    v_bundle_used   NUMERIC;
    v_credit_before NUMERIC;
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
        JOIN   contract ct ON cdr.caller_id   = ct.msisdn
        JOIN   rateplan  rp ON ct.rateplan_id = rp.id
        WHERE  cdr.rated_flag = FALSE
        ORDER  BY cdr.start_time
    LOOP
        IF rec.service_type = 'voice' THEN
            v_ror := rec.ror_voice;
        ELSIF rec.service_type = 'sms' THEN
            v_ror := rec.ror_sms;
        ELSE
            v_ror := rec.ror_data;
        END IF;

        v_cost          := 0;
        v_bundle_used   := 0;
        v_extra         := 0;
        v_credit_before := rec.available_credit;

        SELECT cont_cons.id, sp.id, (sp.amount - cont_cons.consumption)
        INTO v_cc_id, v_pkg_id, v_remaining
        FROM contract_consumption cont_cons
        JOIN service_package sp ON cont_cons.service_package_id = sp.id
        WHERE cont_cons.contract_id = rec.contract_id
          AND sp.type = rec.service_type
        ORDER BY sp.priority ASC
        LIMIT 1;

        v_remaining := COALESCE(v_remaining, 0);

        IF v_remaining > 0 THEN
            IF rec.duration <= v_remaining THEN
                UPDATE contract_consumption
                SET consumption = consumption + rec.duration
                WHERE id = v_cc_id;
                v_bundle_used := rec.duration;
                v_cost        := 0;
            ELSE
                v_extra       := rec.duration - v_remaining;
                v_bundle_used := v_remaining;
                UPDATE contract_consumption
                SET consumption = consumption + v_remaining
                WHERE id = v_cc_id;
                v_cost := v_extra * v_ror;
            END IF;
        ELSE
            v_extra := rec.duration;
            v_cost  := rec.duration * v_ror;
        END IF;

        IF v_cost > rec.available_credit THEN
            v_cost := rec.available_credit;
        END IF;

        IF v_cost > 0 THEN
            UPDATE contract
            SET available_credit = available_credit - v_cost
            WHERE id = rec.contract_id;
        END IF;

        IF v_cost > 0 AND v_ror > 0 THEN
            IF rec.service_type = 'voice' THEN
                UPDATE ror_contract
                SET voice = COALESCE(voice, 0) + CEIL(v_cost / v_ror)::INTEGER
                WHERE contract_id = rec.contract_id AND rateplan_id = rec.rateplan_id;
            ELSIF rec.service_type = 'sms' THEN
                UPDATE ror_contract
                SET sms = COALESCE(sms, 0) + CEIL(v_cost / v_ror)::INTEGER
                WHERE contract_id = rec.contract_id AND rateplan_id = rec.rateplan_id;
            ELSE
                UPDATE ror_contract
                SET data = COALESCE(data, 0) + CEIL(v_cost / v_ror)::INTEGER
                WHERE contract_id = rec.contract_id AND rateplan_id = rec.rateplan_id;
            END IF;
        END IF;

        UPDATE cdr
        SET rated_flag = TRUE, external_charges = v_cost
        WHERE id = rec.id;

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
END;
$func$ LANGUAGE plpgsql;
