-- ============================================================
-- TRIGGER: Initialize contract data on new contract creation
-- Automatically creates contract_consumption and ror_contract
-- rows when a new contract is inserted
-- ============================================================

CREATE OR REPLACE FUNCTION initialize_contract_data()
RETURNS TRIGGER AS $$
BEGIN
    -- Fill contract_consumption for each service package in the rateplan
    INSERT INTO contract_consumption (contract_id, service_package_id, rateplan_id, starting_date, consumption)
    SELECT 
        NEW.id,
        sp.id,
        NEW.rateplan_id,
        CURRENT_DATE,
        0
    FROM service_package sp
    JOIN rateplan_packages rp ON sp.id = rp.package_id
    WHERE rp.rateplan_id = NEW.rateplan_id;

    -- Fill ror_contract
    INSERT INTO ror_contract (contract_id, rateplan_id, data, voice, sms)
    VALUES (NEW.id, NEW.rateplan_id, 0, 0, 0);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_initialize_contract
AFTER INSERT ON contract
FOR EACH ROW
EXECUTE FUNCTION initialize_contract_data();