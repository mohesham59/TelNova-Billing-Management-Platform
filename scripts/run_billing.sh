#!/bin/bash

# ================================================================
#  TeleMeter — Monthly Billing Cycle Script
# ================================================================
#
#  SETUP (do this once):
#  1. Make this script executable:
#     chmod +x scripts/run_billing.sh
#  2. Test it manually:
#     ./scripts/run_billing.sh
#
#  SCHEDULE WITH CRON (runs on 1st of every month at 00:30):
#  Run: crontab -e
#  Add: 30 0 1 * * /full/path/to/scripts/run_billing.sh
#
# ================================================================

# Optional overrides:
#   PROJECT_DIR=/abs/path/to/repo/apps/TeleMeter ./scripts/run_billing.sh
#   JAVA=/abs/path/to/java ./scripts/run_billing.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_DIR="${PROJECT_DIR:-$REPO_ROOT/apps/TeleMeter}"
JAVA="${JAVA:-$(command -v java)}"
LOG_DIR="$REPO_ROOT/logs"
LOG_FILE="$LOG_DIR/billing_$(date +%Y%m).log"

mkdir -p "$LOG_DIR"

echo "" >> "$LOG_FILE"
echo "============================================" >> "$LOG_FILE"
echo "  Billing run started: $(date)" >> "$LOG_FILE"
echo "============================================" >> "$LOG_FILE"

# ================================================================
#  CHECK PROJECT DIR
# ================================================================
if [ ! -d "$PROJECT_DIR" ]; then
    echo "ERROR: PROJECT_DIR not found: $PROJECT_DIR" >> "$LOG_FILE"
    exit 1
fi

# ================================================================
#  CHECK JAVA
# ================================================================
if [ ! -f "$JAVA" ]; then
    echo "ERROR: Java not found: $JAVA" >> "$LOG_FILE"
    exit 1
fi

# ================================================================
#  MOVE TO PROJECT
# ================================================================
cd "$PROJECT_DIR" || {
    echo "ERROR: Cannot cd into $PROJECT_DIR" >> "$LOG_FILE"
    exit 1
}

# ================================================================
#  COPY DEPENDENCIES (if needed)
# ================================================================
if [ ! -d "target/dependency" ]; then
    echo ">> Copying dependencies..." >> "$LOG_FILE"
    mvn dependency:copy-dependencies -q >> "$LOG_FILE" 2>&1
fi

# ================================================================
#  STEP 1 — PARSE CDR FILES
# ================================================================
echo "============================================" >> "$LOG_FILE"
echo ">> STEP 1: CDR Parsing Started" >> "$LOG_FILE"

"$JAVA" -cp "target/classes:target/dependency/*" \
    com.telecom.parser.CdrParser >> "$LOG_FILE" 2>&1

PARSE_EXIT=$?

if [ $PARSE_EXIT -ne 0 ]; then
    echo ">> FAILED: CDR Parsing failed" >> "$LOG_FILE"
    echo ">> Billing aborted" >> "$LOG_FILE"
    echo "============================================" >> "$LOG_FILE"
    exit 1
fi

echo ">> STEP 1: CDR Parsing Completed" >> "$LOG_FILE"

# ================================================================
#  STEP 2 — BILLING ENGINE
# ================================================================
echo "============================================" >> "$LOG_FILE"
echo ">> STEP 2: BillingCycleJob Started" >> "$LOG_FILE"

"$JAVA" -cp "target/classes:target/dependency/*" \
    com.a3m.billing.job.BillingCycleJob >> "$LOG_FILE" 2>&1

EXIT_CODE=$?

# ================================================================
#  RESULT
# ================================================================
if [ $EXIT_CODE -eq 0 ]; then
    echo ">> SUCCESS: Billing completed" >> "$LOG_FILE"
else
    echo ">> FAILED (exit code: $EXIT_CODE)" >> "$LOG_FILE"
fi

echo "  Billing run finished: $(date)" >> "$LOG_FILE"
echo "============================================" >> "$LOG_FILE"

exit $EXIT_CODE
