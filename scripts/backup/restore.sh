#!/usr/bin/env bash
# ==============================================================================
# Production MariaDB Safe Restore Script
# Features:
#   - Checksum verification prior to decryption
#   - Decryption (AES-256-CBC PBKDF2) + Decompression stream
#   - Safe target routing (supports isolated test databases & containers)
#   - Production overwrite guard (--confirm-destructive-overwrite)
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

# ---- Parameters & Defaults ----
BACKUP_FILE="${1:-}"
TARGET_DB="${DB_NAME:-japanese_learning_db}"
TARGET_HOST="${DB_HOST:-127.0.0.1}"
TARGET_PORT="${DB_PORT:-3306}"
TARGET_USER="${DB_USER:-root}"
TARGET_PASSWORD="${DB_PASSWORD:-}"
TARGET_CONTAINER=""
USE_DOCKER_TARGET="false"

CONFIRM_OVERWRITE="false"
SKIP_CHECKSUM="false"

BACKUP_ENCRYPTION_KEY="${BACKUP_ENCRYPTION_KEY:-}"
BACKUP_ENCRYPTION_KEY_FILE="${BACKUP_ENCRYPTION_KEY_FILE:-}"

# Parse optional arguments
shift || true
while [[ $# -gt 0 ]]; do
    case "$1" in
        --confirm-destructive-overwrite)
            CONFIRM_OVERWRITE="true"
            shift
            ;;
        --target-db)
            TARGET_DB="$2"
            shift 2
            ;;
        --target-host)
            TARGET_HOST="$2"
            shift 2
            ;;
        --target-port)
            TARGET_PORT="$2"
            shift 2
            ;;
        --target-user)
            TARGET_USER="$2"
            shift 2
            ;;
        --target-password)
            TARGET_PASSWORD="$2"
            shift 2
            ;;
        --container)
            TARGET_CONTAINER="$2"
            USE_DOCKER_TARGET="true"
            shift 2
            ;;
        --skip-checksum)
            SKIP_CHECKSUM="true"
            shift
            ;;
        --help|-h)
            cat <<EOF
Usage: $0 <backup-file.sql.gz.enc> [OPTIONS]

Options:
  --target-db <name>             Target database name (default: ${DB_NAME:-japanese_learning_db})
  --target-host <host>           Target database host (default: ${DB_HOST:-127.0.0.1})
  --target-port <port>           Target database port (default: ${DB_PORT:-3306})
  --target-user <user>           Target database user (default: ${DB_USER:-root})
  --target-password <pass>       Target database password
  --container <container_name>   Restore into a running Docker container via docker exec
  --confirm-destructive-overwrite Required flag to confirm destructive restore
  --skip-checksum                Skip checking .sha256 companion file (not recommended)
EOF
            exit 0
            ;;
        *)
            echo "[ERROR] Unknown option: $1" >&2
            exit 1
            ;;
    esac
done

if [[ -z "${BACKUP_FILE}" || ! -f "${BACKUP_FILE}" ]]; then
    echo "[ERROR] Valid backup file must be provided as first argument." >&2
    exit 1
fi

if [[ "${CONFIRM_OVERWRITE}" != "true" ]]; then
    echo "==============================================================================" >&2
    echo "[CRITICAL SAFETY GUARD] RESTORE BLOCKED!" >&2
    echo "Restoring a database will OVERWRITE all existing data in target: '${TARGET_DB}'" >&2
    echo "To proceed, you MUST pass the flag: --confirm-destructive-overwrite" >&2
    echo "Example:" >&2
    echo "  $0 ${BACKUP_FILE} --confirm-destructive-overwrite" >&2
    echo "==============================================================================" >&2
    exit 1
fi

# Validate tools
command -v gzip >/dev/null 2>&1 || { echo "[ERROR] gzip is required." >&2; exit 1; }
command -v openssl >/dev/null 2>&1 || { echo "[ERROR] openssl is required." >&2; exit 1; }
command -v sha256sum >/dev/null 2>&1 || { echo "[ERROR] sha256sum is required." >&2; exit 1; }

# Setup encryption pass
OPENSSL_PASS_ARG=()
if [[ -n "${BACKUP_ENCRYPTION_KEY_FILE}" && -f "${BACKUP_ENCRYPTION_KEY_FILE}" ]]; then
    OPENSSL_PASS_ARG=(-pass "file:${BACKUP_ENCRYPTION_KEY_FILE}")
elif [[ -n "${BACKUP_ENCRYPTION_KEY}" ]]; then
    OPENSSL_PASS_ARG=(-pass "pass:${BACKUP_ENCRYPTION_KEY}")
else
    echo "[ERROR] Encryption key is required to decrypt this backup." >&2
    exit 1
fi

# ---- Step 1: Checksum Verification ----
echo "[1/3] Verifying SHA-256 checksum..."
SHA_FILE="${BACKUP_FILE}.sha256"
if [[ "${SKIP_CHECKSUM}" == "false" && -f "${SHA_FILE}" ]]; then
    EXPECTED_HASH=$(awk '{print $1}' "${SHA_FILE}")
    ACTUAL_HASH=$(sha256sum "${BACKUP_FILE}" | awk '{print $1}')
    if [[ "${EXPECTED_HASH}" != "${ACTUAL_HASH}" ]]; then
        echo "[ERROR] CHECKSUM MISMATCH!" >&2
        echo "Expected: ${EXPECTED_HASH}" >&2
        echo "Actual:   ${ACTUAL_HASH}" >&2
        echo "Backup file is corrupted or has been tampered with. Aborting restore." >&2
        exit 2
    fi
    echo "Checksum verified: ${ACTUAL_HASH} OK"
else
    echo "[INFO] Companion checksum file not found or skipped. Proceeding with caution."
fi

# ---- Step 2: Target Client Setup ----
CLIENT_CMD=""
if [[ "${USE_DOCKER_TARGET}" == "true" ]]; then
    command -v docker >/dev/null 2>&1 || { echo "[ERROR] docker is required for container restore." >&2; exit 1; }
    CLIENT_CMD="docker exec -i ${TARGET_CONTAINER} mariadb -u${TARGET_USER} -p${TARGET_PASSWORD} ${TARGET_DB}"
elif command -v mariadb >/dev/null 2>&1; then
    CLIENT_CMD="mariadb -h ${TARGET_HOST} -P ${TARGET_PORT} -u ${TARGET_USER} -p${TARGET_PASSWORD} ${TARGET_DB}"
elif command -v mysql >/dev/null 2>&1; then
    CLIENT_CMD="mysql -h ${TARGET_HOST} -P ${TARGET_PORT} -u ${TARGET_USER} -p${TARGET_PASSWORD} ${TARGET_DB}"
else
    echo "[ERROR] Neither mariadb nor mysql client is available locally." >&2
    exit 1
fi

# ---- Step 3: Stream Decryption, Decompression & Import ----
echo "[2/3] Decrypting, decompressing and streaming into target database '${TARGET_DB}'..."
START_TIME=$(date +%s)

openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 "${OPENSSL_PASS_ARG[@]}" -in "${BACKUP_FILE}" \
    | gzip -d \
    | ${CLIENT_CMD}

PIPE_STATUSES=("${PIPESTATUS[@]}")
for status in "${PIPE_STATUSES[@]}"; do
    if [[ "$status" -ne 0 ]]; then
        echo "[ERROR] Restore pipeline failed with status codes: ${PIPE_STATUSES[*]}" >&2
        exit 3
    fi
done

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo "[3/3] Database restore completed successfully in ${DURATION}s."
echo "Target: ${TARGET_DB} at ${TARGET_HOST}:${TARGET_PORT}"
