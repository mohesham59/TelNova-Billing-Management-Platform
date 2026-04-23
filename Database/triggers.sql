-- ============================================================
-- TRIGGER: Initialize contract data on new contract creation
-- ============================================================
-- Creates:
--   1. contract_consumption rows for each package in the rateplan
--      - consumption = 0 (total usage tracker)
--   2. ror_contract row for tracking current month ROR usage
--
-- Priority convention:
--   Priority 1 = Free Units  (consumed first)
--   Priority 2 = Bundles     (consumed second)
--   Priority N = ...         (consumed in order)
-- ============================================================

DROP TRIGGER IF EXISTS trg_initialize_contract ON contract;
DROP FUNCTION IF EXISTS initialize_contract_data();

CREATE OR REPLACE FUNCTION initialize_contract_data()
RETURNS TRIGGER AS 
$$
DECLARE
    v_cycle_start DATE;
BEGIN
    v_cycle_start := date_trunc('month', CURRENT_DATE)::DATE;

    -- --------------------------------------------------
    -- Create contract_consumption rows for each package
    -- in the rateplan for the current billing cycle
    --
    -- consumption = total units consumed from this package
    -- type is known from service_package.type
    --
    -- Packages are created for ALL priorities:
    --   Priority 1 (Free Units) and Priority 2+ (Bundles)
    -- --------------------------------------------------
    INSERT INTO contract_consumption (
        contract_id,
        service_package_id,
        rateplan_id,
        starting_date,
        consumption
    )
    SELECT
        NEW.id,
        sp.id,
        NEW.rateplan_id,
        v_cycle_start,
        0
    FROM service_package sp
    JOIN rateplan_packages rp ON sp.id = rp.package_id
    WHERE rp.rateplan_id = NEW.rateplan_id
      AND NOT EXISTS (
          SELECT 1
          FROM contract_consumption cc
          WHERE cc.contract_id        = NEW.id
            AND cc.service_package_id = sp.id
            AND cc.rateplan_id        = NEW.rateplan_id
            AND cc.starting_date      = v_cycle_start
      );

    -- --------------------------------------------------
    -- Initialize ror_contract via upsert
    -- Tracks current month out-of-bundle usage
    -- Zeroed when billing cycle resets
    -- --------------------------------------------------
    INSERT INTO ror_contract (contract_id, rateplan_id, data, voice, sms)
    VALUES (NEW.id, NEW.rateplan_id, 0, 0, 0)
    ON CONFLICT (contract_id, rateplan_id) DO NOTHING;

    RETURN NEW;
END;
$$
 LANGUAGE plpgsql;

CREATE TRIGGER trg_initialize_contract
AFTER INSERT ON contract
FOR EACH ROW
EXECUTE FUNCTION initialize_contract_data();