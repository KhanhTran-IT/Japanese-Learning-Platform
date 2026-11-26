#!/usr/bin/env bash
# ==============================================================================
# Unit & Integration Tests for scripts/ci/validate_action_pins.sh
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VALIDATOR="${SCRIPT_DIR}/validate_action_pins.sh"
TEMP_DIR=$(mktemp -d)

cleanup() {
    rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

echo "=== Running Action Pin Validator Tests ==="
TOTAL_TESTS=0
PASSED_TESTS=0

run_test() {
    local test_name="$1"
    local expected_status="$2"
    local workflow_content="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    local test_file="${TEMP_DIR}/test_${TOTAL_TESTS}.yml"
    echo "$workflow_content" > "$test_file"
    
    set +e
    output=$("$VALIDATOR" "$test_file" 2>&1)
    status=$?
    set -e
    
    if [ "$status" -eq "$expected_status" ]; then
        echo "  [PASS] Test ${TOTAL_TESTS}: ${test_name}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo "  [FAIL] Test ${TOTAL_TESTS}: ${test_name}"
        echo "         Expected status: ${expected_status}, got: ${status}"
        echo "         Output:"
        echo "$output"
        return 1
    fi
}

# Test 1: Pinned 40-char SHA with comment passes
run_test "Pinned 40-char SHA with readable version comment passes" 0 "
name: Test Workflow
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683 # v4.2.2
"

# Test 2: Local action passes
run_test "Local repository-relative action passes" 0 "
name: Test Workflow
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: ./my-local-action
"

# Test 3: Mutable tag fails
run_test "Mutable tag reference @v4 is rejected" 1 "
name: Test Workflow
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
"

# Test 4: Mutable branch fails
run_test "Mutable branch reference @master is rejected" 1 "
name: Test Workflow
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: aquasecurity/trivy-action@master
"

# Test 5: Short SHA fails
run_test "Short commit SHA (7 chars) is rejected" 1 "
name: Test Workflow
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@11bd719 # short SHA
"

# Test 6: Missing ref separator fails
run_test "Missing '@' separator is rejected" 1 "
name: Test Workflow
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout
"

echo "------------------------------------------------------------------------------"
echo "Validator Test Results: ${PASSED_TESTS}/${TOTAL_TESTS} tests passed."

if [ "$PASSED_TESTS" -ne "$TOTAL_TESTS" ]; then
    echo "ERROR: Some tests failed." >&2
    exit 1
fi

echo "All Action Pin Validator tests passed successfully!"
exit 0
