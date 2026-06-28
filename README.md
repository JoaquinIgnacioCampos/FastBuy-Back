# FastBuy — Backend

API REST en Spring Boot para FastBuy, un sistema de pedidos de bebidas para eventos. Gestiona el ciclo de vida completo de los pedidos — desde la navegación del catálogo hasta la gestión de la cola y el procesamiento de pagos con Mercado Pago — para múltiples eventos y barras simultáneas.

- **Repo del frontend:** [FastBuy-Front](https://github.com/JoaquinIgnacioCampos/FastBuy-Front)
- **URL de producción:** alojado en Render (tier gratuito)

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 4.0.6 |
| Lenguaje | Java 17 |
| ORM | Spring Data JPA / Hibernate |
| DB dev | H2 (en memoria, se resetea al reiniciar) |
| DB prod | Neon Postgres (persistente) |
| Autenticación | BCrypt + tokens de sesión opacos |
| Pagos | Mercado Pago Checkout Pro |

---

## Estructura del proyecto

```
src/main/java/grupo4/fastbuyback/
├── Controllers/      # Endpoints REST
│   ├── AuthController          POST /auth/bartender/login, /auth/admin/login
│   ├── OrdersController        CRUD + transiciones de estado de pedidos
│   ├── EventsController        GET /events, menú y barras por evento
│   ├── BarsController          GET /bars
│   ├── ProductsController      GET /products
│   ├── CategoriesController    GET /categories
│   ├── PaymentsController      POST /payments/preference
│   └── PaymentAccountsController GET/POST/DELETE /events/:id/payment-account
├── Services/         # Lógica de negocio
│   ├── OrdersService           Asignación, posición en cola, cálculo de ETA
│   ├── AuthService             Login BCrypt, emisión de tokens de sesión
│   ├── PaymentsService         Creación de preferencia MP, ruteo de token por evento
│   └── PaymentAccountsService  Validación y almacenamiento encriptado del token de vendedor
├── Entities/         # Entidades JPA (Order, Bar, Event, Product, BartenderUser, AdminUser, …)
├── Repositories/     # Interfaces de Spring Data
├── DTOs/             # Records de request/response
└── Config/
    ├── DataInitializer         Seed de bartenders y admins (BCrypt) al iniciar
    ├── BartenderAuthInterceptor Gate de token Bearer para endpoints de escritura de pedidos
    ├── AdminAuthInterceptor    Gate de token Bearer para escrituras de cuenta de pago
    └── CorsConfig              CORS + registro de interceptores
src/main/resources/
├── schema.sql        # DDL para H2 dev (aplicado automáticamente por Spring)
├── data.sql          # Datos seed para H2 dev (eventos, barras, productos, categorías)
└── application*.properties
deploy/
├── postgres-init.sql # Schema completo + seed para una provisión nueva de Neon
├── reset-total.sql   # Limpia pedidos, restaura stock, re-ancla fechas de eventos (pre-demo)
├── migrate-2026-06-23.sql  # Agrega columna item_count
└── migrate-2026-06-24.sql  # Agrega tabla admin_users
```

---

## Ejecución local

```bash
./mvnw spring-boot:run
```

El backend se enlaza en `:8080`. El schema H2 y los datos seed se aplican automáticamente al iniciar (`schema.sql` + `data.sql`). No se necesita configuración de base de datos para el desarrollo local.

Para correr junto con el frontend y ngrok (necesario para las redirecciones de Mercado Pago), usar el script de inicio del repo del frontend:

```powershell
.\start-fastbuy.ps1
```

---

## Referencia de API

### Autenticación

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/bartender/login` | Devuelve token de sesión de bartender + info de barra |
| POST | `/auth/admin/login` | Devuelve token de sesión de admin + info de evento |

Body para ambos: `{ "username": "...", "password": "..." }`

### Eventos

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/events` | Todos los eventos que terminaron hace menos de 6 h, ordenados por fecha |
| GET | `/events/:id/menu` | Productos disponibles en cualquier barra de este evento |
| GET | `/events/:id/bars` | Barras pertenecientes a este evento |
| GET | `/events/:id/payment-account` | Config actual del token de vendedor (abierto) |
| POST | `/events/:id/payment-account` | Establecer token de vendedor (requiere auth de admin) |
| DELETE | `/events/:id/payment-account` | Eliminar token de vendedor (requiere auth de admin) |

### Pedidos

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/orders` | Crear pedido; asigna automáticamente la barra menos cargada |
| GET | `/orders?bar=:barId` | Pedidos activos de una barra (QUEUE + PREPARING + READY) |
| GET | `/orders?bar=:barId&status=delivered` | Pedidos entregados de una barra |
| GET | `/orders/:id` | Pedido individual con `queuePosition` + `etaMinutes` |
| POST | `/orders/:id/advance` | Avanzar estado: QUEUE → PREPARING → READY (auth bartender) |
| POST | `/orders/:id/deliver` | Marcar READY → DELIVERED (auth bartender) |
| POST | `/orders/:id/cancel` | Cancelar pedido (no-show) (auth bartender) |
| POST | `/orders/:id/release` | Liberar PREPARING → QUEUE (auth bartender) |

### Pagos

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/payments/preference` | Crear preferencia de Mercado Pago; devuelve `initPoint` |

### Catálogo

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/bars` | Todas las barras |
| GET | `/products` | Todos los productos (unión entre todas las barras) |
| GET | `/categories` | Todas las categorías |

---

## Autenticación

### Auth de bartender
`POST /auth/bartender/login` devuelve `{ username, barId, barLabel, eventId, eventName, token }`.
Guardar el token en `fb_bartender_session.token` y enviarlo como `Authorization: Bearer <token>` en los endpoints de escritura de pedidos. El `BartenderAuthInterceptor` lo valida contra `bartender_users.session_token`.

### Auth de admin
`POST /auth/admin/login` devuelve `{ username, eventId, eventName, token }`.
Guardar el token en `fb_admin_session.token` y enviarlo como `Authorization: Bearer <token>` en `POST`/`DELETE /events/:id/payment-account`. El `AdminAuthInterceptor` lo valida contra `admin_users.session_token`. Las credenciales de admin están limitadas a un evento — no pueden acceder a la config de pago de otro evento.

**El login entre roles está bloqueado por diseño:** las credenciales de bartender solo funcionan en `/auth/bartender/login` y las de admin solo en `/auth/admin/login` (tablas y endpoints separados).

---

## Credenciales de desarrollo

Estas son generadas por `DataInitializer` en cada arranque fresco de H2. Los hashes BCrypt se calculan al iniciar.

### Bartenders

| Usuario | Contraseña | Barra | Evento |
|---------|-----------|-------|--------|
| `eclipse-north` | `norte123` | Barra Norte | Festival Eclipse (e1) |
| `eclipse-center` | `centro123` | Barra Central | Festival Eclipse (e1) |
| `eclipse-south` | `sur123` | Barra Sur VIP | Festival Eclipse (e1) |
| `cumbia-main` | `principal123` | Barra Principal | Festival Cumbiero (e2) |
| `cumbia-vip` | `vip123` | Barra VIP | Festival Cumbiero (e2) |

### Admins (Organizadores)

| Usuario | Contraseña | Evento |
|---------|-----------|--------|
| `admin-eclipse` | `eclipse2025` | Festival Eclipse (e1) |
| `admin-cumbia` | `cumbia2025` | Festival Cumbiero (e2) |
| `admin-cosquin` | `cosquin2025` | Cosquín Rock (e3) |
| `admin-lolla` | `lolla2025` | Lollapalooza Argentina 2027 (e5) |

---

## Ciclo de vida de un pedido

```
QUEUE → PREPARING → READY → DELIVERED
              ↑
      QUEUE ← (release)

Cualquier estado → CANCELLED (no-show)
```

La asignación de barra ocurre al momento del `POST /orders`: el servidor elige la barra con menor cantidad de ítems en vuelo (pedidos en QUEUE + PREPARING) que tenga stock de todos los productos solicitados. El cliente nunca elige la barra.

La posición en cola y el ETA se devuelven en `GET /orders/:id`:
- `queuePosition` — cantidad de pedidos en QUEUE + PREPARING + READY con ID menor (+ 1)
- `etaMinutes` — `ceil((ítemsAdelante + ítemsDeEstePedido) × 90s / 60)`, mínimo 1 min

---

## Base de datos

### Dev (H2)
Schema aplicado desde `schema.sql`; datos seed desde `data.sql`. Las tablas y datos se resetean en cada reinicio. `DataInitializer` genera los usuarios de bartender y admin (BCrypt) después del `ApplicationReadyEvent`.

### Prod (Neon Postgres)
`spring.jpa.hibernate.ddl-auto=none` — los cambios de schema deben aplicarse manualmente.

**Provisión nueva:** ejecutar `deploy/postgres-init.sql` en el editor SQL de Neon una sola vez. `DataInitializer` genera los usuarios al primer arranque.

**Migraciones incrementales:**
| Archivo | Qué agrega |
|---------|-----------|
| `migrate-2026-06-23.sql` | Columna `item_count` en `orders` |
| `migrate-2026-06-24.sql` | Tabla `admin_users` |

**Reset pre-demo:** ejecutar `deploy/reset-total.sql` para limpiar pedidos, restaurar el stock de productos y re-anclar las fechas de los eventos relativas a `NOW()`.

---

## Variables de entorno (producción)

| Variable | Descripción |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | URL JDBC de Neon Postgres |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la BD Neon |
| `MP_ACCESS_TOKEN` | Token de acceso de la plataforma Mercado Pago |
| `FASTBUY_TOKEN_KEY` | Clave AES para encriptar tokens de vendedor almacenados |
| `FASTBUY_TOKEN_SALT` | Salt para la derivación de la clave AES |
| `FASTBUY_CORS_ORIGINS` | Orígenes de navegador permitidos separados por coma (ej. `https://fastbuy.pages.dev`) |

Los valores por defecto de `FASTBUY_TOKEN_KEY`/`FASTBUY_TOKEN_SALT` para dev son intencionalmente inseguros y deben sobreescribirse en producción. Nunca commitear el token de acceso de Mercado Pago ni la contraseña de la BD.
