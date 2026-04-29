DROP FUNCTION IF EXISTS generate_bill(INTEGER, DATE);

CREATE OR REPLACE FUNCTION generate_bill(
    p_contract_id  INTEGER,
    p_billing_date DATE
)
RETURNS INTEGER AS 
$$
DECLARE
    v_bill_id          INTEGER;
    v_period_start     DATE;
    v_period_end       DATE;
    v_contract_msisdn  VARCHAR(20);
    v_rateplan_id      INTEGER;
    v_voice_usage      INTEGER;
    v_data_usage       INTEGER;
    v_sms_usage        INTEGER;
    v_usage_cost       NUMERIC(12,2);
    v_recurring_fee    NUMERIC(12,2);
    v_one_time_fees    NUMERIC(12,2);
    v_subtotal         NUMERIC(12,2);
    v_taxes            NUMERIC(12,2);
    v_total_amount     NUMERIC(12,2);
    v_tax_rate         CONSTANT NUMERIC := 0.10;
BEGIN
    -- Billing period = the month BEFORE billing_date
    v_period_start := DATE_TRUNC('month', p_billing_date - INTERVAL '1 month')::DATE;
    v_period_end   := DATE_TRUNC('month', p_billing_date)::DATE;

    -- Duplicate bill protection 
    IF EXISTS (
        SELECT 1 FROM bill
        WHERE  contract_id  = p_contract_id
          AND  period_start = v_period_start
          AND  period_end   = v_period_end
    ) THEN
        RAISE NOTICE 'Bill already exists for contract % period % to %',
            p_contract_id, v_period_start, v_period_end;
        RETURN NULL;
    END IF;

    -- Fetch contract details 
    SELECT msisdn, rateplan_id
    INTO   v_contract_msisdn, v_rateplan_id
    FROM   contract
    WHERE  id = p_contract_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Contract id % not found', p_contract_id;
    END IF;

    -- Usage cost: sum rated_cost from CDRs in the period 
    SELECT COALESCE(SUM(c.rated_cost), 0)
    INTO   v_usage_cost
    FROM   cdr c
    WHERE  c.caller_id   = v_contract_msisdn
      AND  c.rated_flag  = TRUE
      AND  c.start_time >= v_period_start::TIMESTAMP
      AND  c.start_time <  v_period_end::TIMESTAMP;

    -- Aggregate CDR usage counts for display 
    SELECT
        COALESCE(SUM(CASE WHEN c.service_type = 'voice'
                          THEN c.duration ELSE 0 END), 0)::INTEGER,
        COALESCE(SUM(CASE WHEN c.service_type = 'data'
                          THEN c.duration ELSE 0 END), 0)::INTEGER,
        COALESCE(SUM(CASE WHEN c.service_type = 'sms'
                          THEN c.duration ELSE 0 END), 0)::INTEGER
    INTO v_voice_usage, v_data_usage, v_sms_usage
    FROM cdr c
    WHERE  c.caller_id   = v_contract_msisdn
      AND  c.rated_flag  = TRUE
      AND  c.start_time >= v_period_start::TIMESTAMP
      AND  c.start_time <  v_period_end::TIMESTAMP;

    -- Monthly recurring fee from rateplan 
    SELECT COALESCE(rp.monthly_fee, 0)
    INTO   v_recurring_fee
    FROM   rateplan rp
    WHERE  rp.id = v_rateplan_id;

    -- One-time fees 
    SELECT COALESCE(SUM(otf.price), 0)
    INTO   v_one_time_fees
    FROM   contract_one_time cot
    JOIN   one_time_fee      otf ON cot.fee_id = otf.id
    WHERE  cot.contract_id  = p_contract_id
      AND  cot.billed_flag  = FALSE
      AND  cot.applied_date >= v_period_start
      AND  cot.applied_date <  v_period_end;

    -- Calculate totals 
    v_subtotal     := v_usage_cost + v_recurring_fee + v_one_time_fees;
    v_taxes        := ROUND(v_subtotal * v_tax_rate, 2);
    v_total_amount := ROUND(v_subtotal + v_taxes, 2);

    -- Insert bill row
    INSERT INTO bill (
        contract_id, billing_date,
        period_start, period_end,
        usage_cost,
        recurring_fees, one_time_fees,
        voice_usage, data_usage, sms_usage,
        taxes, subtotal, total_amount
    )
    VALUES (
        p_contract_id, p_billing_date,
        v_period_start, v_period_end,
        v_usage_cost,
        v_recurring_fee, v_one_time_fees,
        v_voice_usage, v_data_usage, v_sms_usage,
        v_taxes, v_subtotal, v_total_amount
    )
    RETURNING id INTO v_bill_id;

    -- Mark one-time fees as billed (only this period) 
    UPDATE contract_one_time
    SET    billed_flag = TRUE,
           bill_id     = v_bill_id
    WHERE  contract_id  = p_contract_id
      AND  billed_flag  = FALSE
      AND  applied_date >= v_period_start
      AND  applied_date <  v_period_end;

    -- Link ror_contract to this bill 
    UPDATE ror_contract
    SET    bill_id = v_bill_id
    WHERE  contract_id = p_contract_id
      AND  bill_id IS NULL;

    -- Insert invoice record (PDF path)
    INSERT INTO invoice (bill_id, month)
    VALUES (v_bill_id, NOW());

    RETURN v_bill_id;
END;
$$
 LANGUAGE plpgsql;
