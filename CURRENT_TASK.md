# CURRENT TASK

## Backend Payment Simulation & Paid Enrollment Completion

**Status:** TODO

**Priority:** Post-MVP Milestone 2 - Paid Course Checkout
**Suggested branch:** `feature/350-Backend-Payment-Simulation-Paid-Enrollment`

---

## Goal

Complete the paid-course checkout flow after a student has created a `PENDING` order. A safe local payment simulation must record a successful payment, mark the order as paid, and grant the student access to the course exactly once.

This task turns the existing order foundation into a demo-ready paid enrollment flow. It is not an integration with a real payment provider.

---

## Scope

### 1. Payment persistence

- Add a Flyway migration after `V7` for payment records associated with an order.
- Add the payment entity, status enum, repository, DTOs, and response mapping following existing module conventions.
- Persist an immutable payment amount and a locally generated transaction reference suitable for the simulation.

### 2. Payment simulation endpoint

- Add an authenticated student endpoint that simulates a successful payment for the student's own `PENDING` order.
- Keep the simulation explicitly local/demo-only through a configuration guard; it must not be presented as a real payment gateway integration.
- Reject payment attempts for another student's order or an invalid order state with project-standard error codes.

### 3. Transactional paid enrollment completion

- In one transaction, record the successful payment, change the order to `PAID`, create or reactivate the paid course enrollment as `ACTIVE`, and increase `Course.totalStudents` exactly once.
- Make a repeated request safe: it must not create duplicate payment/enrollment records or increment the course count again.
- Protect against concurrent completion of the same order using the repository/database strategy that best matches the current codebase.

### 4. Tests and documentation

- Add focused service tests for ownership, invalid states, successful completion, retry/idempotency, and duplicate/concurrent-side-effect protection.
- Add controller integration coverage for the endpoint and its authorization boundary.
- Run the relevant backend test suite with the project's required Java version and update learning documents with the key transactional and idempotency decisions.

---

## Out of Scope

- Real VNPay, MoMo, Stripe, or other payment-provider integration.
- Provider signatures, webhook verification, redirect pages, refunds, cancellations, coupons, invoices, or reconciliation jobs.
- A student-facing payment UI. That belongs to the subsequent frontend checkout task after this backend contract is stable.
- Administrative payment management.

---

## Acceptance Criteria

- [ ] A pending paid-course order can be completed through the guarded local simulation endpoint by its owner.
- [ ] Completion creates one successful payment record, marks the order `PAID`, and gives the student an `ACTIVE` enrollment.
- [ ] The course enrollment counter changes once only.
- [ ] Retrying the same completion request is idempotent and produces no duplicate side effects.
- [ ] Another student cannot inspect or complete the order.
- [ ] Invalid order states return consistent domain errors.
- [ ] Migration works for both H2 tests and the configured MariaDB environment.
- [ ] Service and controller tests pass under the required JDK.

---

## Context From the Previous Task

`Backend Paid Course Order Foundation` is complete in commit `69df95c`.

- `orders` and `order_items` were introduced in Flyway `V7`.
- Students can create a `PENDING` order for a published paid course and view only their own orders.
- Order items snapshot course title and effective price so later course changes do not rewrite purchase history.
- The order module deliberately deferred payment persistence, payment completion, and paid enrollment activation to this task.

---

## Verification Command

```bash
cd backend
JAVA_HOME=/path/to/jdk-21 mvn clean verify
```
