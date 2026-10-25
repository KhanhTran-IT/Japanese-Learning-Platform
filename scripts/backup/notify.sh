#!/usr/bin/env bash
# ==============================================================================
# Notification Helper for Backup & Recovery Workflows
# Sends alerts to webhook endpoints (Slack, Discord, Alertmanager, custom)
# ==============================================================================
set -euo pipefail

SEVERITY="${1:-INFO}" # INFO, WARNING, CRITICAL
TITLE="${2:-Backup Notification}"
MESSAGE="${3:-No message provided}"
DETAILS="${4:-}"

WEBHOOK_URL="${BACKUP_NOTIFICATION_WEBHOOK:-${WEBHOOK_URL:-}}"
TIMESTAMP="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
HOSTNAME="$(hostname 2>/dev/null || echo "unknown-host")"

# Always log to stderr/stdout
echo "[${TIMESTAMP}] [${SEVERITY}] [${HOSTNAME}] ${TITLE}: ${MESSAGE}" >&2
if [[ -n "${DETAILS}" ]]; then
    echo "Details: ${DETAILS}" >&2
fi

# If no webhook configured, we are done
if [[ -z "${WEBHOOK_URL}" ]]; then
    exit 0
fi

# Format payload based on webhook target or generic JSON
COLOR="#36a64f" # green
if [[ "${SEVERITY}" == "WARNING" ]]; then
    COLOR="#ecb22e" # yellow
elif [[ "${SEVERITY}" == "CRITICAL" ]]; then
    COLOR="#e01e5a" # red
fi

PAYLOAD=$(cat <<EOF
{
  "timestamp": "${TIMESTAMP}",
  "host": "${HOSTNAME}",
  "severity": "${SEVERITY}",
  "title": "${TITLE}",
  "message": "${MESSAGE}",
  "details": "${DETAILS}",
  "attachments": [
    {
      "color": "${COLOR}",
      "title": "${TITLE}",
      "text": "${MESSAGE}\n${DETAILS}",
      "ts": $(date +%s)
    }
  ]
}
EOF
)

# Send webhook with a 10s timeout; do not crash the calling script if webhook fails
curl -s -S -X POST \
    -H "Content-Type: application/json" \
    -d "${PAYLOAD}" \
    --max-time 10 \
    "${WEBHOOK_URL}" >/dev/null 2>&1 || {
        echo "[WARNING] Failed to deliver webhook notification to ${WEBHOOK_URL}" >&2
    }
