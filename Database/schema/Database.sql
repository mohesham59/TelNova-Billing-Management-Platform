-- ============================================================
-- TELECOM BILLING SCHEMA
-- ============================================================

-- ------------------------------------------------------------
-- FILE (raw CDR file ingestion tracker)
-- ------------------------------------------------------------
CREATE TABLE file (
    id          SERIAL PRIMARY KEY,
    parsed_flag BOOLEAN NOT NULL DEFAULT FALSE
);
-- 
-- -- ------------------------------------------------------------
--- CUSTOMER
-- ------------------------------------------------------------
CREATE TABLE users (
    id        VARCHAR(14) PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    address   TEXT,
    birthdate DATE
);

-- -- ------------------------------------------------------------
-- -- RATEPLAN
-- -- ------------------------------------------------------------
CREATE TABLE rateplan (
    id        SERIAL PRIMARY KEY,
    plan_name      VARCHAR(255) NOT NULL,
    ror_data  NUMERIC(5,2),     -- e.g. 0.05
    ror_voice NUMERIC(5,2),     -- e.g. 0.05
    ror_sms   NUMERIC(5,2),     -- e.g. 0.05
    monthly_fee     NUMERIC(10,2)     -- base price of the plan
);

-- ------------------------------------------------------------
-- SERVICE_PACKAGE
-- bundled quotas sold as part of a contract
-- ------------------------------------------------------------
CREATE TYPE service_type AS ENUM ('voice', 'data', 'sms');
CREATE TABLE service_package (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    type     service_type  NOT NULL,  -- 'voice', 'data', 'sms'.
    amount   NUMERIC(12,4) NOT NULL, -- quota amount (minutes / MB / count)
    priority INTEGER NOT NULL DEFAULT 1 -- for consumption order (lower = consumed first)
);

-- ------------------------------------------------------------
-- RATEPLAN_PACKAGES
-- packages for each rate plan
-- ------------------------------------------------------------
CREATE TABLE rateplan_packages (
    rateplan_id INTEGER REFERENCES rateplan(id),
    package_id  INTEGER REFERENCES service_package(id),
    PRIMARY KEY (rateplan_id, package_id)
);

-- ------------------------------------------------------------
-- CONTRACT
-- ties a customer to a rateplan + an MSISDN (phone number)
-- ------------------------------------------------------------
CREATE TYPE contract_status AS ENUM ('active', 'suspended', 'de-active', 'on-hold');
CREATE TABLE contract (
    id              SERIAL PRIMARY KEY,
    user_id     VARCHAR(14) NOT NULL REFERENCES users(id),
    rateplan_id     INTEGER NOT NULL REFERENCES rateplan(id),
    msisdn          VARCHAR(20) NOT NULL UNIQUE,
    status          contract_status NOT NULL DEFAULT 'active',
    credit_limit    NUMERIC(12,2) NOT NULL DEFAULT 0,
    available_credit NUMERIC(12,2) NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- CONTRACT_CONSUMPTION
-- tracks how much of each service_package has been consumed
-- in a billing period for a contract
-- ------------------------------------------------------------
CREATE TABLE contract_consumption (
    id                  SERIAL PRIMARY KEY,
    contract_id         INTEGER NOT NULL REFERENCES contract(id),
    service_package_id  INTEGER NOT NULL REFERENCES service_package(id),
    rateplan_id         INTEGER NOT NULL REFERENCES rateplan(id),
    starting_date       DATE NOT NULL,
    consumption         NUMERIC(12,4) NOT NULL DEFAULT 0,
    data                NUMERIC(12,4) NOT NULL DEFAULT 0,  -- MB consumed
    minutes             NUMERIC(10,2) NOT NULL DEFAULT 0,  -- voice minutes consumed
    sms                 INTEGER       NOT NULL DEFAULT 0   -- SMS count consumed
);

-- ------------------------------------------------------------
-- ROR_CONTRACT
-- Tracking the ROR usage of that contract.
-- ------------------------------------------------------------
CREATE TABLE ror_contract (
    contract_id INTEGER NOT NULL REFERENCES contract(id),
    rateplan_id INTEGER NOT NULL REFERENCES rateplan(id),
    data        INTEGER,
    voice       INTEGER,
    sms         INTEGER,
    PRIMARY KEY (contract_id, rateplan_id)
-- bill_id added after bill table below (FK added via ALTER)
);

-- ------------------------------------------------------------
-- BILL
-- one bill per billing cycle per contract
-- ------------------------------------------------------------
CREATE TABLE bill (
    id                   SERIAL PRIMARY KEY,
    contract_id          INTEGER NOT NULL REFERENCES contract(id),
    billing_date         DATE NOT NULL,
    recurring_fees       NUMERIC(12,2) NOT NULL DEFAULT 0,
    one_time_fees        NUMERIC(12,2) NOT NULL DEFAULT 0,
    voice_usage          INTEGER NOT NULL DEFAULT 0,  -- seconds
    data_usage           INTEGER NOT NULL DEFAULT 0,  -- MB
    sms_usage            INTEGER NOT NULL DEFAULT 0,  -- count
    taxes                NUMERIC(12,2) NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- Now we can add the FK from ror_contract → bill
-- ------------------------------------------------------------
ALTER TABLE ror_contract
    ADD COLUMN bill_id INTEGER REFERENCES bill(id);

-- ------------------------------------------------------------
-- INVOICE
-- generated PDF invoice derived from a bill
-- ------------------------------------------------------------
CREATE TABLE invoice (
    id               SERIAL PRIMARY KEY,
    bill_id          INTEGER NOT NULL REFERENCES bill(id),
    pdf_path         TEXT,
    month  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- CDR (Call Detail Record)
-- raw usage event; parsed from file, rated against rateplan
-- ------------------------------------------------------------
CREATE TABLE cdr (
    id               SERIAL PRIMARY KEY,
    file_id          INTEGER NOT NULL REFERENCES file(id),
    caller_id        VARCHAR(20) NOT NULL,  -- calling party MSISDN
    receiver_id      VARCHAR(255) NOT NULL,  -- called party MSISDN
    start_time       TIMESTAMP NOT NULL,
    duration         INTEGER NOT NULL DEFAULT 0,  -- seconds
    service_id       INTEGER REFERENCES service_package(id),
    hplmn            VARCHAR(20),   -- Home PLMN code
    vplmn            VARCHAR(20),   -- Visited PLMN code (roaming)
    external_charges NUMERIC(12,2) NOT NULL DEFAULT 0,
    rated_flag       BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================
-- INDEXES (performance basics)
-- ============================================================
-- CREATE INDEX idx_cdr_rated_flag     ON cdr(rated_flag);
-- CREATE INDEX idx_cdr_file_id        ON cdr(file_id);
-- CREATE INDEX idx_cdr_dial_a         ON cdr(dial_a);
-- CREATE INDEX idx_contract_msisdn    ON contract(msisdn);
-- CREATE INDEX idx_contract_customer  ON contract(customer_id);
-- CREATE INDEX idx_bill_contract      ON bill(contract_id);
-- CREATE INDEX idx_bill_billing_date  ON bill(billing_date);
-- CREATE INDEX idx_invoice_bill       ON invoice(bill_id);
