#!/usr/bin/env bash
# ==============================================================================
# Production MariaDB Safe Backup Script
# Features:
#   - Online, non-blocking dump (--single-transaction, --quick)
#   - High compression (gzip -9)
#   - Strong AES-256-CBC PBKDF2 encryption
#   - Cryptographic integrity check (SHA-256)
#   - Tiered Grandfather-Father-Son retention (daily, weekly, monthly)
#   - External storage synchronization (S3, Rclone, Rsync)
#   - Atomic temporary writes & failure alerting
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Source config if available
if [[ -f "${SCRIPT_DIR}/backup.env" ]]; then
    # shellcheck disable=SC1091
    source "${SCRIPT_DIR}/backup.env"
elif [[ -f "${SCRIPT_DIR}/../../.env.prod" ]]; then
    # shellcheck disable=SC1091
    source "${SCRIPT_DIR}/../../.env.prod"
fi

# ---- Default Configuration ----
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-japanese_learning_db}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DOCKER_CONTAINER="${DOCKER_CONTAINER:-japanese_learning_db_prod}"
USE_DOCKER_EXEC="${USE_DOCKER_EXEC:-false}"

BACKUP_DIR="${BACKUP_DIR:-/var/backups/japanese-learning-db}"
MIN_BACKUP_SIZE_BYTES="${MIN_BACKUP_SIZE_BYTES:-10240}" # 10 KB minimum

RETENTION_DAYS_DAILY="${RETENTION_DAYS_DAILY:-7}"
RETENTION_WEEKS="${RETENTION_WEEKS:-4}"
RETENTION_MONTHS="${RETENTION_MONTHS:-12}"

EXTERNAL_STORAGE_TYPE="${EXTERNAL_STORAGE_TYPE:-none}"
EXTERNAL_STORAGE_DEST="${EXTERNAL_STORAGE_DEST:-}"

BACKUP_ENCRYPTION_KEY="${BACKUP_ENCRYPTION_KEY:-}"
BACKUP_ENCRYPTION_KEY_FILE="${BACKUP_ENCRYPTION_KEY_FILE:-}"

# Parse command line overrides
CUSTOM_TIER=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --tier)
            CUSTOM_TIER="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [--tier <daily|weekly|monthly>]"
            exit 0
            ;;
        *)
            echo "[ERROR] Unknown option: $1" >&2
            exit 1
            ;;
    esac
done

# ---- Helper for Alerting ----
notify() {
    local severity="$1"
    local title="$2"
    local message="$3"
    local details="${4:-}"
    if [[ -x "${SCRIPT_DIR}/notify.sh" ]]; then
        "${SCRIPT_DIR}/notify.sh" "${severity}" "${title}" "${message}" "${details}" || true
    else
        echo "[${severity}] ${title}: ${message} (${details})" >&2
    fi
}

# Trap unexpected errors
trap 'notify "CRITICAL" "MariaDB Backup Failed" "Script aborted unexpectedly on line ${LINENO} with command: ${BASH_COMMAND}"' ERR

# ---- Prerequisite Validation ----
command -v gzip >/dev/null 2>&1 || { echo "[ERROR] gzip is required but not installed." >&2; exit 1; }
command -v openssl >/dev/null 2>&1 || { echo "[ERROR] openssl is required but not installed." >&2; exit 1; }
command -v sha256sum >/dev/null 2>&1 || { echo "[ERROR] sha256sum is required but not installed." >&2; exit 1; }

# Determine dump tool
DUMP_CMD=""
if [[ "${USE_DOCKER_EXEC}" == "true" ]]; then
    command -v docker >/dev/null 2>&1 || { echo "[ERROR] USE_DOCKER_EXEC is true but docker binary was not found." >&2; exit 1; }
    DUMP_CMD="docker exec -i ${DOCKER_CONTAINER} mariadb-dump"
elif command -v mariadb-dump >/dev/null 2>&1; then
    DUMP_CMD="mariadb-dump"
elif command -v mysqldump >/dev/null 2>&1; then
    DUMP_CMD="mysqldump"
else
    echo "[ERROR] Neither mariadb-dump nor mysqldump is installed, and USE_DOCKER_EXEC is false." >&2
    exit 1
fi

# Validate credentials and key
if [[ -z "${DB_PASSWORD}" ]]; then
    echo "[ERROR] DB_PASSWORD is not set." >&2
    notify "CRITICAL" "MariaDB Backup Failed" "DB_PASSWORD is missing or empty."
    exit 1
fi

OPENSSL_PASS_ARG=()
if [[ -n "${BACKUP_ENCRYPTION_KEY_FILE}" && -f "${BACKUP_ENCRYPTION_KEY_FILE}" ]]; then
    OPENSSL_PASS_ARG=(-pass "file:${BACKUP_ENCRYPTION_KEY_FILE}")
elif [[ -n "${BACKUP_ENCRYPTION_KEY}" ]]; then
    OPENSSL_PASS_ARG=(-pass "pass:${BACKUP_ENCRYPTION_KEY}")
else
    echo "[ERROR] Neither BACKUP_ENCRYPTION_KEY nor BACKUP_ENCRYPTION_KEY_FILE is set." >&2
    notify "CRITICAL" "MariaDB Backup Failed" "Encryption key is unconfigured. Plaintext backups are forbidden in production."
    exit 1
fi

# ---- Determine Tier & Paths ----
DATE_DAY_OF_MONTH="$(date +%d)"
DATE_DAY_OF_WEEK="$(date +%u)" # 1=Mon ... 7=Sun
TIMESTAMP="$(date +"%Y%m%d_%H%M%S")"

if [[ -n "${CUSTOM_TIER}" ]]; then
    TIER="${CUSTOM_TIER}"
elif [[ "${DATE_DAY_OF_MONTH}" == "01" ]]; then
    TIER="monthly"
elif [[ "${DATE_DAY_OF_WEEK}" == "7" ]]; then
    TIER="weekly"
else
    TIER="daily"
fi

TARGET_DIR="${BACKUP_DIR}/${TIER}"
TMP_DIR="${BACKUP_DIR}/tmp"
mkdir -p "${TARGET_DIR}" "${TMP_DIR}"

BASE_NAME="${DB_NAME}_${TIER}_${TIMESTAMP}"
TMP_ENC_FILE="${TMP_DIR}/${BASE_NAME}.sql.gz.enc"
FINAL_ENC_FILE="${TARGET_DIR}/${BASE_NAME}.sql.gz.enc"
FINAL_SHA_FILE="${TARGET_DIR}/${BASE_NAME}.sql.gz.enc.sha256"
FINAL_META_FILE="${TARGET_DIR}/${BASE_NAME}.sql.gz.enc.meta.json"

echo "=============================================================================="
echo "Starting MariaDB Backup: ${DB_NAME}"
echo "Tier:        ${TIER}"
echo "Timestamp:   ${TIMESTAMP}"
echo "Target:      ${FINAL_ENC_FILE}"
echo "=============================================================================="

# ---- Execute Dump, Compress & Encrypt ----
echo "[1/5] Dumping, compressing and encrypting database..."
START_TIME=$(date +%s)

# Non-locking flags: --single-transaction --quick --routines --triggers --events
DUMP_FLAGS=(
    "--single-transaction"
    "--quick"
    "--routines"
    "--triggers"
    "--events"
    "--default-character-set=utf8mb4"
    "--hex-blob"
    "--no-autocommit"
)

if [[ "${USE_DOCKER_EXEC}" == "true" ]]; then
    # When using docker exec, flags run inside the container
    ${DUMP_CMD} -u"${DB_USER}" -p"${DB_PASSWORD}" "${DUMP_FLAGS[@]}" "${DB_NAME}" \
        | gzip -9 \
        | openssl enc -aes-256-cbc -pbkdf2 -iter 100000 -salt "${OPENSSL_PASS_ARG[@]}" -out "${TMP_ENC_FILE}"
else
    ${DUMP_CMD} -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" "${DUMP_FLAGS[@]}" "${DB_NAME}" \
        | gzip -9 \
        | openssl enc -aes-256-cbc -pbkdf2 -iter 100000 -salt "${OPENSSL_PASS_ARG[@]}" -out "${TMP_ENC_FILE}"
fi

# Check pipeline status (all stages must exit 0)
PIPE_STATUSES=("${PIPESTATUS[@]}")
for status in "${PIPE_STATUSES[@]}"; do
    if [[ "$status" -ne 0 ]]; then
        rm -f "${TMP_ENC_FILE}"
        echo "[ERROR] Dump pipeline failed with statuses: ${PIPE_STATUSES[*]}" >&2
        notify "CRITICAL" "MariaDB Backup Failed" "Dump or encryption pipeline exited with non-zero code (${PIPE_STATUSES[*]})."
        exit 1
    fi
done

# ---- Integrity Validation ----
echo "[2/5] Running integrity checks on encrypted artifact..."

# Size check
ENC_SIZE=$(stat -c%s "${TMP_ENC_FILE}" 2>/dev/null || stat -f%z "${TMP_ENC_FILE}")
if [[ "${ENC_SIZE}" -lt "${MIN_BACKUP_SIZE_BYTES}" ]]; then
    rm -f "${TMP_ENC_FILE}"
    echo "[ERROR] Backup size (${ENC_SIZE} bytes) is below minimum threshold (${MIN_BACKUP_SIZE_BYTES} bytes)." >&2
    notify "CRITICAL" "MariaDB Backup Failed" "Backup size is suspiciously small (${ENC_SIZE} bytes < ${MIN_BACKUP_SIZE_BYTES} bytes)."
    exit 1
fi

# Decryption test check: ensure the passphrase can decrypt the header without error
if ! head -c 65536 "${TMP_ENC_FILE}" | openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 "${OPENSSL_PASS_ARG[@]}" 2>/dev/null | head -c 16 >/dev/null; then
    rm -f "${TMP_ENC_FILE}"
    echo "[ERROR] Backup decryption test failed! The encryption key or output stream is corrupted." >&2
    notify "CRITICAL" "MariaDB Backup Failed" "Encryption verification test failed. Output file cannot be decrypted."
    exit 1
fi

# Compute SHA-256 checksum
SHA256_HASH=$(sha256sum "${TMP_ENC_FILE}" | awk '{print $1}')

# Atomic move from tmp to final target directory
mv "${TMP_ENC_FILE}" "${FINAL_ENC_FILE}"
echo "${SHA256_HASH}  $(basename "${FINAL_ENC_FILE}")" > "${FINAL_SHA_FILE}"

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# Write metadata record
cat <<EOF > "${FINAL_META_FILE}"
{
  "database": "${DB_NAME}",
  "tier": "${TIER}",
  "timestamp": "${TIMESTAMP}",
  "file": "$(basename "${FINAL_ENC_FILE}")",
  "size_bytes": ${ENC_SIZE},
  "sha256": "${SHA256_HASH}",
  "duration_seconds": ${DURATION},
  "cipher": "aes-256-cbc-pbkdf2-iter100000",
  "compression": "gzip-9",
  "created_at_utc": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "host": "$(hostname 2>/dev/null || echo "unknown")"
}
EOF

echo "Artifact created: ${FINAL_ENC_FILE} (${ENC_SIZE} bytes in ${DURATION}s)"
echo "SHA-256 Checksum: ${SHA256_HASH}"

# ---- Retention Policy Execution ----
echo "[3/5] Applying local retention policy..."
# Daily retention
find "${BACKUP_DIR}/daily" -name "*.sql.gz.enc*" -type f -mtime +"${RETENTION_DAYS_DAILY}" -exec rm -f {} + 2>/dev/null || true
# Weekly retention (weeks * 7 days)
WEEKLY_DAYS=$((RETENTION_WEEKS * 7))
find "${BACKUP_DIR}/weekly" -name "*.sql.gz.enc*" -type f -mtime +"${WEEKLY_DAYS}" -exec rm -f {} + 2>/dev/null || true
# Monthly retention (months * 30 days)
MONTHLY_DAYS=$((RETENTION_MONTHS * 30))
find "${BACKUP_DIR}/monthly" -name "*.sql.gz.enc*" -type f -mtime +"${MONTHLY_DAYS}" -exec rm -f {} + 2>/dev/null || true

# ---- External Storage Synchronization ----
echo "[4/5] External storage synchronization (${EXTERNAL_STORAGE_TYPE})..."
if [[ "${EXTERNAL_STORAGE_TYPE}" == "s3" ]]; then
    if ! command -v aws >/dev/null 2>&1; then
        echo "[WARNING] 'aws' CLI not found. Skipping S3 upload." >&2
        notify "WARNING" "Backup S3 Sync Skipped" "AWS CLI not found on server."
    elif [[ -z "${EXTERNAL_STORAGE_DEST}" ]]; then
        echo "[WARNING] EXTERNAL_STORAGE_DEST is empty. Skipping S3 upload." >&2
    else
        echo "Uploading to ${EXTERNAL_STORAGE_DEST%/}/${TIER}/..."
        aws s3 cp "${FINAL_ENC_FILE}" "${EXTERNAL_STORAGE_DEST%/}/${TIER}/"
        aws s3 cp "${FINAL_SHA_FILE}" "${EXTERNAL_STORAGE_DEST%/}/${TIER}/"
        aws s3 cp "${FINAL_META_FILE}" "${EXTERNAL_STORAGE_DEST%/}/${TIER}/"
    fi
elif [[ "${EXTERNAL_STORAGE_TYPE}" == "rclone" ]]; then
    if ! command -v rclone >/dev/null 2>&1; then
        echo "[WARNING] 'rclone' not found. Skipping rclone sync." >&2
        notify "WARNING" "Backup Rclone Sync Skipped" "Rclone binary not found on server."
    elif [[ -z "${EXTERNAL_STORAGE_DEST}" ]]; then
        echo "[WARNING] EXTERNAL_STORAGE_DEST is empty. Skipping rclone sync." >&2
    else
        echo "Copying to ${EXTERNAL_STORAGE_DEST}/${TIER}/..."
        rclone copy "${FINAL_ENC_FILE}" "${EXTERNAL_STORAGE_DEST}/${TIER}/"
        rclone copy "${FINAL_SHA_FILE}" "${EXTERNAL_STORAGE_DEST}/${TIER}/"
        rclone copy "${FINAL_META_FILE}" "${EXTERNAL_STORAGE_DEST}/${TIER}/"
    fi
elif [[ "${EXTERNAL_STORAGE_TYPE}" == "rsync" ]]; then
    if ! command -v rsync >/dev/null 2>&1; then
        echo "[WARNING] 'rsync' not found. Skipping rsync." >&2
    elif [[ -z "${EXTERNAL_STORAGE_DEST}" ]]; then
        echo "[WARNING] EXTERNAL_STORAGE_DEST is empty. Skipping rsync." >&2
    else
        rsync -avz "${FINAL_ENC_FILE}" "${FINAL_SHA_FILE}" "${FINAL_META_FILE}" "${EXTERNAL_STORAGE_DEST%/}/${TIER}/"
    fi
else
    echo "No external storage configured (EXTERNAL_STORAGE_TYPE=${EXTERNAL_STORAGE_TYPE})."
fi

# ---- Completion Notification ----
echo "[5/5] Backup completed successfully."
notify "INFO" "MariaDB Backup Completed" "Tier: ${TIER}, Size: ${ENC_SIZE} bytes, File: $(basename "${FINAL_ENC_FILE}")" "SHA256: ${SHA256_HASH}"

echo "=============================================================================="
echo "Backup Workflow Successfully Finished!"
echo "File:     ${FINAL_ENC_FILE}"
echo "Checksum: ${FINAL_SHA_FILE}"
echo "Metadata: ${FINAL_META_FILE}"
echo "=============================================================================="
exit 0
