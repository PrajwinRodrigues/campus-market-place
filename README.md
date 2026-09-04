# Campus Marketplace

A REST API backend for a buy-and-sell marketplace for college students — students list items (books, electronics, furniture, etc.), browse/search what others are selling, favorite listings, and message sellers directly.

## Tech stack

| Layer | Choice |
|---|---|
| Language / Runtime | Java 17, built with JDK 22 |
| Framework | Spring Boot 3.2.5 |
| Persistence | Spring Data JPA (Hibernate) + MySQL 8/9 |
| Auth | Spring Security + JWT (stateless, via `jjwt`) |
| Docs | springdoc-openapi (Swagger UI) |
| Build | Maven |

## Data model

- **User** — registered student (name, email, password hash, phone, college, hostel/location, role). Doubles as the Spring Security principal.
- **Listing** — an item for sale (title, description, price, category, condition, status, location, image URLs, seller).
- **Favorite** — a user bookmarking a listing (unique per user+listing).
- **Message** — a chat message tied to a listing, between a sender and receiver (buyer↔seller).

Enums: `Category` (BOOKS, ELECTRONICS, FURNITURE, STATIONERY, HOSTEL_ESSENTIALS, CLOTHING, SPORTS, VEHICLES, OTHER), `ItemCondition` (NEW, LIKE_NEW, GOOD, FAIR), `ListingStatus` (AVAILABLE, RESERVED, SOLD), `Role` (STUDENT, ADMIN).

## Running it locally

**Prerequisites:** JDK 17+ (project targets 17; built here with JDK 22), Maven, MySQL running locally.

1. Create the database and a dedicated app user — pick your own password, don't reuse the example:
   ```sql
   CREATE DATABASE campus_marketplace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'campus_app'@'localhost' IDENTIFIED BY 'choose-your-own-password';
   GRANT ALL PRIVILEGES ON campus_marketplace.* TO 'campus_app'@'localhost';
   ```
2. Set the matching env var before running (there's no working default — the app won't start without it, on purpose):
   ```bash
   export DB_PASSWORD=choose-your-own-password   # PowerShell: $env:DB_PASSWORD = "..."
   ```
   Other optional overrides: `DB_USERNAME` (defaults to `campus_app`), `JWT_SECRET`, `JWT_EXPIRATION_MS`, `UPLOAD_DIR`.
3. Build and run:
   ```bash
   mvn clean package -DskipTests
   java -jar target/campus-marketplace-0.0.1-SNAPSHOT.jar
   ```
4. App runs on `http://localhost:8080`. Interactive API docs: `http://localhost:8080/swagger-ui/index.html`.

Hibernate manages the schema automatically (`ddl-auto: update`) — no manual migrations needed for this project's scope.

## API overview

All endpoints are prefixed `/api`. Endpoints marked 🔒 require `Authorization: Bearer <token>`.

**Auth**
- `POST /auth/register` — create account, returns JWT
- `POST /auth/login` — returns JWT

**Users** 🔒
- `GET /users/me` / `PUT /users/me`

**Listings**
- `POST /listings` 🔒 — create
- `GET /listings` — search/browse, public. Query params: `keyword`, `category`, `condition`, `status`, `minPrice`, `maxPrice`, plus paging (`page`, `size`, `sort`)
- `GET /listings/{id}` — public
- `GET /listings/mine` 🔒
- `PUT /listings/{id}` 🔒 (owner/admin only)
- `PATCH /listings/{id}/status` 🔒 (owner/admin only)
- `POST /listings/{id}/images` 🔒 — multipart image upload (owner/admin only)
- `DELETE /listings/{id}` 🔒 (owner/admin only)

**Favorites** 🔒 — `POST /favorites/{listingId}`, `DELETE /favorites/{listingId}`, `GET /favorites`

**Messages** 🔒 — `POST /messages`, `GET /messages/conversations`, `GET /messages/listing/{listingId}/user/{otherUserId}`, `PATCH /messages/listing/{listingId}/user/{otherUserId}/read`

## Design notes worth knowing (in case you're asked)

- **Stateless JWT auth**: `JwtAuthFilter` runs once per request, validates the bearer token, and populates `SecurityContextHolder` — no server-side sessions.
- **Dynamic search**: `ListingSpecifications` builds a JPA `Specification` from whichever filters are present, so `GET /listings` supports any combination of keyword/category/condition/status/price range without a combinatorial explosion of query methods.
- **DTOs, not entities, cross the wire**: every response is a `record` mapped from the entity in the service layer, inside the `@Transactional` boundary — this matters because collections/associations are lazy-loaded (`open-in-view: false`), so they must be resolved *before* the transaction closes, or Jackson serialization fails later with `LazyInitializationException`.
- **Ownership checks**: listing mutations check `seller.id == currentUser.id || role == ADMIN` in the service layer, not the controller — keeps authorization logic in one place.

## Known rough edges (not yet built)

- No pagination cap / rate limiting.
- No email verification or password reset flow.
- No real-time messaging (REST polling only, no WebSocket).
- No refresh tokens — JWT just expires (default 24h) and the user re-logs in.
- No automated test suite yet.
