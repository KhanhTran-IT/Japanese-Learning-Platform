#!/usr/bin/env bash
# ==============================================================================
# GitHub Actions Supply Chain & Action Pinning Validator
#
# Audits all GitHub Actions workflows (*.yml, *.yaml) to ensure every
# third-party action is pinned to an immutable 40-character commit SHA.
#
# Rejects mutable references (branches like @main/@master, tags like @v4/@v1.2).
# Ensures supply chain integrity against tag tampering and malicious repointing.
# ==============================================================================
set -euo pipefail

WORKFLOWS_DIR="${1:-.github/workflows}"

if [ ! -d "$WORKFLOWS_DIR" ] && [ ! -f "$WORKFLOWS_DIR" ]; then
    echo "ERROR: Target directory or file '${WORKFLOWS_DIR}' not found." >&2
    exit 1
fi

echo "=============================================================================="
echo "AUDITING GITHUB ACTIONS SUPPLY CHAIN INTEGRITY"
echo "Target: ${WORKFLOWS_DIR}"
echo "=============================================================================="

ERRORS=0
TOTAL_ACTIONS=0
PINNED_ACTIONS=0

# Regex for full 40-character hexadecimal SHA
SHA_REGEX='^[0-9a-fA-F]{40}$'
COMMENT_REGEX='#[[:space:]]*v?[0-9a-zA-Z._-]+'

# Find all yaml/yml files
if [ -f "$WORKFLOWS_DIR" ]; then
    WORKFLOW_FILES=("$WORKFLOWS_DIR")
else
    mapfile -t WORKFLOW_FILES < <(find "$WORKFLOWS_DIR" -type f \( -name "*.yml" -o -name "*.yaml" \) | sort)
fi

if [ ${#WORKFLOW_FILES[@]} -eq 0 ]; then
    echo "WARNING: No workflow files found under ${WORKFLOWS_DIR}."
    exit 0
fi

for file in "${WORKFLOW_FILES[@]}"; do
    echo "Scanning: ${file}..."
    line_number=0
    while IFS= read -r line || [ -n "$line" ]; do
        line_number=$((line_number + 1))
        
        # Check if line contains a 'uses:' directive (ignoring comments)
        if [[ "$line" =~ ^[[:space:]]*(-[[:space:]]+)?uses:[[:space:]]*([^[:space:]#]+)(.*)$ ]]; then
            action_ref="${BASH_REMATCH[2]}"
            comment="${BASH_REMATCH[3]}"
            
            # Strip quotes if any
            action_ref="${action_ref%\"}"
            action_ref="${action_ref#\"}"
            action_ref="${action_ref%\'}"
            action_ref="${action_ref#\'}"
            
            TOTAL_ACTIONS=$((TOTAL_ACTIONS + 1))
            
            # Local action check (e.g. ./ or ./.github/...)
            if [[ "$action_ref" =~ ^\./ ]]; then
                echo "  [OK] Line ${line_number}: Local action '${action_ref}' (safe, repository-internal)"
                PINNED_ACTIONS=$((PINNED_ACTIONS + 1))
                continue
            fi
            
            # Remote action must have '@'
            if [[ "$action_ref" != *@* ]]; then
                echo "  [FAIL] Line ${line_number}: Invalid action format '${action_ref}' (missing '@' separator)" >&2
                ERRORS=$((ERRORS + 1))
                continue
            fi
            
            action_name="${action_ref%%@*}"
            target_ref="${action_ref#*@}"
            
            # Check if target_ref is a full 40-character hex commit SHA
            if [[ "$target_ref" =~ $SHA_REGEX ]]; then
                # Check for readable version comment
                if [[ "$comment" =~ $COMMENT_REGEX ]]; then
                    version_hint="${BASH_REMATCH[0]}"
                    echo "  [OK] Line ${line_number}: ${action_name}@${target_ref:0:7}... ${version_hint}"
                else
                    echo "  [WARN] Line ${line_number}: ${action_name}@${target_ref:0:7}... (Recommended: add readable version comment like '# vX.Y.Z')"
                fi
                PINNED_ACTIONS=$((PINNED_ACTIONS + 1))
            else
                echo "  [FAIL] Line ${line_number}: Insecure mutable reference in '${action_name}'" >&2
                echo "         Found:   @${target_ref}" >&2
                echo "         Reason:  Mutable references (branch or tag) can be repointed or compromised." >&2
                echo "         Fix:     Pin to a full 40-character commit SHA, e.g. '${action_name}@<full-40-char-sha> # v...'" >&2
                ERRORS=$((ERRORS + 1))
            fi
        fi
    done < "$file"
done

echo "------------------------------------------------------------------------------"
echo "Supply Chain Audit Summary: Total actions: ${TOTAL_ACTIONS} | Pinned: ${PINNED_ACTIONS} | Violations: ${ERRORS}"

if [ "$ERRORS" -gt 0 ]; then
    echo "FAILED: Found ${ERRORS} mutable or insecure action reference(s)." >&2
    exit 1
fi

echo "PASSED: All actions are securely pinned to full commit SHAs."
exit 0
