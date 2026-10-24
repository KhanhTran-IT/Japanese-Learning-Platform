# Production Monitoring, Alerts & Dashboards

> **Last updated:** 2026-10-24 — Production Observability feature (Task #367)

This document describes the monitoring infrastructure, custom metrics, recommended alert rules,
and Grafana dashboard layout for the Japanese Learning Platform backend.

---

## Table of Contents

- [1. Architecture Overview](#1-architecture-overview)
- [2. Custom Business Metrics](#2-custom-business-metrics)
- [3. Health Probes](#3-health-probes)
- [4. Structured Logging](#4-structured-logging)
- [5. Recommended Prometheus Alert Rules](#5-recommended-prometheus-alert-rules)
- [6. Grafana Dashboard Layout](#6-grafana-dashboard-layout)
- [7. Runbooks](#7-runbooks)
- [8. Sensitive Data Redaction](#8-sensitive-data-redaction)

---

## 1. Architecture Overview

```
┌────────────┐    scrape     ┌────────────────┐    query    ┌──────────┐
│ Prometheus │ ◄─────────── │  Spring Boot   │            │  Grafana │
│            │ /actuator/   │  Actuator +    │            │          │
│            │ prometheus   │  Micrometer    │            │          │
└─────┬──────┘              └────────────────┘            └────┬─────┘
      │                           │                            │
      │  alert rules              │ JSON logs                  │ dashboards
      ▼                           ▼                            │
┌──────────────┐          ┌──────────────┐                     │
│ Alertmanager │          │  ELK / Loki  │◄────────────────────┘
│  → Slack     │          │  CloudWatch  │   log queries
│  → Email     │          └──────────────┘
└──────────────┘
```

**Key endpoints:**

| Endpoint | Purpose | Auth |
|---|---|---|
| `GET /actuator/health` | Overall status (minimal: `{"status":"UP"}`) | Public |
| `GET /actuator/health/liveness` | Kubernetes liveness probe | Public |
| `GET /actuator/health/readiness` | Kubernetes readiness probe (DB + Redis) | Public |
| `GET /actuator/prometheus` | Prometheus metrics scraping | Public* |
| `GET /actuator/info` | Basic app info | Public |
| `GET /actuator/metrics` | Micrometer metrics browser | Public* |

> \* Restrict `/actuator/prometheus` and `/actuator/metrics` via network policy in production
> (e.g., only allow scraping from Prometheus pod IP range).

---

## 2. Custom Business Metrics

All custom metrics use the `app.` prefix and are registered via `ObservabilityMetrics.java`.

### Authentication Failures

| Metric | Type | Tags | Description |
|---|---|---|---|
| `app_auth_failures_total` | Counter | `reason=invalid_credentials` | Wrong email/password |
| `app_auth_failures_total` | Counter | `reason=account_locked` | Login to locked account |
| `app_auth_failures_total` | Counter | `reason=expired_token` | Refresh token expired in DB |
| `app_auth_failures_total` | Counter | `reason=invalid_token` | JWT signature/validation failed |

### Rate Limiting

| Metric | Type | Tags | Description |
|---|---|---|---|
| `app_rate_limit_exceeded_total` | Counter | `type=login` | Login rate limit triggered |
| `app_rate_limit_exceeded_total` | Counter | `type=refresh` | Token refresh rate limit triggered |

### Quiz Submissions

| Metric | Type | Tags | Description |
|---|---|---|---|
| `app_quiz_submissions_total` | Counter | `quiz_id`, `passed=true\|false` | Quiz submission with outcome |

### Order Processing

| Metric | Type | Tags | Description |
|---|---|---|---|
| `app_order_processing_total` | Counter | `status=PAID\|CANCELLED\|FAILED`, `payment_method` | Order state transitions |

### Built-in Actuator Metrics (auto-provided)

| Metric | Description |
|---|---|
| `http_server_requests_seconds` | HTTP request duration (by method, URI, status) |
| `jvm_memory_used_bytes` | JVM memory usage |
| `jvm_threads_live_threads` | Active thread count |
| `hikaricp_connections_active` | Active DB connection pool connections |
| `hikaricp_connections_idle` | Idle DB connections |
| `process_cpu_usage` | CPU usage |

---

## 3. Health Probes

### Liveness Probe (`/actuator/health/liveness`)

- **Purpose:** Is the JVM alive and not deadlocked?
- **Includes:** `livenessState` only
- **Kubernetes config:**
  ```yaml
  livenessProbe:
    httpGet:
      path: /actuator/health/liveness
      port: 8080
    initialDelaySeconds: 60
    periodSeconds: 30
    failureThreshold: 3
  ```

### Readiness Probe (`/actuator/health/readiness`)

- **Purpose:** Can the app serve traffic? Are dependencies (DB, Redis) healthy?
- **Includes:** `readinessState`, `db`, `redisCustomHealthIndicator`
- **Kubernetes config:**
  ```yaml
  readinessProbe:
    httpGet:
      path: /actuator/health/readiness
      port: 8080
    initialDelaySeconds: 30
    periodSeconds: 15
    failureThreshold: 3
  ```

### Docker Compose Healthchecks

All services in `docker-compose.prod.yml` have healthchecks:
- **MariaDB:** `healthcheck.sh --connect --innodb_initialized`
- **Redis:** `redis-cli -a $PASSWORD ping | grep PONG`
- **Backend:** `curl -f http://localhost:8080/actuator/health/readiness`

Backend `depends_on` uses `condition: service_healthy` to ensure dependencies are ready.

---

## 4. Structured Logging

### Dev Profile (Human-readable)
```
2026-10-24 14:30:15.123 [http-nio-8080-exec-1] [abc-123-def-456] INFO  AuthServiceImpl - User logged in: user@test.com
```

### Prod Profile (JSON — Logstash Encoder)
```json
{
  "@timestamp": "2026-10-24T14:30:15.123+07:00",
  "level": "INFO",
  "logger_name": "com.japaneselearning.module_auth.service.AuthServiceImpl",
  "message": "User logged in: user@test.com",
  "correlationId": "abc-123-def-456",
  "requestMethod": "POST",
  "requestUri": "/api/auth/login",
  "clientIp": "10.0.0.1",
  "service": "japanese-learning-api",
  "environment": "production"
}
```

### Correlation ID

Every request gets a correlation ID (from `X-Correlation-ID` header or auto-generated UUID).
- Stored in SLF4J MDC under key `correlationId`
- Returned in response header `X-Correlation-ID`
- Included in all log lines for request tracing

---

## 5. Recommended Prometheus Alert Rules

```yaml
groups:
  - name: japanese-learning-alerts
    rules:

      # ---- Authentication ----
      - alert: HighAuthFailureRate
        expr: rate(app_auth_failures_total[5m]) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High authentication failure rate"
          description: "More than 0.5 auth failures/sec for 5 minutes. Possible brute-force attack."

      - alert: AccountLockSpike
        expr: increase(app_auth_failures_total{reason="account_locked"}[15m]) > 10
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Multiple locked account login attempts"
          description: "{{ $value }} locked-account login attempts in 15 minutes."

      # ---- Rate Limiting ----
      - alert: RateLimitingSpike
        expr: rate(app_rate_limit_exceeded_total[5m]) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High rate limiting trigger rate"
          description: "Rate limiting is triggering more than 1/sec. Check for DDoS or misconfiguration."

      # ---- Infrastructure ----
      - alert: DatabaseDown
        expr: up{job="japanese-learning-api"} == 1 and spring_data_repository_invocations_seconds_count == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database appears unreachable"

      - alert: RedisDown
        expr: management_health_readiness_status != 1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Readiness check failing (DB or Redis down)"

      # ---- HTTP Errors ----
      - alert: High5xxRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "More than 5% of requests returning 5xx errors"

      - alert: SlowResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "P95 response time exceeds 2 seconds"

      # ---- JVM ----
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM heap memory usage above 90%"

      - alert: HighThreadCount
        expr: jvm_threads_live_threads > 500
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Active thread count exceeds 500"
```

---

## 6. Grafana Dashboard Layout

### Row 1: System Overview
| Panel | Type | Query |
|---|---|---|
| App Status | Stat | `up{job="japanese-learning-api"}` |
| Request Rate | Graph | `rate(http_server_requests_seconds_count[5m])` |
| Error Rate (5xx) | Graph | `rate(http_server_requests_seconds_count{status=~"5.."}[5m])` |
| P95 Latency | Graph | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))` |

### Row 2: Authentication & Security
| Panel | Type | Query |
|---|---|---|
| Auth Failures | Graph | `rate(app_auth_failures_total[5m])` by `reason` |
| Rate Limit Triggers | Graph | `rate(app_rate_limit_exceeded_total[5m])` by `type` |
| Login Success Rate | Gauge | `1 - (rate(app_auth_failures_total{reason="invalid_credentials"}[5m]) / rate(http_server_requests_seconds_count{uri="/api/auth/login"}[5m]))` |

### Row 3: Business Metrics
| Panel | Type | Query |
|---|---|---|
| Quiz Submissions | Graph | `rate(app_quiz_submissions_total[5m])` by `passed` |
| Quiz Pass Rate | Gauge | `sum(app_quiz_submissions_total{passed="true"}) / sum(app_quiz_submissions_total)` |
| Orders Processed | Graph | `rate(app_order_processing_total[5m])` by `status` |

### Row 4: Infrastructure
| Panel | Type | Query |
|---|---|---|
| JVM Heap Usage | Graph | `jvm_memory_used_bytes{area="heap"}` |
| DB Connection Pool | Graph | `hikaricp_connections_active` / `hikaricp_connections_idle` |
| Active Threads | Graph | `jvm_threads_live_threads` |
| CPU Usage | Graph | `process_cpu_usage` |

---

## 7. Runbooks

### High Auth Failure Rate
1. Check if failures are from a single IP → possible brute-force
2. Check `reason` tag distribution (invalid_credentials vs account_locked)
3. If concentrated on one IP, consider IP ban or WAF rule
4. If widespread, check if password reset is needed

### Redis Down
1. Check `docker-compose logs redis` or Kubernetes Redis pod logs
2. Verify Redis password and connection settings
3. Check Redis memory usage (`redis-cli INFO memory`)
4. Rate limiter will fall back based on `redis-failure-policy` setting

### Database Connection Pool Exhaustion
1. Check `hikaricp_connections_active` vs `hikaricp_connections_max`
2. Look for long-running transactions in MariaDB: `SHOW PROCESSLIST`
3. Check for connection leaks (connections not returned to pool)
4. Consider increasing `spring.datasource.hikari.maximum-pool-size`

### High 5xx Rate
1. Check application logs filtered by `correlationId` for failing requests
2. Look for common patterns (specific endpoint, specific error type)
3. Check DB and Redis health via readiness probe
4. Review recent deployments for regressions

---

## 8. Sensitive Data Redaction

All log output (both human-readable and JSON) passes through `LogMaskingConverter` which redacts:

| Pattern | Example Input | Redacted Output |
|---|---|---|
| JSON passwords | `"password":"secret"` | `"password":"***REDACTED***"` |
| Form passwords | `password=secret&` | `password=***REDACTED***&` |
| Bearer tokens | `Bearer eyJhbG...` | `Bearer ***REDACTED***` |
| JSON tokens | `"refreshToken":"abc"` | `"refreshToken":"***REDACTED***"` |
| Cookie values | `refreshToken=abc;` | `refreshToken=***REDACTED***;` |
| Cookie headers | `Cookie: token=abc` | `Cookie: ***REDACTED***` |

**Fields masked:** `password`, `currentPassword`, `newPassword`, `confirmPassword`, `passwordHash`,
`refreshToken`, `token`, `accessToken`, `tokenHash`, `JSESSIONID`, `SESSION`.

> ⚠️ **Important:** The production health endpoint (`/actuator/health`) returns only
> `{"status":"UP"}` with `show-details: never` to prevent information disclosure.
