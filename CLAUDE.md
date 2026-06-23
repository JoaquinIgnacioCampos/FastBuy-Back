# FastBuy Backend — Claude Instructions

## Project Overview
Spring Boot backend for FastBuy. Java 17, Maven wrapper (`mvnw`). Started via `start-fastbuy.ps1` in the frontend directory.

- GitHub: https://github.com/JoaquinIgnacioCampos/FastBuy-Back
- Branching: `main` (stable) + `develop` (daily work). Feature branches off `develop`.

## Behavioral Instructions

<!-- New instructions are appended here as they are given across sessions. -->

### 2026-05-21
- Log every behavioral change given by the user in both `FastBuy-Front/CLAUDE.md` and `FastBuy-Back/CLAUDE.md` to track progress across sessions.
- ngrok free static domains are randomly assigned. Custom subdomains require a paid plan. Do not assume a custom ngrok domain is available without the user confirming it's reserved.

### 2026-05-27
- `start-fastbuy-back.ps1` (in the frontend repo) uses a single ngrok tunnel on port 5173. The free ngrok plan supports only 1 tunnel per agent; a separate backend tunnel is not possible.
- Backend API calls from external devices reach Spring Boot via a **Vite dev-server proxy**: Vite proxies `/api/*` -> `http://localhost:8080/*`. The frontend sets `VITE_API_URL=/api`, so requests are always relative and route through the proxy.
- Both start scripts use an inline **Ctrl+C stop** pattern: pressing Ctrl+C in the launcher terminal kills all services (Spring Boot, Vite, ngrok) via a `try/finally` block. `stop-fastbuy.ps1` has been deleted — it is no longer needed.
- `start-fastbuy-back.ps1` kills any stale process on ports 5173/8080 before launching, to prevent leftover Vite instances from occupying port 5173 in demo mode.
- **No CORS configuration needed** — the browser never calls Spring Boot directly (all requests go through the Vite proxy), so no `@CrossOrigin` or `WebMvcConfigurer` changes are required.

### 2026-06-02 — Schema, assignment, and spike notes
- `events` table gained `starts_at TIMESTAMP NOT NULL` and `ends_at TIMESTAMP NOT NULL`. `GET /events` filters out events whose `endsAt < NOW - 6h` and orders by `startsAt ASC`. Seed `data.sql` uses H2 `DATEADD(...CURRENT_TIMESTAMP)` so tests don't drift with the calendar.
- `products` table gained `image VARCHAR(255)`, `emoji VARCHAR(16)`, `subtitle VARCHAR(255)`. `image` can be either a URL (rendered as `<img>` on the frontend) or an emoji string; `emoji` is a fallback when the image fails or is empty.
- New `bar_products` join table (bar_id, product_id) carries the many-to-many association between bars and the products they serve. `GET /bars/{barId}/menu` returns the per-bar subset via a native query in `ProductsRepository.findByBarId`. `GET /products` continues to return the union.
- `POST /orders` now accepts an **optional** `bar` field. When omitted, `OrdersService.pickLeastLoadedBar` finds the bar with the fewest in-flight beverages (sum of `q` across QUEUE+PREPARING orders) that carries every requested product. If no bar can serve all items → 422 via `IllegalStateException` (mapped by `GlobalExceptionHandler`). The assigned bar id is in the response.
- `POST /orders/{id}/advance` continues to step the order exactly one state: QUEUE → PREPARING → READY. The frontend bartender screen now exposes two distinct actions ("Comenzar a preparar" and "Marcar listo") in separate sub-sections, both calling this endpoint.
- Seed orders in `data.sql` expanded from 3 to 8 rows distributed across north/center/south so all three bars have queue+preparing+ready coverage. `OrdersControllerTest` uses a `SEED_STATES` map (IDs 1–8) in its `@AfterEach` cleanup to restore the table — extend that map if new seed orders are added.
- Mercado Pago real-charge spike research is in `mercado-pago-spike.md` (gitignored from the merge — delete before merging the spike PR). Checkout Pro via `POST /payments/preference` is the lowest-effort path; needs `MP_ACCESS_TOKEN` env var. Sandbox test card: `5031 7557 3453 0604`, CVV `123`, any future expiry.

### 2026-06-03 — Pre-release cleanup: event-scoped bars, bartender login, delivered tab, MP account linking

- **Schema changes:** `bars` table gains `event_id VARCHAR(50) NOT NULL FK → events(id)`. New tables: `bartender_users(id, username UNIQUE, password_hash, bar_id FK)` and `payment_accounts(id, event_id UNIQUE FK, mp_user_id, access_token, refresh_token, expires_at, linked_at)`.
- **Two distinct live events with different lineups:**
  - *Festival Eclipse (e1)*: 3 bars (`eclipse-north`, `eclipse-center`, `eclipse-south`), 15 Eclipse products (p1–p15, beer/food focus) + `p_test` dev product at $5 ARS.
  - *Festival Cumbiero (e2)*: 2 bars (`cumbia-main`, `cumbia-vip`), 8 Cumbiero products (p16–p23, fernet/wine focus) + `p_test`. No product overlap between events.
- **`Bar` entity** gains `@ManyToOne Event event` (`@JsonIgnore`) and a read-only `eventId` column (`insertable=false, updatable=false`) that appears in the JSON response.
- **`BarsRepository`** gains `findByEventId(eventId)`. **`OrdersRepository`** gains `findTop50ByBarAndStatusOrderByIdDesc` for the delivered tab. **`OrdersService.pickLeastLoadedBar`** accepts `eventId` and scopes candidate bars to `barsRepo.findByEventId(eventId)` when provided; falls back to all bars when `eventId` is null.
- **`EventsController`** gains `GET /events/{eventId}/bars` and `GET /events/{eventId}/menu` (distinct union of products across all bars of that event, implemented in `BarsService.getMenuForEvent`).
- **`OrdersController`** accepts optional `?status=delivered` on `GET /orders?bar={id}` — returns the last 50 delivered orders for that bar via the new repository method.
- **`CreateOrderRequest`** gains optional `eventId` field for event-scoped bar assignment.
- **Bartender auth** (`POST /auth/bartender/login`): `AuthController` → `AuthService` → `BartenderUsersRepository.findByUsername` → BCrypt password check → `LoginResponse{username, barId, barLabel, eventId, eventName}`. Returns 401 on miss.
- **`DataInitializer`** (`Config/DataInitializer.java`): seeds 5 `BartenderUser` rows on first boot (table-empty guard) using `BCryptPasswordEncoder`. Dev credentials: `eclipse-north/norte123`, `eclipse-center/centro123`, `eclipse-south/sur123`, `cumbia-main/principal123`, `cumbia-vip/vip123`.
- **`SecurityConfig`** exposes `PasswordEncoder` as a `@Bean` (BCrypt). All endpoints remain `permitAll()` — no JWT/session wiring this round.
- **Payment account linking** (`GET|POST|DELETE /events/{eventId}/payment-account`): `PaymentAccountsController` → `PaymentAccountsService`. `POST` body `{ accessToken }` validates the token against MP `/users/me`, stores the account. `PaymentsService.createPreference` now calls `PaymentAccountsService.getTokenForEvent(eventId)` as the first fallback before the env-var token.
- **`PaymentsService`** logs the MP preference response at INFO when not simulated (one line, no secrets).
- **Tests:** all 63 pass. `OrdersControllerTest` updated for new bar IDs and seed layout. `DataEndpointsTest` updated for 5 bars, 24 products, 5 visible events, and per-event menu sizes (Eclipse 16, Cumbiero 9).

### 2026-06-03 — Lenient bar assignment + Mercado Pago integration
- `OrdersService.pickLeastLoadedBar` is two-phase: **strict** (bar serves every requested PID) wins; **lenient fallback** (bar with the most matches, tie-broken by least load) catches mixed carts that previously 422'd. Result: `POST /orders` now succeeds for any cart with valid product IDs. The brittle "no bar can serve all items" 422 is gone — `OrdersControllerTest.createOrder_mixedCart_fallsBackToBestMatchBar` covers the new path.
- New `POST /payments/preference` (Checkout Pro). Body `{ orderId }`; response `{ preferenceId, initPoint, sandboxInitPoint, simulated }`. Implementation in `Services/PaymentsService.java` uses Spring `RestClient` directly (no MP Java SDK — saves ~3 MB and the deprecated transitive deps).
- **Graceful fallback when MP_ACCESS_TOKEN is blank**: the endpoint returns a *simulated* `initPoint` of the form `<webOrigin>/?status=approved&external_reference=FB<id>`. This lets the frontend redirect-back code work end-to-end without an MP account — same code path, just no MP hop. Set `MP_ACCESS_TOKEN` (and optionally `FASTBUY_WEB_ORIGIN`) to enable real sandbox/production charges.
- `mercadopago.access-token` and `fastbuy.web-origin` are wired via `application.properties` (`${MP_ACCESS_TOKEN:}` / `${FASTBUY_WEB_ORIGIN:http://localhost:5173}`).
- `data.sql` now has 7 events (3 currently live with wide ~half-day windows so they survive a long dev session, 1 upcoming, 1 recently-finished within grace, 1 long-past filtered out, 1 far-future). Product `image` columns now hold unique emoji glyphs — no more `/img/products/*.png` URLs that 404 and collapse to duplicate fallbacks.
- The previous `mercado-pago-spike.md` was deleted — its plan is now realized in the live code.

### 2026-06-10 — Multi-bartender order locking
- `orders` table gains `claimed_by VARCHAR(100)` and `claimed_at TIMESTAMP` (both nullable). No FK — username is stable enough.
- `OrderResponse` DTO gains `claimedBy` (String, nullable).
- `advanceOrder(id, bartenderId)`: QUEUE→PREPARING sets `claimedBy` + `claimedAt = now()`. PREPARING→READY keeps `claimedBy`.
- `getOrdersByBar(barId)`: lazy expiry before returning — PREPARING orders with `claimedAt < now() - 2 min` are reset to QUEUE with null claim fields. Relies on 5s frontend polling; no scheduler needed.
- `releaseOrder(id)`: new method, resets PREPARING → QUEUE + null claims. Exposed via `POST /orders/{id}/release`.
- `POST /orders/{id}/advance` body: optional `{ bartenderId }` via new `AdvanceRequest` record. Backwards compatible (no body = null bartenderId = no claim set).
- New tests in `OrdersControllerTest`: `advanceOrder_setsClaimForBartender`, `getOrders_expiredLock_resetsToQueue`, `releaseOrder_returnsToQueue`. 66 tests total, all green.

### 2026-06-10 — Mercado Pago sandbox notes
- `mercadopago.sandbox=true` in `application-local.properties` routes to `sandbox_init_point` regardless of token prefix (replaces `token.startsWith("TEST-")` check).
- `auto_return` is omitted in sandbox mode to avoid browser "too many redirects" error caused by MP's extra internal hops in the sandbox environment.
- Test seller credentials are `APP_USR-` format (production-style, no `TEST-` token available from the developer panel for this account). Sandbox works using the test buyer's **account balance** — guest card payments (simulated test cards) are unreliable in MP sandbox Argentina with Checkout Pro. This is a known MP limitation, not a code issue.
- Test buyer credentials are stored as comments in `application-local.properties` (gitignored). Do not commit them.

### 2026-06-22 — Order responses, no-show cancel, single-order lookup, claim window (develop)
- `OrderResponse` gained `barLabel` (friendly bar name, e.g. "Barra Norte"), resolved once per list call in `OrdersService` via `barsRepo.findById(barId)` (single shared bar id per list).
- New `OrderState.CANCELLED` + `OrdersService.cancelOrder` (READY → CANCELLED) + `POST /orders/{id}/cancel` — a no-show drops off the active board and frees the bartender.
- New `GET /orders/{id}` → `OrdersService.getOrder` returns a single order in any state (incl. delivered/cancelled), 404 if absent. Lets the customer view distinguish freed / cancelled / delivered.
- PREPARING claim-expiry window widened **2 → 15 min** (`CLAIM_EXPIRY_MINUTES`); the 2-minute window reverted orders mid-preparation, breaking "Marcar listo" / "Liberar".
- `OrdersControllerTest` now 32 tests (cancel, barLabel, single-order lookup, updated expiry).
- The app is hosted (Render backend + Cloudflare Pages frontend). Locally, `start-fastbuy.ps1` still runs an ngrok tunnel so MP has a public return URL.
- **MP `auto_return`** is only set when `fastbuy.web-origin` is `https://…` (`PaymentsService`). MP returns 400 (`auto_return invalid. back_url.success must be defined`) for a localhost back_url, which made local payments fail instantly — so local dev uses the ngrok https origin. Hosted prod uses `https://*.pages.dev`.
- These are on **develop**; not yet on **production**.

### 2026-06-22 — Code-review pass (develop)
- **Auth on bartender writes.** `BartenderAuthInterceptor` requires `Authorization: Bearer <token>` on `/orders/*/advance|deliver|cancel|release` (registered in `CorsConfig.addInterceptors`; OPTIONS preflight passes). Login issues an opaque `session_token` (new column on `bartender_users`, stored via `AuthService`, returned in `LoginResponse`). Customer GETs and `POST /orders` stay open. Tests use an `authPost` helper with a seeded token.
- **Stock.** `createOrder` is `@Transactional` and reserves stock with an atomic conditional decrement (`ProductsRepository.decrementStock`, `UPDATE … WHERE stock >= q`); insufficient → 422, rolled back.
- **Lock expiry** is a `@Scheduled` (30s) bulk `UPDATE` (`OrdersRepository.expireStaleClaims`) — `@EnableScheduling` on the app; `GET /orders` no longer mutates.
- **Queue position.** `getOrder` computes a 1-based `queuePosition` (`OrderResponse` field) for QUEUE orders.
- **MP tokens encrypted at rest.** `PaymentAccountsService` encrypts/decrypts the per-event access token via a `TextEncryptor` bean (`SecurityConfig`); key/salt from `FASTBUY_TOKEN_KEY`/`FASTBUY_TOKEN_SALT` (dev default insecure — set in prod).
- **MP token passed per-request** via `MPRequestOptions` (not the static `MercadoPagoConfig`) to avoid a concurrency race.
- These are on **develop**; not yet on **production**.
