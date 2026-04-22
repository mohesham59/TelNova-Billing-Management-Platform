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


