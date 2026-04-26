DROP FUNCTION IF EXISTS bill_all_active_contracts(DATE);

CREATE OR REPLACE FUNCTION bill_all_active_contracts(
    p_billing_date DATE
)
RETURNS TABLE (
    out_contract_id  INTEGER,
    out_msisdn       VARCHAR,
    out_bill_id      INTEGER,
    out_total_amount NUMERIC
) AS $wrapper$
DECLARE
    rec       RECORD;
    v_bill_id INTEGER;
BEGIN
    FOR rec IN
        SELECT c.id, c.msisdn
        FROM   contract c
        WHERE  c.status = 'active'
        ORDER  BY c.id
    LOOP
        v_bill_id := generate_bill(rec.id, p_billing_date);

        -- Skip if duplicate bill detected (returns NULL)
        IF v_bill_id IS NULL THEN
            CONTINUE;
        END IF;

        out_contract_id := rec.id;
        out_msisdn      := rec.msisdn;
        out_bill_id     := v_bill_id;

        SELECT b.total_amount
        INTO   out_total_amount
        FROM   bill b
        WHERE  b.id = v_bill_id;

        RETURN NEXT;
    END LOOP;
END;
$wrapper$ LANGUAGE plpgsql;
