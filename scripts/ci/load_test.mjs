#!/usr/bin/env node
/**
 * ==============================================================================
 * Controlled Load Test for High-Traffic Read Endpoints
 * Japanese Learning Platform CI Pipeline
 *
 * Targets:
 *  - GET /actuator/health
 *  - GET /api/health
 *  - GET /api/v1/courses?page=0&size=10
 *
 * Enforces SLA Thresholds:
 *  - Error Rate: 0.00%
 *  - p95 Latency: <= 300ms
 *  - p99 Latency: <= 600ms
 *
 * Outputs:
 *  - reports/load-test-report.json
 *  - reports/load-test-summary.md
 * ==============================================================================
 */

import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { performance } from 'node:perf_hooks';

const BASE_URL = process.env.TARGET_URL || 'http://localhost:8080';
const CONCURRENCY = parseInt(process.env.LOAD_CONCURRENCY || '10', 10);
const TOTAL_REQUESTS = parseInt(process.env.LOAD_TOTAL_REQUESTS || '200', 10);
const OUTPUT_DIR = process.env.OUTPUT_DIR || 'reports';

// Practical SLA Thresholds
const SLA_MAX_P95_MS = parseFloat(process.env.SLA_MAX_P95_MS || '300');
const SLA_MAX_P99_MS = parseFloat(process.env.SLA_MAX_P99_MS || '600');
const SLA_MAX_ERROR_RATE_PCT = parseFloat(process.env.SLA_MAX_ERROR_RATE_PCT || '0.0');

const ENDPOINTS = [
  '/actuator/health',
  '/api/health',
  '/api/v1/courses?page=0&size=10',
];

console.log('==========================================================');
console.log(` Controlled Load Test — Japanese Learning Platform`);
console.log(` Target URL:       ${BASE_URL}`);
console.log(` Concurrency:      ${CONCURRENCY} virtual clients`);
console.log(` Total Requests:   ${TOTAL_REQUESTS}`);
console.log(` Endpoints:        ${ENDPOINTS.join(', ')}`);
console.log(` SLA Thresholds:   p95 <= ${SLA_MAX_P95_MS}ms, p99 <= ${SLA_MAX_P99_MS}ms, errors <= ${SLA_MAX_ERROR_RATE_PCT}%`);
console.log('==========================================================\n');

const latencies = [];
const statusCounts = {};
let failedRequests = 0;
let requestCounter = 0;

async function sendRequest(index) {
  const endpoint = ENDPOINTS[index % ENDPOINTS.length];
  const url = `${BASE_URL}${endpoint}`;
  const start = performance.now();
  let status = 0;
  let success = false;

  try {
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'User-Agent': 'CI-LoadTest-Runner/1.0',
      },
      signal: AbortSignal.timeout(10000),
    });
    status = res.status;
    await res.text();
    success = res.ok;
  } catch (err) {
    status = 0;
    success = false;
  }

  const durationMs = performance.now() - start;
  latencies.push(durationMs);
  statusCounts[status] = (statusCounts[status] || 0) + 1;

  if (!success) {
    failedRequests++;
  }
}

async function runWorker() {
  while (true) {
    const current = requestCounter++;
    if (current >= TOTAL_REQUESTS) {
      break;
    }
    await sendRequest(current);
  }
}

const overallStart = performance.now();
const workers = Array.from({ length: CONCURRENCY }, () => runWorker());
await Promise.all(workers);
const overallDurationMs = performance.now() - overallStart;

// Calculations
latencies.sort((a, b) => a - b);
const total = latencies.length;
const sum = latencies.reduce((acc, val) => acc + val, 0);
const avg = total > 0 ? sum / total : 0;
const min = total > 0 ? latencies[0] : 0;
const max = total > 0 ? latencies[total - 1] : 0;

function percentile(p) {
  if (total === 0) return 0;
  const index = Math.min(Math.floor((p / 100) * total), total - 1);
  return latencies[index];
}

const p50 = percentile(50);
const p90 = percentile(90);
const p95 = percentile(95);
const p99 = percentile(99);

const errorRatePct = total > 0 ? (failedRequests / total) * 100 : 100;
const throughputRps = overallDurationMs > 0 ? (total / (overallDurationMs / 1000)) : 0;

// Validate SLAs
const p95Passed = p95 <= SLA_MAX_P95_MS;
const p99Passed = p99 <= SLA_MAX_P99_MS;
const errorRatePassed = errorRatePct <= SLA_MAX_ERROR_RATE_PCT;
const testPassed = p95Passed && p99Passed && errorRatePassed;

console.log('--- Results Summary ---');
console.log(` Completed in:      ${overallDurationMs.toFixed(2)} ms`);
console.log(` Throughput:        ${throughputRps.toFixed(2)} req/s`);
console.log(` Successful:        ${total - failedRequests} / ${total}`);
console.log(` Failed:            ${failedRequests} (${errorRatePct.toFixed(2)}%)`);
console.log(` Status Breakdown:  ${JSON.stringify(statusCounts)}`);
console.log(` Min Latency:       ${min.toFixed(2)} ms`);
console.log(` Avg Latency:       ${avg.toFixed(2)} ms`);
console.log(` p50 Latency:       ${p50.toFixed(2)} ms`);
console.log(` p90 Latency:       ${p90.toFixed(2)} ms`);
console.log(` p95 Latency:       ${p95.toFixed(2)} ms (Threshold: <= ${SLA_MAX_P95_MS}ms) -> ${p95Passed ? 'PASS' : 'FAIL'}`);
console.log(` p99 Latency:       ${p99.toFixed(2)} ms (Threshold: <= ${SLA_MAX_P99_MS}ms) -> ${p99Passed ? 'PASS' : 'FAIL'}`);
console.log(` Max Latency:       ${max.toFixed(2)} ms`);
console.log(` Overall Result:    ${testPassed ? '✅ PASSED' : '❌ FAILED'}\n`);

// Ensure output directory exists
mkdirSync(OUTPUT_DIR, { recursive: true });

// Output JSON report
const reportData = {
  timestamp: new Date().toISOString(),
  targetUrl: BASE_URL,
  status: testPassed ? 'PASSED' : 'FAILED',
  config: {
    concurrency: CONCURRENCY,
    totalRequests: TOTAL_REQUESTS,
    endpoints: ENDPOINTS,
    sla: {
      maxP95Ms: SLA_MAX_P95_MS,
      maxP99Ms: SLA_MAX_P99_MS,
      maxErrorRatePct: SLA_MAX_ERROR_RATE_PCT,
    },
  },
  metrics: {
    durationMs: overallDurationMs,
    throughputRps,
    totalRequests: total,
    successfulRequests: total - failedRequests,
    failedRequests,
    errorRatePct,
    statusCodes: statusCounts,
    latenciesMs: {
      min,
      avg,
      p50,
      p90,
      p95,
      p99,
      max,
    },
  },
  slaCompliance: {
    p95Passed,
    p99Passed,
    errorRatePassed,
    allPassed: testPassed,
  },
};

const jsonPath = resolve(OUTPUT_DIR, 'load-test-report.json');
writeFileSync(jsonPath, JSON.stringify(reportData, null, 2), 'utf-8');
console.log(`Saved JSON report to ${jsonPath}`);

// Output Markdown summary
const mdSummary = `## Controlled Read Endpoint Load Test Summary

- **Timestamp:** \`${new Date().toISOString()}\`
- **Target:** \`${BASE_URL}\`
- **Concurrency:** \`${CONCURRENCY}\` clients
- **Total Requests:** \`${total}\`
- **Throughput:** \`${throughputRps.toFixed(2)} req/sec\`
- **Status:** **${testPassed ? '✅ PASSED' : '❌ FAILED'}**

### Latency Distribution

| Metric | Measured Value | SLA Threshold | Status |
| :--- | :--- | :--- | :--- |
| **p95 Latency** | \`${p95.toFixed(2)} ms\` | \`<= ${SLA_MAX_P95_MS} ms\` | ${p95Passed ? '✅ PASS' : '❌ FAIL'} |
| **p99 Latency** | \`${p99.toFixed(2)} ms\` | \`<= ${SLA_MAX_P99_MS} ms\` | ${p99Passed ? '✅ PASS' : '❌ FAIL'} |
| **Error Rate** | \`${errorRatePct.toFixed(2)}%\` | \`<= ${SLA_MAX_ERROR_RATE_PCT}%\` | ${errorRatePassed ? '✅ PASS' : '❌ FAIL'} |
| **Average** | \`${avg.toFixed(2)} ms\` | N/A | - |
| **p50** | \`${p50.toFixed(2)} ms\` | N/A | - |
| **Max** | \`${max.toFixed(2)} ms\` | N/A | - |

### Status Codes
\`\`\`json
${JSON.stringify(statusCounts, null, 2)}
\`\`\`
`;

const mdPath = resolve(OUTPUT_DIR, 'load-test-summary.md');
writeFileSync(mdPath, mdSummary, 'utf-8');
console.log(`Saved Markdown summary to ${mdPath}`);

if (!testPassed) {
  process.exit(1);
}
process.exit(0);
