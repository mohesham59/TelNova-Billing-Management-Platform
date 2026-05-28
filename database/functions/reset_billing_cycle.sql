-- ============================================================
--   Reset billing cycle
-- ============================================================
-- Called at the start of each new billing period.
-- Should be called AFTER bill generation captures the data.
--
-- For every active contract:
--   1. Creates fresh contract_consumption rows with zeroed
--      consumption for each package in the contract's rateplan
--   2. Zeroes ror_contract counters for the new cycle
--
-- Flow:
--   1. CDRs rated     → ror_contract accumulates usage
--   2. Bill generated → bill captures ror_contract values
--   3. Cycle reset    → ror_contract zeroed for next month
--
-- Safe to call multiple times — uses NOT EXISTS / ON CONFLICT
--
-- Package priority is preserved from service_package:
--   Priority 1 = Free Units  → consumed first during rating
--   Priority 2 = Bundles     → consumed second during rating
-- ============================================================

DROP FUNCTION IF EXISTS reset_billing_cycle();

CREATE OR REPLACE FUNCTION reset_billing_cycle()
RETURNS TABLE (
    contract_id      INTEGER,
    package_id       INTEGER,
    package_name     VARCHAR,
    package_type     service_type,
    package_priority INTEGER,
    rateplan_id      INTEGER,
    cycle_start      DATE
) AS 
$$
DECLARE
    v_cycle_start   DATE;
    v_contracts     INTEGER := 0;
    v_packages      INTEGER := 0;
BEGIN
    v_cycle_start := date_trunc('month', CURRENT_DATE)::DATE;

    -- --------------------------------------------------
    -- Step 1: Create fresh contract_consumption rows
    -- --------------------------------------------------
    RETURN QUERY
    INSERT INTO contract_consumption (
        contract_id,
        service_package_id,
        rateplan_id,
        starting_date,
        consumption
    )
    SELECT
        c.id,
        sp.id,
        c.rateplan_id,
        v_cycle_start,
        0
    FROM contract c
    JOIN rateplan_packages rp ON c.rateplan_id = rp.rateplan_id
    JOIN service_package sp   ON rp.package_id = sp.id
    WHERE c.status = 'active'
      AND NOT EXISTS (
          SELECT 1
          FROM contract_consumption cc
          WHERE cc.contract_id        = c.id
            AND cc.service_package_id = sp.id
            AND cc.rateplan_id        = c.rateplan_id
            AND cc.starting_date      = v_cycle_start
      )
    RETURNING
        contract_consumption.contract_id,
        contract_consumption.service_package_id,
        (SELECT sp2.name FROM service_package sp2
         WHERE sp2.id = contract_consumption.service_package_id),
        (SELECT sp2.type FROM service_package sp2
         WHERE sp2.id = contract_consumption.service_package_id),
        (SELECT sp2.priority FROM service_package sp2
         WHERE sp2.id = contract_consumption.service_package_id),
        contract_consumption.rateplan_id,
        contract_consumption.starting_date;

    -- --------------------------------------------------
    -- Step 2: Zero out ror_contract for active contracts
    -- --------------------------------------------------

	UPDATE ror_contract rc
	SET    data  = 0,
	       voice = 0,
	       sms   = 0
	WHERE rc.contract_id IN (
	    SELECT id FROM contract WHERE status = 'active'
	);
	-- Restore available_credit to credit_limit for all active contracts
	UPDATE contract
	SET    available_credit = credit_limit
	WHERE  status = 'active';

    -- --------------------------------------------------
    -- Log summary
    -- --------------------------------------------------
    SELECT COUNT(DISTINCT cc.contract_id),
           COUNT(*)
    INTO v_contracts, v_packages
    FROM contract_consumption cc
    WHERE cc.starting_date = v_cycle_start;

    RAISE NOTICE 'Billing cycle reset complete for %', v_cycle_start;
    RAISE NOTICE 'Contracts processed: %', v_contracts;
    RAISE NOTICE 'Package rows created: %', v_packages;

END;
$$
LANGUAGE plpgsql;
