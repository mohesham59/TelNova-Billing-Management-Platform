#!/usr/bin/env bash
# ============================================================
#  run_billing.sh  – TeleMeter Billing Cycle Runner
#  Called by the billing-cron container (or manually).
#  DB credentials come from environment variables.
# ============================================================
set -euo pipefail

# --- Config (read from Docker env or defaults) ---
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-telecom_billing}"
DB_USER="${DB_USER:-telecom}"
PGPASSWORD="${DB_PASSWORD:-telecom_pass}"
export PGPASSWORD

LOG_DIR="/logs"
LOG_FILE="${LOG_DIR}/billing_$(date +'%Y%m').log"
TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')

mkdir -p "${LOG_DIR}"

log() {
    echo "[${TIMESTAMP}] $*" | tee -a "${LOG_FILE}"
}

log "=========================================="
log "  Billing Cycle Started"
log "=========================================="
log "  DB: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

# Run the billing stored procedure
log "Calling bill_all_active_contracts()..."
psql \
    -h "${DB_HOST}" \
    -p "${DB_PORT}" \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    -v ON_ERROR_STOP=1 \
    -c "SELECT bill_all_active_contracts();" \
    >> "${LOG_FILE}" 2>&1

EXIT_CODE=$?

if [ "${EXIT_CODE}" -eq 0 ]; then
    log "Billing cycle completed successfully."
else
    log "ERROR: Billing cycle failed with exit code ${EXIT_CODE}."
    exit "${EXIT_CODE}"
fi

log "=========================================="
