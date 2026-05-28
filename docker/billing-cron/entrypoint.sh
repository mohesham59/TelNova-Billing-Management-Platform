#!/usr/bin/env bash
# ============================================================
# Billing Cron Entrypoint
# ============================================================

set -euo pipefail

CRON_SCHEDULE="${BILLING_CRON:-0 1 * * *}"
SCRIPT_PATH="/scripts/run_billing.sh"
LOG_FILE="/logs/billing_cron.log"

echo "[billing-cron] Schedule: ${CRON_SCHEDULE}"
echo "[billing-cron] Script  : ${SCRIPT_PATH}"

# Create crontab
CRONTAB_FILE="/tmp/billing_crontab"

cat > "${CRONTAB_FILE}" <<EOF
# TeleMeter Billing Cycle Cron
${CRON_SCHEDULE} ${SCRIPT_PATH} >> ${LOG_FILE} 2>&1
EOF

echo "[billing-cron] Generated crontab:"
cat "${CRONTAB_FILE}"

# Start supercronic
exec supercronic "${CRONTAB_FILE}"
