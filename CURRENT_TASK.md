# CURRENT TASK

## Frontend Paid Course Checkout & Enrollment UX

**Status:** COMPLETED

**Priority:** Post-MVP Milestone 2 - Paid Course Checkout

**Suggested branch:** `feature/355-Frontend-Paid-Course-Checkout-Enrollment-UX`

---

## Goal

Give students a clear, complete demo checkout journey for paid courses: discover a course, create or resume a pending order, review its server-authoritative price, simulate payment in the local environment, and arrive at the enrolled learning flow without confusing or duplicate actions.

---

## Scope

### 1. Order and payment client contracts

- Add focused frontend services for `GET/POST /api/v1/orders` and `POST /api/v1/payments/simulate/{orderId}` using existing Axios conventions.
- Keep API DTO mapping explicit; never calculate or send course prices from the client.
- Handle standard API errors consistently, including `ORDER_DUPLICATE_PENDING`, ownership/access errors, and disabled payment simulation.

### 2. Paid-course CTA flow

- Update the course detail CTA so a paid course displays a clear purchase action instead of the free enrollment action.
- On purchase, create a pending order and navigate to checkout.
- When the backend returns `ORDER_DUPLICATE_PENDING`, retrieve the student's orders, find the pending order for that course, and continue to its checkout page rather than leaving the user at a dead end.
- Preserve existing behavior for guest users, free courses, and already-enrolled students.

### 3. Checkout page

- Add a protected student route such as `/checkout/:orderId`.
- Display only order data returned by the backend: course title, order code, snapshot price, order status, and payment status/result.
- Provide a clearly labelled local/demo payment action only when the order is `PENDING`.
- Prevent repeat clicks while the request is in flight; after success, route the student into the purchased course's learning flow.
- Render useful loading, empty, error, unauthorized, and already-paid states without exposing another user's order.

### 4. Navigation, UX and tests

- Add a discoverable entry to a student's pending orders or checkout journey only where it fits the existing navigation pattern; avoid cluttering public navigation.
- Keep responsive desktop/mobile behavior and existing visual language intact.
- Add targeted unit tests for services and key checkout/CTA states, then run the frontend test suite and production build.

---

## Out of Scope

- Real VNPay, MoMo, Stripe, payment redirects, webhooks, refunds, invoices, coupons, or payment history management.
- Changing backend payment/order business rules except for a verified contract defect that blocks this frontend flow.
- Admin payment tools.

---

## Acceptance Criteria

- [x] A student can start checkout from a paid course detail page.
- [x] The UI uses backend order snapshots and never trusts client-calculated prices.
- [x] A duplicate pending order leads the student to its existing checkout instead of an unhelpful error.
- [x] The checkout route is protected and safely handles missing, foreign, pending, and paid orders.
- [x] The simulation action is disabled while pending and unavailable after payment success.
- [x] Successful payment leads to the purchased course learning flow.
- [x] Free-course and already-enrolled course flows do not regress.
- [x] Frontend tests and production build pass.

---

## Backend Contract Available

- `POST /api/v1/orders` creates a student-owned `PENDING` order for a paid course.
- `GET /api/v1/orders/me` lists the current student's orders.
- `GET /api/v1/orders/{id}` returns only the owner's order and item snapshots.
- `POST /api/v1/payments/simulate/{orderId}` completes a local/demo payment when simulation is enabled.
- Payment simulation is disabled by default in the production profile; do not portray it as a real payment method.

---

## Verification Commands

```bash
cd frontend
npm run test
npm run build
```
