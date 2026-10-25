#!/usr/bin/env bash
# ==============================================================================
# Automated Test Suite for Backup, Restore, and Isolated Verification Scripts
# Validates encryption, checksum integrity, tamper detection, and schema parsing
# WITHOUT requiring a live database or touching production data.
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_TMP_DIR="/tmp/test_backup_workflow_$$"

mkdir -p "${TEST_TMP_DIR}/backups"
chmod +x "${SCRIPT_DIR}"/*.sh

cleanup() {
    rm -rf "${TEST_TMP_DIR}"
}
trap cleanup EXIT

echo "=============================================================================="
echo "RUNNING BACKUP & RESTORE WORKFLOW UNIT & INTEGRATION TESTS"
echo "Workdir: ${TEST_TMP_DIR}"
echo "=============================================================================="

# ---- Step 1: Create Mock Production MariaDB Dump ----
echo "[Test 1/6] Generating sample MariaDB dump with Japanese learning schema..."
MOCK_SQL="${TEST_TMP_DIR}/mock_database.sql"
cat << 'EOF' > "${MOCK_SQL}"
-- MariaDB dump 10.19  Distrib 10.11.8-MariaDB, for debian-linux-gnu (x86_64)
-- Host: 127.0.0.1    Database: japanese_learning_db
-- ------------------------------------------------------
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;

DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `flyway_schema_history` VALUES (1,'1','init schema','SQL','V1__init_schema.sql',123456,'root',NOW(),15,1);

DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `course_sections`;
CREATE TABLE `course_sections` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `lessons`;
CREATE TABLE `lessons` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `quizzes`;
CREATE TABLE `quizzes` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `quiz_questions`;
CREATE TABLE `quiz_questions` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `quiz_answers`;
CREATE TABLE `quiz_answers` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS `refresh_tokens`;
CREATE TABLE `refresh_tokens` (`id` bigint(20) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `courses` VALUES (1,'Khóa học tiếng Nhật N5 - 日本語初級','Nền tảng Hiragana, Katakana và Kanji');

-- Dump completed on 2026-10-25 15:00:00
EOF

echo "  -> Mock database dump generated successfully ($(wc -l < "${MOCK_SQL}") lines)."

# ---- Step 2: Encrypt and Package Mock Backup ----
echo "[Test 2/6] Packaging and encrypting mock backup..."
TEST_KEY="TestSecretKeyForTestingWorkflow2026!"
BACKUP_FILE="${TEST_TMP_DIR}/backups/japanese_learning_db_daily_20261025_150000.sql.gz.enc"

gzip -9 -c "${MOCK_SQL}" \
    | openssl enc -aes-256-cbc -pbkdf2 -iter 100000 -salt -pass "pass:${TEST_KEY}" -out "${BACKUP_FILE}"

sha256sum "${BACKUP_FILE}" | awk '{print $1}' > "${BACKUP_FILE}.sha256"

if [[ ! -f "${BACKUP_FILE}" || ! -f "${BACKUP_FILE}.sha256" ]]; then
    echo "[FAIL] Backup artifact or checksum file was not created." >&2
    exit 1
fi
echo "  -> Encrypted artifact created: ${BACKUP_FILE} ($(stat -c%s "${BACKUP_FILE}") bytes)."

# ---- Step 3: Run Isolated Restore Verification on Valid Backup ----
echo "[Test 3/6] Running verify-restore.sh on authentic backup..."
BACKUP_ENCRYPTION_KEY="${TEST_KEY}" "${SCRIPT_DIR}/verify-restore.sh" "${BACKUP_FILE}"
echo "  -> Verification passed for authentic backup [OK]."

# ---- Step 4: Test Tamper Detection ----
echo "[Test 4/6] Testing Cryptographic Tamper Detection (Checksum Mismatch)..."
TAMPERED_FILE="${TEST_TMP_DIR}/backups/tampered.sql.gz.enc"
cp "${BACKUP_FILE}" "${TAMPERED_FILE}"
cp "${BACKUP_FILE}.sha256" "${TAMPERED_FILE}.sha256"

# Flip a byte in the encrypted file
printf '\xFF' | dd of="${TAMPERED_FILE}" bs=1 seek=100 count=1 conv=notrunc status=none

set +e
BACKUP_ENCRYPTION_KEY="${TEST_KEY}" "${SCRIPT_DIR}/verify-restore.sh" "${TAMPERED_FILE}" >/dev/null 2>&1
STATUS=$?
set -e

if [[ $STATUS -eq 0 ]]; then
    echo "[FAIL] Tampered file was unexpectedly accepted by verify-restore.sh!" >&2
    exit 1
else
    echo "  -> Tamper detection SUCCESS: Corrupted file was correctly rejected with exit code ${STATUS} [OK]."
fi

# ---- Step 5: Test Wrong Encryption Key Detection ----
echo "[Test 5/6] Testing Wrong Decryption Key Detection..."
set +e
BACKUP_ENCRYPTION_KEY="WrongPassphrase12345!" "${SCRIPT_DIR}/verify-restore.sh" "${BACKUP_FILE}" >/dev/null 2>&1
KEY_STATUS=$?
set -e

if [[ $KEY_STATUS -eq 0 ]]; then
    echo "[FAIL] Backup with wrong encryption key was unexpectedly accepted!" >&2
    exit 1
else
    echo "  -> Wrong key detection SUCCESS: Rejected with exit code ${KEY_STATUS} [OK]."
fi

# ---- Step 6: Test Restore Guard Without Flag ----
echo "[Test 6/6] Testing restore.sh destructive overwrite guard..."
set +e
"${SCRIPT_DIR}/restore.sh" "${BACKUP_FILE}" >/dev/null 2>&1
GUARD_STATUS=$?
set -e

if [[ $GUARD_STATUS -ne 1 ]]; then
    echo "[FAIL] restore.sh did not block execution when --confirm-destructive-overwrite was missing!" >&2
    exit 1
else
    echo "  -> Safety guard SUCCESS: restore.sh blocked unconfirmed restore with exit code 1 [OK]."
fi

echo "=============================================================================="
echo "ALL 6 BACKUP & RESTORE WORKFLOW TESTS PASSED CLEANLY!"
echo "=============================================================================="
exit 0
