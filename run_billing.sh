#!/bin/bash

# ================================================================
#  TeleMeter — Monthly Billing Cycle Script
# ================================================================
#
#  SETUP (do this once):
#  1. Set PROJECT_DIR to the full path of your TeleMeter/ folder
#  2. Set JAVA to the full path of your java binary
#     Run: which java  — to find it
#  3. Make this script executable:
#     chmod +x run_billing.sh
#  4. Test it manually:
#     ./run_billing.sh
#
#  SCHEDULE WITH CRON (runs on 1st of every month at 00:30):
#  Run: crontab -e
#  Add: 30 0 1 * * /full/path/to/run_billing.sh
#
# ================================================================

# ── EDIT THESE TWO LINES TO MATCH YOUR MACHINE ─────────────────
PROJECT_DIR="/your/path/to/TeleMeter-Billing-Management-Platform/TeleMeter"
JAVA="/your/path/to/jdk/bin/java"
# ───────────────────────────────────────────────────────────────
#
# Examples:
#   Linux:   PROJECT_DIR="/home/mohesham/Desktop/ITI - Telecom/TeleMeter-Billing-Management-Platform/TeleMeter"
#            JAVA="/usr/bin/java"   # or wherever your java is — run: which java

#   macOS:   PROJECT_DIR="/Users/youruser/TeleMeter-Billing-Management-Platform/TeleMeter"
#            JAVA="/usr/bin/java"

#   Windows: Use Git Bash or WSL and follow the Linux format above
# ───────────────────────────────────────────────────────────────


LOG_DIR="$PROJECT_DIR/../logs"
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
