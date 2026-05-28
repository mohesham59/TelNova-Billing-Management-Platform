# ============================================================
#  Billing Cron Container
#  Runs scripts/run_billing.sh on a configurable cron schedule
#  Uses supercronic for reliable container-friendly cron
# ============================================================

FROM debian:bookworm-slim

# Install PostgreSQL client (psql) + wget + ca-certs
RUN apt-get update && apt-get install -y --no-install-recommends \
        postgresql-client \
        wget \
        ca-certificates \
        bash \
    && rm -rf /var/lib/apt/lists/*

# Install supercronic (lightweight cron for containers)
ENV SUPERCRONIC_VERSION=v0.2.29
RUN wget -qO /usr/local/bin/supercronic \
      "https://github.com/aptible/supercronic/releases/download/${SUPERCRONIC_VERSION}/supercronic-linux-amd64" \
    && chmod +x /usr/local/bin/supercronic

# Create log directory
RUN mkdir -p /logs

# Copy entrypoint script that generates the crontab at runtime
# Path relative to project root (build context = .)
COPY docker/billing-cron/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
