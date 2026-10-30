# CURRENT TASK

## Frontend Student Order History & Purchase Recovery

**Status:** DONE

**Priority:** Post-MVP Milestone 2 - Paid Course Checkout

**Suggested branch:** `feature/358-Frontend-Student-Order-History-Purchase-Recovery`

---

## Goal

Give students one reliable place to find every course order they own, understand its status, resume a pending demo checkout, and return to learning after payment. The experience must use the existing server-owned order contract and must not expose payment or order data belonging to another user.

---

## Scope

### 1. Student order history page

- Add a protected student route such as `/student/orders`.
- Load data only through `GET /api/v1/orders/me` and render responsive order cards or a table following the existing student visual language.
- Show order code, creation time, item title(s), server snapshot amount, and a clear localized status label.
- Include purposeful loading, empty, retry, and API-error states.

### 2. Status-aware recovery actions

- `PENDING`: provide a clear action to resume `/checkout/:orderId`.
- `PAID`: provide an action to open the relevant enrolled course or `My Courses` fallback.
- `CANCELLED`, `FAILED`, `EXPIRED`, and `REFUNDED`: show a non-actionable explanation appropriate to the existing backend statuses; do not add unsupported cancellation/refund APIs.
- Do not make client-side price calculations or infer ownership from route data.

### 3. Navigation and purchase continuity

- Add a discoverable, role-safe entry for student orders in the existing desktop/mobile navigation or student account menu without adding clutter for guests or admins.
- Refine the existing pending-order banner in `MyCoursesPage` so it points consistently to the order-history/checkout recovery flow.
- Preserve all free-course enrollment, existing checkout, and learning navigation behavior.

### 4. Quality

- Add focused service/page tests for status rendering and recovery navigation.
- Run the frontend test suite and production build.
- Update task and learning documentation with the final contract and test results.

---

## Out of Scope

- Real payment providers, webhooks, refunds, cancellation endpoints, invoices, coupons, and admin order management.
- New backend order APIs unless a verified frontend blocker exists.
- Changing order/payment business rules.

---

## Acceptance Criteria

- [x] A student can open a protected order-history page and see only their own orders.
- [x] Each order displays backend snapshot details and a meaningful status.
- [x] A pending order resumes the existing checkout page.
- [x] A paid order offers a safe route back into learning or My Courses.
- [x] Terminal statuses do not show unsupported payment actions.
- [x] Student navigation exposes the page without exposing it to guests or admins.
- [x] Existing checkout and free enrollment flows remain intact.
- [x] Frontend tests and production build pass.

---

## Existing Contract

- `GET /api/v1/orders/me` returns only the authenticated student's orders.
- `GET /api/v1/orders/{id}` enforces ownership and supplies checkout details.
- `POST /api/v1/payments/simulate/{orderId}` is available only for a pending order when demo simulation is enabled.
- Backend owns price snapshots, order state transitions, enrollment activation, and access control.

---

## Verification Commands

```bash
cd frontend
npm run test
npm run build
```
