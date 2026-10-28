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
echo -n "Waiting for backend readiness at ${BASE_URL} ..."
WAIT_TIME=0
READY=false
LAST_HTTP_STATUS="000"
while [ "$WAIT_TIME" -lt "$MAX_WAIT_SECONDS" ]; do
  # Check if backend PID file exists and process is still alive
  if [ -f "backend.pid" ]; then
    PID=$(cat backend.pid)
    if ! kill -0 "$PID" 2>/dev/null; then
      echo ""
      echo "❌ Backend process (PID ${PID}) has exited unexpectedly!"
      if [ -f "reports/backend-app.log" ]; then
        echo "=== Backend Application Crash Log (last 60 lines) ==="
        tail -n 60 reports/backend-app.log
      fi
      exit 1
    fi
  fi

  # Check readiness probe first, then standard health endpoint
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health/readiness" || true)
  LAST_HTTP_STATUS="${HTTP_STATUS}"
  if [ "$HTTP_STATUS" = "200" ]; then
    READY=true
    break
  fi

  # Fallback: if probes returned 404 but general health is 200, accept it
  if [ "$HTTP_STATUS" = "404" ]; then
    GEN_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" || true)
    if [ "$GEN_HEALTH" = "200" ]; then
      READY=true
      break
    fi
  fi

  sleep 2
  WAIT_TIME=$((WAIT_TIME + 2))
  echo -n "."
done
echo ""

if [ "$READY" = "false" ]; then
  echo "❌ Timed out waiting for backend to become ready after ${MAX_WAIT_SECONDS} seconds (Last HTTP code: ${LAST_HTTP_STATUS})."
  if [ -f "reports/backend-app.log" ]; then
    echo "=== Backend Application Log (last 60 lines) ==="
    tail -n 60 reports/backend-app.log
  fi
  cat <<EOF > "${REPORT_FILE}"
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "targetUrl": "${BASE_URL}",
  "status": "FAILED",
  "error": "Backend failed to become ready within ${MAX_WAIT_SECONDS}s (Last HTTP status: ${LAST_HTTP_STATUS})",
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
  elif [ -n "$expected_keyword" ] && ! grep -qE "$expected_keyword" "${response_file}"; then
    passed=false
    failure_reason="Response body did not match pattern '${expected_keyword}'. Snippet: $(head -c 160 "${response_file}")"
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
run_smoke_test "Actuator Health Status" "GET" "/actuator/health" "200" '"status":[[:space:]]*"UP"'
run_smoke_test "Actuator Liveness Probe" "GET" "/actuator/health/liveness" "200" '"status":[[:space:]]*"UP"'
run_smoke_test "Actuator Readiness Probe" "GET" "/actuator/health/readiness" "200" '"status":[[:space:]]*"UP"'
run_smoke_test "Operational Health Endpoint" "GET" "/api/health" "200" '"service":[[:space:]]*"japanese-learning-api"'
run_smoke_test "OpenAPI Documentation JSON" "GET" "/api-docs" "200" '"openapi"'
run_smoke_test "Public Courses Read API" "GET" "/api/v1/courses?page=0&size=5" "200" '"code":[[:space:]]*1000'
run_smoke_test "Auth Error Envelope (Invalid Credentials)" "POST" "/api/v1/auth/login" "401" '"code":[[:space:]]*2002' '{"email":"smoke_test_unregistered@test.internal","password":"wrong_password_123"}'

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
