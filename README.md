# Tic Tac Toe (Online)

A Spring Boot backend for online multiplayer Tic Tac Toe featuring real-time WebSocket
gameplay, an Elo-based leaderboard, and delegated user authentication.

Two players register, one creates a game, the other joins, and they play alternating moves.
Every move is broadcast live to subscribers over WebSocket. When a game finishes, results are
recorded and each player's Elo rating is updated on a Redis-backed leaderboard.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Modules](#modules)
- [Data model](#data-model)
- [Game lifecycle](#game-lifecycle)
- [Elo & leaderboard design](#elo--leaderboard-design)
- [Real-time updates](#real-time-updates)
- [Authentication model](#authentication-model)
- [API reference](#api-reference)
- [Error responses](#error-responses)
- [Running](#running)
- [Configuration](#configuration)
- [Testing](#testing)
- [Project structure](#project-structure)

---

## Features

- **User accounts** — registration delegated to an external auth microservice, with a local
  profile (username, first/last name) linked to the returned auth identity.
- **Player profiles** — a lightweight, uniquely-named game identity linked to an auth user.
- **Game management** — create, join, start, list, inspect, and delete games.
- **Turn-based gameplay** — server-authoritative move validation (turn order, cell occupancy,
  game state), automatic win/draw detection.
- **Real-time board updates** — every move is broadcast over WebSocket (STOMP) via Redis
  pub/sub, so multiple app instances can fan out updates.
- **Optimistic concurrency** — games use a JPA `@Version` so concurrent moves fail safely
  with `409 Conflict` instead of corrupting state.
- **Result recording** — every finished game writes an immutable per-player result row
  (win = 3, draw = 1, loss = 0 points).
- **Elo leaderboard** — ratings computed with the standard Elo formula (K = 32, initial 1200),
  cached in Redis sorted sets/hashes with an authoritative audit-based rebuild and fallback.
- **Admin** — paginated listing of registered user profiles.
- **Consistent error handling** — a single `@RestControllerAdvice` maps domain exceptions to
  structured JSON error bodies.

---

## Tech stack

- **Java 21**, **Spring Boot 3.3.5**
- Spring Web (REST), Spring WebSocket (STOMP + SockJS)
- Spring Data JPA + **H2** (file-based, `AUTO_SERVER`)
- Spring Data Redis (leaderboard cache + pub/sub)
- Bean Validation (Jakarta)
- Lombok
- UUIDv7 IDs (`java-uuid-generator`)
- Maven, Docker / Docker Compose

---

## Architecture

The service is a modular monolith organized by domain (user, player, game, result, admin). It
depends on two external systems: an **auth microservice** for credential handling and **Redis**
for the leaderboard cache and real-time event bus. Game and result data live in H2 via JPA.

```
                         ┌──────────────────────────┐
        X-User-Id        │      Auth microservice    │
   ┌───────────────────► │  POST /auth/register      │
   │  (register/login    └──────────────────────────┘
   │   handled by gateway)          ▲
   │                                │ REST (RestClient)
┌──┴────────┐   REST / WS   ┌───────┴───────────────────────────────────┐
│  Clients  │ ────────────► │        Tic Tac Toe backend (Spring)        │
│ (browser/ │ ◄──────────── │                                            │
│  service) │   /topic/...  │  user · player · game · result · admin     │
└───────────┘               │                                            │
                            │   JPA  │              │  Redis             │
                            └────────┼──────────────┼────────────────────┘
                                     ▼              ▼
                              ┌────────────┐  ┌───────────────────────────┐
                              │  H2 (file) │  │ Redis                     │
                              │ profiles   │  │  ZSET leaderboard:ratings │
                              │ players    │  │  HASH leaderboard:stats:* │
                              │ games      │  │  PUBSUB channel           │
                              │ game_results│ │    "game-events"          │
                              └────────────┘  └───────────────────────────┘
```

**Move → broadcast flow (decoupled via Redis pub/sub so it scales horizontally):**

```
POST /games/{id}/moves
        │
        ▼
   GameService.makeMove()  ──►  validate turn / cell / state
        │                       update board (JPA, @Version)
        │                       if terminal → GameResultService.recordResult()
        ▼
   GameEventPublisher  ──PUBLISH──►  Redis channel "game-events"
                                            │
                                            ▼
                                   GameEventSubscriber (any instance)
                                            │
                                            ▼
                               STOMP send → /topic/games/{id}
                                            │
                                            ▼
                                 subscribed WebSocket clients
```

---

## Modules

| Package   | Responsibility |
|-----------|----------------|
| `common`  | Cross-cutting config: CORS/`WebConfig`, `WebSocketConfig`, `RedisConfig`, `GlobalExceptionHandler`, `UuidGenerator`, and the `security` header-based auth resolver. |
| `user`    | Account registration via the external auth service + local `Profile`. |
| `player`  | Game-facing player identity (unique username) linked to an auth user. |
| `game`    | Game entity, board logic, state machine, move handling, and the game-event pub/sub. |
| `result`  | Immutable game results, Elo computation, and the Redis-backed leaderboard. |
| `admin`   | Administrative views (paginated user listing). |

---

## Data model

All REST paths are served under the context path **`/tic-tac-toe`**.

**`profiles`** (`user`) — string UUIDv7 id
| Column | Notes |
|--------|-------|
| `id` | PK, 32-char UUIDv7 |
| `authId` | unique, from auth service, not updatable |
| `username`, `firstName`, `lastName` | required |
| `createdAt`, `updatedAt` | timestamps |

**`players`** (`player`) — string UUIDv7 id
| Column | Notes |
|--------|-------|
| `id` | PK, 32-char UUIDv7 |
| `authId` | unique, nullable |
| `username` | unique |
| `createdAt` | timestamp |

**`games`** (`game`) — numeric identity id
| Column | Notes |
|--------|-------|
| `id` | PK, auto-increment |
| `playerXId` | required, not updatable |
| `playerOId` | nullable until an opponent joins |
| `board` | 9-char string, e.g. `---------` |
| `status` | `GameStatus` enum |
| `version` | `@Version` optimistic lock |
| `createdAt` | timestamp |

**`game_results`** (`result`) — numeric identity id, immutable
| Column | Notes |
|--------|-------|
| `id` | PK, auto-increment |
| `gameId`, `playerId` | not updatable |
| `outcome` | `WIN` / `DRAW` / `LOSS` |
| `points` | 3 / 1 / 0 |
| `createdAt` | timestamp |

The board is a 9-character string indexed row-major (0–8), using `X`, `O`, and `-` for empty.
`X` always moves first; the side to move is derived from the mark counts on the board.

---

## Game lifecycle

Game status is an explicit state machine; illegal transitions throw `InvalidGameStateException`
(`409`).

```
WAITING_FOR_OPPONENT ──join──► READY ──start──► IN_PROGRESS ──┐
                                                    ▲         │ (each move)
                                                    └─────────┘
                                                         │
                                            ┌────────────┼────────────┐
                                            ▼            ▼            ▼
                                          X_WON        O_WON        DRAW   (terminal)
```

- `WAITING_FOR_OPPONENT` → set on create (only `playerX` present).
- `READY` → an opponent joins (sets `playerO`). A player cannot join their own game.
- `IN_PROGRESS` → a participant starts the game; moves are now accepted.
- `X_WON` / `O_WON` / `DRAW` → terminal. Reaching a terminal status triggers result recording
  and leaderboard update.

**Move validation** (`POST /games/{id}/moves`) enforces: game is `IN_PROGRESS`, caller is a
participant, it is the caller's turn (X or O), and the target cell is empty. Concurrent moves
on the same game are guarded by the `@Version` optimistic lock (retry on `409`).

---

## Elo & leaderboard design

Results are the **source of truth**; the Redis leaderboard is a cache derived from them.

- **Result points** — `WIN = 3`, `DRAW = 1`, `LOSS = 0` (stored per player, per game).
- **Elo** — `EloRating` uses the standard formula with `INITIAL_RATING = 1200`, `K = 32`:
  `expected = 1 / (1 + 10^((opp - r)/400))`, `newRating = round(r + K * (actual - expected))`
  where actual score is `WIN = 1.0`, `DRAW = 0.5`, `LOSS = 0.0`.
- **Audit calculator** (`AuditEloCalculator`) — replays all results in order to compute
  authoritative per-player rating/W/L/D. Used to warm up and rebuild Redis, and as the fallback
  when Redis is unavailable.
- **Redis cache** (`RedisLeaderboard`) —
  - `ZSET leaderboard:ratings` — member `playerId`, score `rating`.
  - `HASH leaderboard:stats:{playerId}` — fields `wins`, `losses`, `draws`.
  - Rebuilt from the audit calculator on `ApplicationReadyEvent`; updated incrementally after
    each finished game.
- **Ranking** (`LeaderboardRanker`) — sort by rating desc, then username (case-insensitive);
  tied ratings share a rank (1, 2, 2, 4, …).

If Redis reads fail, `LeaderboardService` transparently falls back to the audit computation, so
the leaderboard is always available.

---

## Real-time updates

WebSocket is configured with STOMP over SockJS:

- **Endpoint:** `ws://<host>/tic-tac-toe/ws` (SockJS fallback enabled)
- **Application prefix:** `/app` (client → server)
- **Broker prefix:** `/topic`
- **Game topic:** `/topic/games/{gameId}` — receives a `GameResponse` payload on every move.

Broadcasts are delivered through the Redis channel **`game-events`**: the move handler publishes,
and a subscriber on any instance relays to the local STOMP broker. This can be disabled/enabled
via `game.redis.enabled` (default `true`).

---

## Authentication model

This service does **not** manage passwords or issue tokens itself:

1. `POST /users/register` forwards email + password to the external **auth microservice**
   (`auth.service.base-url`, default `http://localhost:8081/auth`) via `AuthClient`.
2. On success, the returned auth id is stored on a local `Profile`.
3. Protected endpoints identify the caller via the **`X-User-Id`** header (assumed validated by
   an upstream gateway). `AuthPrincipalArgumentResolver` injects it as an `AuthUser` into
   `@AuthPrincipal`-annotated controller parameters; a missing header yields `401`.

Endpoints requiring `X-User-Id`: `GET /users/me`, `POST /players`, `GET /players/me`,
`POST /games/{id}/start`, `POST /games/{id}/moves`.

---

## API reference

Base URL: `http://localhost:8082/tic-tac-toe` (local default). Replace host/port as needed.

### Users

```bash
# Register (delegates to auth service, creates local profile)
curl -i -X POST http://localhost:8082/tic-tac-toe/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","username":"alice","password":"password123","firstName":"Alice","lastName":"A"}'

# Current user's profile
curl http://localhost:8082/tic-tac-toe/users/me -H "X-User-Id: <authId>"
```

| Method | Path | Auth | Body | Result |
|--------|------|------|------|--------|
| POST | `/users/register` | – | `RegisterRequest` | `201` `ProfileResponse` |
| GET | `/users/me` | `X-User-Id` | – | `ProfileResponse` |

### Players

| Method | Path | Auth | Body | Result |
|--------|------|------|------|--------|
| POST | `/players` | `X-User-Id` | `{ "username": "alice" }` | `201` `PlayerResponse` |
| GET | `/players/me` | `X-User-Id` | – | `PlayerResponse` |
| GET | `/players/{id}` | – | – | `PlayerResponse` |
| GET | `/players` | – | – | `PlayerResponse[]` |

Username rules: 3–20 chars, letters/digits/underscores, case-insensitively unique.

### Games

```bash
# Create a game (X)
curl -X POST http://localhost:8082/tic-tac-toe/games \
  -H "Content-Type: application/json" -d '{"playerXId":"<playerId>"}'

# Opponent joins (O)
curl -X POST http://localhost:8082/tic-tac-toe/games/1/join \
  -H "Content-Type: application/json" -d '{"playerId":"<playerId>"}'

# A participant starts the game
curl -X POST http://localhost:8082/tic-tac-toe/games/1/start -H "X-User-Id: <authId>"

# Make a move (cell 0-8)
curl -X POST http://localhost:8082/tic-tac-toe/games/1/moves \
  -H "X-User-Id: <authId>" -H "Content-Type: application/json" -d '{"cell":4}'
```

| Method | Path | Auth | Body | Result |
|--------|------|------|------|--------|
| POST | `/games` | – | `{ "playerXId": "..." }` | `201` `GameResponse` |
| GET | `/games` | – | – | `GameResponse[]` |
| GET | `/games/{id}` | – | – | `GameResponse` |
| POST | `/games/{id}/join` | – | `{ "playerId": "..." }` | `GameResponse` |
| POST | `/games/{id}/start` | `X-User-Id` | – | `GameResponse` |
| PUT | `/games/{id}` | – | `{ "board": "XOX--O--X" }` | `GameResponse` |
| POST | `/games/{id}/moves` | `X-User-Id` | `{ "cell": 0-8 }` | `GameResponse` |
| DELETE | `/games/{id}` | – | – | `204` |

`GameResponse`: `{ id, playerXId, playerOId, board, status, createdAt }`.

### Results & leaderboard

| Method | Path | Query | Result |
|--------|------|-------|--------|
| GET | `/results` | `playerId` or `gameId` (optional) | `GameResultResponse[]` |
| GET | `/leaderboard` | – | `LeaderboardEntry[]` |

`LeaderboardEntry`: `{ rank, playerId, username, rating, wins, losses, draws, gamesPlayed }`.

### Admin

| Method | Path | Query | Result |
|--------|------|-------|--------|
| GET | `/admin/users` | `page` (≥0), `size` (≤100, default 20) | `PagedResponse<ProfileResponse>` |

---

## Error responses

All errors share a consistent JSON shape produced by `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-07-18T00:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "username already exists",
  "errors": { "username": "must be 3-20 characters" }
}
```

`errors` is only present for validation failures (`400`).

| Exception | Status |
|-----------|--------|
| `MethodArgumentNotValidException` (validation) | `400` |
| `UnauthorizedException` (missing `X-User-Id`) | `401` |
| `PlayerNotFoundException`, `GameNotFoundException`, `ProfileNotFoundException` | `404` |
| `UsernameAlreadyExistsException`, `PlayerAlreadyRegisteredException` | `409` |
| `InvalidMoveException`, `InvalidGameStateException` | `409` |
| `ObjectOptimisticLockingFailureException` (concurrent move) | `409` |
| `AuthServiceException` | propagated from auth service |
| `AuthServiceUnavailableException` | `503` |

---

## Running

### Prerequisites

- JDK 21, Maven
- Redis (for leaderboard + real-time events) — optional for basic REST if
  `game.redis.enabled=false`
- The external auth microservice (only needed for `POST /users/register`)

### Local

```bash
# start Redis (example)
docker run -p 6379:6379 redis:7-alpine

mvn spring-boot:run
```

App starts at **http://localhost:8082/tic-tac-toe** (H2 console at
`http://localhost:8082/tic-tac-toe/h2-console`, JDBC URL `jdbc:h2:file:./data/tictactoe`,
user `sa`, no password).

### Docker Compose

```bash
docker compose up --build
```

This builds the app image and starts it alongside Redis (`REDIS_HOST=redis`). Set `SERVER_PORT`
to align the container port with the compose port mapping if you change it.

---

## Configuration

Key properties (`src/main/resources/application.yml`), most overridable via environment variables:

| Property | Default | Purpose |
|----------|---------|---------|
| `server.port` | `8082` (`SERVER_PORT`) | HTTP port |
| `server.servlet.context-path` | `/tic-tac-toe` | Base path for all routes |
| `spring.datasource.url` | `jdbc:h2:file:./data/tictactoe;AUTO_SERVER=TRUE` | H2 file DB |
| `spring.data.redis.host` / `.port` | `localhost` / `6379` (`REDIS_HOST` / `REDIS_PORT`) | Redis connection |
| `auth.service.base-url` | `http://localhost:8081/auth` | External auth service |
| `game.redis.enabled` | `true` | Enable Redis leaderboard cache + pub/sub |

---

## Testing

```bash
mvn test
```

---

## Project structure

```
src/main/java/com/bidhutkarki/tictactoe
├── TicTacToeApplication.java
├── common
│   ├── GlobalExceptionHandler.java   # unified error responses
│   ├── WebConfig.java                # CORS + arg resolver registration
│   ├── WebSocketConfig.java          # STOMP /ws, /topic, /app
│   ├── RedisConfig.java              # pub/sub listener container
│   ├── UuidGenerator.java            # UUIDv7 ids
│   └── security                      # AuthPrincipal / AuthUser / resolver
├── user      # Profile, external auth client, registration
├── player    # Player identity + registration
├── game      # Game entity, Board, GameStatus, moves, event pub/sub
├── result    # GameResult, Elo, Redis leaderboard, ranking
└── admin      # paginated user listing
```
