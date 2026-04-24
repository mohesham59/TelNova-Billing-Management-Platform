ALTER TABLE cdr DROP COLUMN service_id;
ALTER TABLE cdr ADD COLUMN service_type service_type;

CREATE TABLE One_time_fee (
    id   SERIAL PRIMARY KEY,
    name   TEXT,
    price   NUMERIC(5, 2)
);

CREATE TABLE contract_one_time (
    contract_id     INTEGER REFERENCES contract(id),
    fee_id          INTEGER REFERENCES One_time_fee(id)
);


-- Add subtotal (before tax)
ALTER TABLE bill
ADD COLUMN subtotal NUMERIC(12,2) NOT NULL DEFAULT 0;

-- Add total amount (after tax)
ALTER TABLE bill
ADD COLUMN total_amount NUMERIC(12,2) NOT NULL DEFAULT 0;


-- -------------------------------------------------
--  contract_consumption — remove redundant columns
--  kol row bta3 service Id wa7da f elconsumption bta3 
--  elservice hwa elconsumption column no need for other 3.
-- -------------------------------------------------
ALTER TABLE contract_consumption DROP COLUMN IF EXISTS data;
ALTER TABLE contract_consumption DROP COLUMN IF EXISTS minutes;
ALTER TABLE contract_consumption DROP COLUMN IF EXISTS sms;

-- -------------------------------------------------
-- add dates to contract
-- -------------------------------------------------
ALTER TABLE contract ADD COLUMN IF NOT EXISTS activation_date   DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS billing_cycle_day INTEGER NOT NULL DEFAULT 1;

-- -------------------------------------------------
-- recreate contract_one_time with proper structure
-- connect it to the bill table and a flag
-- -------------------------------------------------
DROP TABLE IF EXISTS contract_one_time;

CREATE TABLE contract_one_time (
    id              SERIAL PRIMARY KEY,
    contract_id     INTEGER NOT NULL REFERENCES contract(id),
    fee_id          INTEGER NOT NULL REFERENCES one_time_fee(id),
    applied_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    bill_id         INTEGER REFERENCES bill(id),
    billed_flag     BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE bill ADD COLUMN IF NOT EXISTS period_start DATE;
ALTER TABLE bill ADD COLUMN IF NOT EXISTS period_end   DATE;

ALTER TABLE cdr ADD COLUMN IF NOT EXISTS rating_error  TEXT;
