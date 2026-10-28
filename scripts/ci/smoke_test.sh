#!/usr/bin/env bash
# ==============================================================================
# API Smoke Test Suite for Japanese Learning Platform CI
# Tests core operational, documentation, public, and error handling endpoints.
# Does NOT print secrets or tokens in logs.
# Outputs: reports/smoke-test-report.json
# ==============================================================================

set -euo pipefail

BASE_URL="${TARGET_URL:-http://localhost:8080}"
OUTPUT_DIR="${OUTPUT_DIR:-reports}"
REPORT_FILE="${OUTPUT_DIR}/smoke-test-report.json"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-60}"

mkdir -p "${OUTPUT_DIR}"

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
TEST_RESULTS="[]"

echo "=========================================================="
echo " Starting API Smoke Tests on: ${BASE_URL}"
echo "=========================================================="

# 1. Wait for server readiness
echo -n "Waiting for backend readiness at ${BASE_URL}/actuator/health/readiness ..."
WAIT_TIME=0
READY=false
while [ "$WAIT_TIME" -lt "$MAX_WAIT_SECONDS" ]; do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health/readiness" || true)
  if [ "$HTTP_STATUS" = "200" ]; then
    READY=true
    break
  fi
  sleep 2
  WAIT_TIME=$((WAIT_TIME + 2))
  echo -n "."
done
echo ""

if [ "$READY" = "false" ]; then
  echo "❌ Timed out waiting for backend to become ready after ${MAX_WAIT_SECONDS} seconds."
  cat <<EOF > "${REPORT_FILE}"
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "targetUrl": "${BASE_URL}",
  "status": "FAILED",
  "error": "Backend failed to become ready within ${MAX_WAIT_SECONDS}s",
  "summary": { "total": 1, "passed": 0, "failed": 1 },
  "results": []
}
EOF
  exit 1
fi

echo "✅ Backend is READY. Running smoke test assertions..."
echo ""

run_smoke_test() {
  local name="$1"
  local method="$2"
  local endpoint="$3"
  local expected_status="$4"
  local expected_keyword="$5"
  local post_body="${6:-}"

  TOTAL_TESTS=$((TOTAL_TESTS + 1))
  local url="${BASE_URL}${endpoint}"
  local response_file
  response_file=$(mktemp)
  local headers_file
  headers_file=$(mktemp)

  local http_code
  if [ "$method" = "POST" ]; then
    http_code=$(curl -s -S -X POST "${url}" \
      -H "Content-Type: application/json" \
      -d "${post_body}" \
      -D "${headers_file}" \
      -o "${response_file}" \
      -w "%{http_code}" || echo "000")
  else
    http_code=$(curl -s -S -X GET "${url}" \
      -D "${headers_file}" \
      -o "${response_file}" \
      -w "%{http_code}" || echo "000")
  fi

  local passed=true
  local failure_reason=""

  if [ "$http_code" != "$expected_status" ]; then
    passed=false
    failure_reason="Expected HTTP ${expected_status}, received ${http_code}"
  elif [ -n "$expected_keyword" ] && ! grep -q "$expected_keyword" "${response_file}"; then
    passed=false
    failure_reason="Response body did not contain required keyword: '${expected_keyword}'"
  fi

  # Check security header: X-Content-Type-Options
  if ! grep -qi "X-Content-Type-Options" "${headers_file}"; then
    echo "  [WARN] Missing X-Content-Type-Options header on ${endpoint}"
  fi

  local result_status
  if [ "$passed" = "true" ]; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
    result_status="PASSED"
    echo "  ✅ PASS: ${name} (${method} ${endpoint} -> HTTP ${http_code})"
  else
    FAILED_TESTS=$((FAILED_TESTS + 1))
    result_status="FAILED"
    echo "  ❌ FAIL: ${name} (${method} ${endpoint}) - ${failure_reason}"
  fi

  # Record to JSON array using jq
  local entry
  entry=$(jq -n \
    --arg name "$name" \
    --arg method "$method" \
    --arg endpoint "$endpoint" \
    --arg status "$result_status" \
    --arg expectedCode "$expected_status" \
    --arg actualCode "$http_code" \
    --arg reason "$failure_reason" \
    '{name: $name, method: $method, endpoint: $endpoint, status: $status, expectedHttpCode: $expectedCode, actualHttpCode: $actualCode, error: $reason}')

  TEST_RESULTS=$(echo "$TEST_RESULTS" | jq --argjson item "$entry" '. += [$item]')
  rm -f "${response_file}" "${headers_file}"
}

# --- Smoke Test Cases ---
run_smoke_test "Actuator Health Status" "GET" "/actuator/health" "200" '"status":"UP"'
run_smoke_test "Actuator Liveness Probe" "GET" "/actuator/health/liveness" "200" '"status":"UP"'
run_smoke_test "Actuator Readiness Probe" "GET" "/actuator/health/readiness" "200" '"status":"UP"'
run_smoke_test "Operational Health Endpoint" "GET" "/api/health" "200" '"service":"japanese-learning-api"'
run_smoke_test "OpenAPI Documentation JSON" "GET" "/api-docs" "200" '"openapi"'
run_smoke_test "Public Courses Read API" "GET" "/api/v1/courses?page=0&size=5" "200" '"success":true'
run_smoke_test "Auth Error Envelope (Invalid Credentials)" "POST" "/api/v1/auth/login" "401" '"success":false' '{"email":"smoke_test_unregistered@test.internal","password":"wrong_password_123"}'

echo ""
echo "=========================================================="
echo " Smoke Test Summary: ${PASSED_TESTS}/${TOTAL_TESTS} Passed, ${FAILED_TESTS} Failed"
echo "=========================================================="

# Write structured report
jq -n \
  --arg timestamp "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" \
  --arg targetUrl "$BASE_URL" \
  --arg status "$([ "$FAILED_TESTS" -eq 0 ] && echo "PASSED" || echo "FAILED")" \
  --argjson total "$TOTAL_TESTS" \
  --argjson passed "$PASSED_TESTS" \
  --argjson failed "$FAILED_TESTS" \
  --argjson results "$TEST_RESULTS" \
  '{
    timestamp: $timestamp,
    targetUrl: $targetUrl,
    status: $status,
    summary: { total: $total, passed: $passed, failed: $failed },
    results: $results
  }' > "${REPORT_FILE}"

echo "Structured report written to ${REPORT_FILE}"

if [ "$FAILED_TESTS" -gt 0 ]; then
  echo "❌ API smoke tests failed."
  exit 1
fi

echo "✅ All API smoke tests passed successfully."
exit 0
