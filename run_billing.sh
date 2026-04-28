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

echo ""                                                >> "$LOG_FILE"
echo "============================================"   >> "$LOG_FILE"
echo "  Billing run started: $(date)"                 >> "$LOG_FILE"
echo "============================================"   >> "$LOG_FILE"

# Verify PROJECT_DIR exists
if [ ! -d "$PROJECT_DIR" ]; then
    echo "ERROR: PROJECT_DIR not found: $PROJECT_DIR" >> "$LOG_FILE"
    echo "       Please update PROJECT_DIR at the top of this script." >> "$LOG_FILE"
    exit 1
fi

# Verify JAVA exists
if [ ! -f "$JAVA" ]; then
    echo "ERROR: Java binary not found: $JAVA"        >> "$LOG_FILE"
    echo "       Run 'which java' to find your Java path." >> "$LOG_FILE"
    exit 1
fi

# Move to project directory
cd "$PROJECT_DIR" || {
    echo "ERROR: Cannot cd into $PROJECT_DIR"         >> "$LOG_FILE"
    exit 1
}

# Copy dependencies if not already present
if [ ! -d "target/dependency" ]; then
    echo ">> Copying dependencies..."                 >> "$LOG_FILE"
    mvn dependency:copy-dependencies -q              >> "$LOG_FILE" 2>&1
fi

# Run the billing job
echo ">> Running BillingCycleJob..."                  >> "$LOG_FILE"
"$JAVA" -cp "target/classes:target/dependency/*" \
    com.a3m.billing.job.BillingCycleJob              >> "$LOG_FILE" 2>&1

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ">> SUCCESS"                                 >> "$LOG_FILE"
else
    echo ">> FAILED (exit code: $EXIT_CODE)"          >> "$LOG_FILE"
fi

echo "  Billing run finished: $(date)"               >> "$LOG_FILE"
echo "============================================"   >> "$LOG_FILE"

exit $EXIT_CODE

