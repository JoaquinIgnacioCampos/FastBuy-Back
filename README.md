# FastBuy — Backend

Spring Boot REST API for FastBuy, an event-based beverage ordering system. Manages the full order lifecycle — from catalog browsing to queue management and Mercado Pago payment processing — for multiple concurrent events and bars.

- **Frontend repo:** [FastBuy-Front](https://github.com/JoaquinIgnacioCampos/FastBuy-Front)
- **Prod URL:** hosted on Render (free tier)

---

## Tech stack

| Layer | Tech |
|-------|------|
| Framework | Spring Boot 4.0.6 |
| Language | Java 17 |
| ORM | Spring Data JPA / Hibernate |
| Dev DB | H2 (in-memory, auto-reset on restart) |
| Prod DB | Neon Postgres (persistent) |
| Auth | BCrypt + opaque session tokens |
| Payments | Mercado Pago Checkout Pro |

---

## Project structure

```
src/main/java/grupo4/fastbuyback/
├── Controllers/      # REST endpoints
│   ├── AuthController          POST /auth/bartender/login, /auth/admin/login
│   ├── OrdersController        CRUD + state transitions for orders
│   ├── EventsController        GET /events, per-event menu + bars
│   ├── BarsController          GET /bars
│   ├── ProductsController      GET /products
│   ├── CategoriesController    GET /categories
│   ├── PaymentsController      POST /payments/preference
│   └── PaymentAccountsController GET/POST/DELETE /events/:id/payment-account
├── Services/         # Business logic
│   ├── OrdersService           Assignment, queue position, ETA calculation
│   ├── AuthService             BCrypt login, session token issuance
│   ├── PaymentsService         Mercado Pago preference creation, per-event token routing
│   └── PaymentAccountsService  Seller token validation + encrypted storage
├── Entities/         # JPA entities (Order, Bar, Event, Product, BartenderUser, AdminUser, …)
├── Repositories/     # Spring Data interfaces
├── DTOs/             # Request/response records
└── Config/
    ├── DataInitializer         Seeds bartender + admin users (BCrypt) on first boot
    ├── BartenderAuthInterceptor Bearer token gate for order write endpoints
    ├── AdminAuthInterceptor    Bearer token gate for payment-account writes
    └── CorsConfig              CORS + interceptor registration
src/main/resources/
├── schema.sql        # DDL for H2 dev (auto-applied by Spring)
├── data.sql          # Seed data for H2 dev (events, bars, products, categories)
└── application*.properties
deploy/
├── postgres-init.sql # Full schema + seed for a fresh Neon provision
├── reset-total.sql   # Wipe orders, restore stock, re-anchor event dates (pre-demo)
├── migrate-2026-06-23.sql  # Add item_count column
└── migrate-2026-06-24.sql  # Add admin_users table
```

---

## Running locally

```bash
./mvnw spring-boot:run
```

The backend binds to `:8080`. H2 schema and seed data are applied automatically on every boot (`schema.sql` + `data.sql`). No database setup needed for local dev.

To run alongside the frontend with ngrok (required for Mercado Pago redirects), use the launcher script from the frontend repo:

```powershell
.\start-fastbuy.ps1
```

---

## API reference

### Auth

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/bartender/login` | Returns bartender session token + bar info |
| POST | `/auth/admin/login` | Returns admin session token + event info |

Request body for both: `{ "username": "...", "password": "..." }`

### Events

| Method | Path | Description |
|--------|------|-------------|
| GET | `/events` | All events ending > 6 h ago, sorted by start date |
| GET | `/events/:id/menu` | Products served at any bar for this event |
| GET | `/events/:id/bars` | Bars belonging to this event |
| GET | `/events/:id/payment-account` | Current seller token config (open) |
| POST | `/events/:id/payment-account` | Set seller token (admin auth required) |
| DELETE | `/events/:id/payment-account` | Remove seller token (admin auth required) |

### Orders

| Method | Path | Description |
|--------|------|-------------|
| POST | `/orders` | Create order; auto-assigns least-loaded bar |
| GET | `/orders?bar=:barId` | Active orders for a bar (QUEUE + PREPARING + READY) |
| GET | `/orders?bar=:barId&status=delivered` | Delivered orders for a bar |
| GET | `/orders/:id` | Single order with `queuePosition` + `etaMinutes` |
| POST | `/orders/:id/advance` | Step: QUEUE → PREPARING → READY (bartender auth) |
| POST | `/orders/:id/deliver` | Mark READY → DELIVERED (bartender auth) |
| POST | `/orders/:id/cancel` | Cancel order (no-show) (bartender auth) |
| POST | `/orders/:id/release` | Release PREPARING → QUEUE (bartender auth) |

### Payments

| Method | Path | Description |
|--------|------|-------------|
| POST | `/payments/preference` | Create Mercado Pago preference; returns `initPoint` |

### Catalog

| Method | Path | Description |
|--------|------|-------------|
| GET | `/bars` | All bars |
| GET | `/products` | All products (union across all bars) |
| GET | `/categories` | All categories |

---

## Authentication

### Bartender auth
`POST /auth/bartender/login` returns `{ username, barId, barLabel, eventId, eventName, token }`.
Store the token in `fb_bartender_session.token` and send it as `Authorization: Bearer <token>` on order write endpoints. The `BartenderAuthInterceptor` validates it against `bartender_users.session_token`.

### Admin auth
`POST /auth/admin/login` returns `{ username, eventId, eventName, token }`.
Store the token in `fb_admin_session.token` and send it as `Authorization: Bearer <token>` on `POST`/`DELETE /events/:id/payment-account`. The `AdminAuthInterceptor` validates it against `admin_users.session_token`. Admin credentials are scoped to one event — they cannot access another event's payment config.

**Cross-role login is prevented by design:** bartender credentials only work at `/auth/bartender/login` and admin credentials only work at `/auth/admin/login` (separate tables, separate endpoints).

---

## Dev credentials

These are seeded by `DataInitializer` on every fresh H2 boot. BCrypt hashes are computed at startup.

### Bartenders

| Username | Password | Bar | Event |
|----------|----------|-----|-------|
| `eclipse-north` | `norte123` | Barra Norte | Festival Eclipse (e1) |
| `eclipse-center` | `centro123` | Barra Central | Festival Eclipse (e1) |
| `eclipse-south` | `sur123` | Barra Sur VIP | Festival Eclipse (e1) |
| `cumbia-main` | `principal123` | Barra Principal | Festival Cumbiero (e2) |
| `cumbia-vip` | `vip123` | Barra VIP | Festival Cumbiero (e2) |

### Admins (Organizadores)

| Username | Password | Event |
|----------|----------|-------|
| `admin-eclipse` | `eclipse2025` | Festival Eclipse (e1) |
| `admin-cumbia` | `cumbia2025` | Festival Cumbiero (e2) |
| `admin-cosquin` | `cosquin2025` | Cosquín Rock (e3) |
| `admin-lolla` | `lolla2025` | Lollapalooza Argentina 2027 (e5) |

---

## Order lifecycle

```
QUEUE → PREPARING → READY → DELIVERED
                 ↑
         QUEUE ← (release)

Any state → CANCELLED (no-show)
```

Bar assignment happens at `POST /orders` time: the server picks the bar with the fewest in-flight item-quantity (QUEUE + PREPARING orders) that stocks every requested product. The client never chooses a bar.

Queue position and ETA are returned on `GET /orders/:id`:
- `queuePosition` — count of QUEUE + PREPARING + READY orders ahead with a smaller ID (+ 1)
- `etaMinutes` — `ceil((itemsAhead + thisOrderItems) × 90s / 60)`, floored at 1 min

---

## Database

### Dev (H2)
Schema applied from `schema.sql`; seed data from `data.sql`. Tables and data reset on every restart. `DataInitializer` seeds bartender and admin users (BCrypt) after `ApplicationReadyEvent`.

### Prod (Neon Postgres)
`spring.jpa.hibernate.ddl-auto=none` — schema changes must be applied manually.

**Fresh provision:** run `deploy/postgres-init.sql` in the Neon SQL editor once. `DataInitializer` seeds users on first boot.

**Incremental migrations:**
| File | What it adds |
|------|-------------|
| `migrate-2026-06-23.sql` | `item_count` column on `orders` |
| `migrate-2026-06-24.sql` | `admin_users` table |

**Pre-demo reset:** run `deploy/reset-total.sql` to wipe orders, restore product stock, and re-anchor event dates relative to `NOW()`.

---

## Environment variables (production)

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | Neon Postgres JDBC URL |
| `SPRING_DATASOURCE_PASSWORD` | Neon DB password |
| `MP_ACCESS_TOKEN` | Mercado Pago platform access token |
| `FASTBUY_TOKEN_KEY` | AES key for encrypting stored seller tokens |
| `FASTBUY_TOKEN_SALT` | Salt for the AES key derivation |
| `FASTBUY_CORS_ORIGINS` | Comma-separated allowed browser origins (e.g. `https://fastbuy.pages.dev`) |

Dev defaults for `FASTBUY_TOKEN_KEY`/`FASTBUY_TOKEN_SALT` are intentionally insecure and must be overridden in production. Never commit the Mercado Pago access token or DB password.
