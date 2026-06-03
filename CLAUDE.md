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

### 2026-06-03 — Lenient bar assignment + Mercado Pago integration
- `OrdersService.pickLeastLoadedBar` is two-phase: **strict** (bar serves every requested PID) wins; **lenient fallback** (bar with the most matches, tie-broken by least load) catches mixed carts that previously 422'd. Result: `POST /orders` now succeeds for any cart with valid product IDs. The brittle "no bar can serve all items" 422 is gone — `OrdersControllerTest.createOrder_mixedCart_fallsBackToBestMatchBar` covers the new path.
- New `POST /payments/preference` (Checkout Pro). Body `{ orderId }`; response `{ preferenceId, initPoint, sandboxInitPoint, simulated }`. Implementation in `Services/PaymentsService.java` uses Spring `RestClient` directly (no MP Java SDK — saves ~3 MB and the deprecated transitive deps).
- **Graceful fallback when MP_ACCESS_TOKEN is blank**: the endpoint returns a *simulated* `initPoint` of the form `<webOrigin>/?status=approved&external_reference=FB<id>`. This lets the frontend redirect-back code work end-to-end without an MP account — same code path, just no MP hop. Set `MP_ACCESS_TOKEN` (and optionally `FASTBUY_WEB_ORIGIN`) to enable real sandbox/production charges.
- `mercadopago.access-token` and `fastbuy.web-origin` are wired via `application.properties` (`${MP_ACCESS_TOKEN:}` / `${FASTBUY_WEB_ORIGIN:http://localhost:5173}`).
- `data.sql` now has 7 events (3 currently live with wide ~half-day windows so they survive a long dev session, 1 upcoming, 1 recently-finished within grace, 1 long-past filtered out, 1 far-future). Product `image` columns now hold unique emoji glyphs — no more `/img/products/*.png` URLs that 404 and collapse to duplicate fallbacks.
- The previous `mercado-pago-spike.md` was deleted — its plan is now realized in the live code.
