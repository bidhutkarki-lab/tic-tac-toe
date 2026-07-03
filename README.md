# Tic Tac Toe (Online)

A Java Spring Boot backend for an online Tic Tac Toe game.

This is **Part 1: Player Registration**. A player registers with just a username.
Gameplay, matchmaking, and other parts will be added later.

## Tech stack

- Java 21
- Spring Boot 3.3.x (Web, Data JPA, Validation)
- In-memory H2 database
- Maven

## Running

```bash
mvn spring-boot:run
```

The app starts on http://localhost:8080

## API

### Register a player

```bash
curl -i -X POST http://localhost:8080/api/players \
  -H "Content-Type: application/json" \
  -d '{"username": "alice"}'
```

`201 Created`
```json
{ "id": 1, "username": "alice", "createdAt": "2026-07-03T01:00:00Z" }
```

Username rules: 3–20 characters, letters/digits/underscores only, case-insensitively unique.

- Duplicate username -> `409 Conflict`
- Invalid username -> `400 Bad Request` (with per-field `errors`)

### List players

```bash
curl http://localhost:8080/api/players
```

## Other

- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:tictactoe`, user `sa`, no password)

## Tests

```bash
mvn test
```
