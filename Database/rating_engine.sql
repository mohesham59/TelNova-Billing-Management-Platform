-- ============================================================
-- FUNCTION: rate_cdrs()
-- ============================================================
-- Reads every unrated CDR, determines the cost per CDR based
-- on the contract's rateplan, deducts from the bundle
-- first, then charges the overflow at the ROR price until the
-- customer's available credit is exhausted.
-- Returns one result row per CDR that was processed.
-- ============================================================

DROP FUNCTION IF EXISTS rate_cdrs();

CREATE OR REPLACE FUNCTION rate_cdrs()
RETURNS TABLE (
    cdr_id        INTEGER,   
    caller        VARCHAR,   
    service       service_type,
    duration      INTEGER,  
    bundle_used   NUMERIC,  
    extra_usage   NUMERIC,   
    cost          NUMERIC,   
    credit_before NUMERIC,   
    credit_after  NUMERIC   
) AS $func$
DECLARE
    rec              RECORD;   -- holds one CDR row + its joined contract/rateplan data
    v_remaining      NUMERIC;  -- the units still available in the bundle
    v_cost           NUMERIC;  -- cost calculated for each row per cdr
    v_extra          NUMERIC;  -- units that exceeded the bundle and are billed at ROR
    v_ror            NUMERIC;  -- price per unit for this service type
    v_cc_id          INTEGER;  -- primary key of the contract_consumption row being deducted
    v_bundle_used    NUMERIC;  -- units actually consumed from the bundle for this CDR
    v_credit_before  NUMERIC;  -- snapshot of available_credit before we touch it
    v_current_credit NUMERIC;  -- fresh read of available_credit from DB each iteration
                               
BEGIN
    -- Main loop: one iteration per unrated CDR 
    
    FOR rec IN
        SELECT
            c.*,                        -- all CDR columns (c.id, c.caller_id, c.service_type, c.duration …)
            ct.id               AS contract_id,      -- contract PK (separate alias to avoid clash with c.id)
            ct.available_credit AS available_credit,  -- starting credit snapshot from the join
            ct.rateplan_id,
            rp.ror_voice,               -- price per second for voice calls
            rp.ror_sms,                 -- price per SMS message
            rp.ror_data                 -- price per byte/MB for data
        FROM   cdr      c
        JOIN   contract ct ON c.caller_id     = ct.msisdn
        JOIN   rateplan  rp ON ct.rateplan_id  = rp.id
        WHERE  c.rated_flag = FALSE       -- only process CDRs not yet rated
          AND  ct.status    = 'active'    -- skip suspended / de-active contracts
        ORDER  BY c.start_time            -- chronological order within the run
    LOOP
        -- ── Step 1: Pick the correct ROR rate for this service type ────────
        -- v_ror is used later both to calculate cost and to track ROR usage.
        IF rec.service_type = 'voice' THEN
            v_ror := rec.ror_voice;
        ELSIF rec.service_type = 'sms' THEN
            v_ror := rec.ror_sms;
        ELSE
            v_ror := rec.ror_data;
        END IF;

        -- ── Step 2: Reset all per-CDR accumulators ────────────────────────
        -- These are reused each iteration so must be zeroed explicitly.
        v_cost        := 0;
        v_bundle_used := 0;
        v_extra       := 0;
        v_cc_id       := NULL;  

        -- ── Step 3: Refresh available credit from the database ────────────
        -- We re-read from the contract table instead of using rec.available_credit
        -- because a previous iteration in this same run may have already deducted
        -- from this customer's credit. 
        SELECT available_credit
        INTO   v_current_credit
        FROM   contract
        WHERE  id = rec.contract_id;

        v_credit_before := v_current_credit;  -- save snapshot for the result row

        -- ── Step 4: Find the first bundle with remaining free units ────────
        -- We join through rateplan_packages to ensure we only look at bundles
        -- that belong to this customer's rateplan (not all service_packages).
        -- The filter (sp.amount - cc.consumption > 0) skips exhausted bundles.
        -- priority ASC means lower-numbered priorities are consumed first.
        -- LIMIT 1 picks only the single highest-priority non-empty bundle.
        SELECT
            cc.id,
            (sp.amount - cc.consumption)   -- remaining free units in this bundle
        INTO
            v_cc_id,
            v_remaining
        FROM   contract_consumption cc
        JOIN   service_package   sp  ON cc.service_package_id = sp.id
        JOIN   rateplan_packages rpp ON rpp.package_id        = sp.id
        WHERE  cc.contract_id         = rec.contract_id
          AND  rpp.rateplan_id        = rec.rateplan_id
          AND  sp.type                = rec.service_type       -- match voice/sms/data
          AND  sp.amount - cc.consumption > 0                  -- skip exhausted bundles
        ORDER  BY sp.priority ASC
        LIMIT  1;

        -- If no bundle exists at all, treat remaining as zero so we fall
        -- straight into full ROR billing below.
        v_remaining := COALESCE(v_remaining, 0);

        -- ── Step 5: Deduct from bundle, then calculate ROR cost ───────────
        IF v_remaining > 0 THEN
            IF rec.duration <= v_remaining THEN
                -- The entire CDR is covered by the free bundle — no charge.
                UPDATE contract_consumption
                SET    consumption = consumption + rec.duration
                WHERE  id = v_cc_id;

                v_bundle_used := rec.duration;
                v_cost        := 0;         -- customer pays nothing for this CDR
            ELSE
                -- CDR exceeds the bundle: use what remains for free,
                -- then bill the overflow (v_extra) at the ROR rate.
                v_bundle_used := v_remaining;
                v_extra       := rec.duration - v_remaining;  -- units beyond bundle

                UPDATE contract_consumption
                SET    consumption = consumption + v_remaining  -- exhaust the bundle
                WHERE  id = v_cc_id;

                v_cost := v_extra * v_ror;  -- charge only the overflow
            END IF;
        ELSE
            -- No bundle at all for this service type on this contract.
            -- Every unit is billed at the ROR rate.
            v_extra := rec.duration;
            v_cost  := rec.duration * v_ror;
        END IF;

        -- ── Step 6: Cap the cost at the customer's available credit ───────
        -- A customer cannot be charged more than they have.
        -- If credit is insufficient, we charge whatever is left (could be 0).
        IF v_cost > v_current_credit THEN
            v_cost := v_current_credit;
        END IF;

        -- ── Step 7: Deduct the cost from the contract's available credit ──
        IF v_cost > 0 THEN
            UPDATE contract
            SET    available_credit = available_credit - v_cost
            WHERE  id = rec.contract_id;
        END IF;

        -- ── Step 8: Track ROR usage in ror_contract ───────────────────────
        -- ror_contract records how many extra (out-of-bundle) units were used
        -- per service type per billing cycle so generate_bill() can report them.
        -- We use v_extra (the true extra units) NOT a back-calculation from v_cost,
        -- because if the cost was capped by credit the division would give a wrong count.
        -- The INSERT … ON CONFLICT ensures the row exists before we UPDATE it.
        IF v_extra > 0 THEN
            INSERT INTO ror_contract (contract_id, rateplan_id, voice, sms, data)
            VALUES (rec.contract_id, rec.rateplan_id, 0, 0, 0)
            ON CONFLICT (contract_id, rateplan_id) DO NOTHING;

            IF rec.service_type = 'voice' THEN
                UPDATE ror_contract
                SET    voice = COALESCE(voice, 0) + v_extra::INTEGER
                WHERE  contract_id = rec.contract_id
                  AND  rateplan_id = rec.rateplan_id;

            ELSIF rec.service_type = 'sms' THEN
                UPDATE ror_contract
                SET    sms = COALESCE(sms, 0) + v_extra::INTEGER
                WHERE  contract_id = rec.contract_id
                  AND  rateplan_id = rec.rateplan_id;

            ELSE
                UPDATE ror_contract
                SET    data = COALESCE(data, 0) + v_extra::INTEGER
                WHERE  contract_id = rec.contract_id
                  AND  rateplan_id = rec.rateplan_id;
            END IF;
        END IF;

        -- ── Step 9: Mark the CDR as rated and store the calculated cost ───
        -- rated_flag = TRUE prevents this CDR from being picked up again.
        -- rated_cost stores what our rating engine calculated (internal cost).
        -- external_charges was already populated by the CDR parser from the
        -- source file and is left untouched here (read-only parsed input).
        UPDATE cdr
        SET    rated_flag  = TRUE,
               rated_cost  = v_cost
        WHERE  id = rec.id;

        -- ── Step 10: Emit one result row for this CDR ─────────────────────
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
