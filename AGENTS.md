# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build & Test Commands

**Root-level (Gradle multi-project):**
- `./gradlew build` - Build all services
- `./gradlew clean` - Clean all services (stops Liberty servers first)
- `./gradlew dockerStart` - Build and start all services via docker-compose
- `./gradlew dockerStop` - Stop docker-compose services

**Per-service (from root, e.g., `game-service`):**
- `./gradlew game-service:libertyStart` - Start service (runs tests first, stops existing server)
- `./gradlew game-service:libertyStop` - Stop service
- `./gradlew game-service:libertyDebug` - Start in debug mode
- `./gradlew game-service:test` - Run JUnit tests

**Frontend (Angular, must run from `frontend/prebuild/`):**
- `cd frontend/prebuild && npm install` - Install dependencies
- `npm run build` - Development build
- `npm run prod` - Production build (uses environment.prod.ts)
- `npm test` - Run Karma/Jasmine tests
- `npm run lint` - Run TSLint

## Non-Obvious Architecture

**Shared Liberty Install:** All services use a single Liberty installation at `build/wlp/` (configured via `liberty.install.baseDir = rootProject.buildDir`)

**Frontend Build Process:** Angular app in `frontend/prebuild/` is built by Gradle task, output copied to `frontend/src/main/webapp/`, then packaged as WAR and deployed to Liberty

**Database Fallback:** Player service automatically falls back to in-memory storage if PostgreSQL is unavailable (see `PlayerDBProducer.java`)

**Player Slot Persistence:** Game board maintains preferred player slots across rounds using `preferredPlayerSlots` map to keep players in same positions

**WebSocket Session Management:** Game rounds track clients by WebSocket session; when last client disconnects, round is automatically deleted

**Dynamic Game Configuration:** Game speed, map selection, and auto-start cooldown are configurable via JNDI entries in `game-service/src/main/liberty/config/server.xml`

## Code Patterns

**JWT Keystore:** All services use keystores in `src/main/liberty/config/resources/security/`. Auth service signs JWTs, other services validate them. Keystore passwords differ per service (auth: "secret", player: "secret2", game: "secret")

**MicroProfile Config:** Services use `@ConfigProperty` for configuration with defaults (e.g., `@ConfigProperty(name = "jwtKeyStorePassword", defaultValue = "secret")`)

**REST Client Injection:** Game service calls player service via MicroProfile REST Client with `@RestClient` injection and URL configured via environment variable `org_libertybikes_restclient_PlayerService_mp_rest_url`

**CORS Configuration:** Game service has explicit CORS config in server.xml allowing all origins for cross-origin requests from frontend

## Testing

**Java Tests:** JUnit 4 tests in `src/test/java/`. Run with `./gradlew <service>:test`

**Angular Tests:** Karma/Jasmine tests in `frontend/prebuild/src/`. Must run from `frontend/prebuild/` directory

**Test Pattern:** Java tests use `@Before` for setup, verify board state directly via public `board` field in GameBoard

## Code Style

**TypeScript:** Single quotes, semicolons required, 140 char line limit, spaces for indentation (see tslint.json)

**Angular:** Component selectors use `app-` prefix with kebab-case, directive selectors use `app` prefix with camelCase

**Java:** Standard Java conventions, MicroProfile annotations for CDI/REST/Config