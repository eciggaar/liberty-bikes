# Plan Mode Architecture Rules (Non-Obvious Only)

## Architectural Constraints

**Shared Liberty Runtime:** All services must use single Liberty installation at `build/wlp/` - cannot create separate Liberty installs per service

**Frontend Build Dependency:** Frontend WAR depends on Angular build in `frontend/prebuild/` - Gradle task orchestrates this, not separate build

**Database Optional Design:** Player service designed to work without PostgreSQL - `PlayerDBProducer` silently falls back to in-memory storage

**Stateless Round Design:** Game rounds are ephemeral - automatically deleted when last WebSocket client disconnects (no persistence)

**Static Player Slot Map:** `GameBoard.preferredPlayerSlots` is static/shared across all rounds - intentional design to maintain player positions

**Service Communication Pattern:** Game service calls player service via MicroProfile REST Client - URL configured via environment variable, not hardcoded

**WebSocket-Only Game Protocol:** Game rounds use WebSocket exclusively - no REST API for game state updates

**Per-Service JWT Keystores:** Each service has own keystore with different password - auth signs, others validate

**CORS Single Point:** Only game-service has CORS enabled - frontend must route through game service for cross-origin requests

**JNDI Dynamic Config:** Game behavior (speed, map, cooldown) configurable at runtime via JNDI - not requiring code changes

## Build System Constraints

**Liberty Task Chain:** `libertyStart` always runs `libertyStop` and `test` first - cannot skip this dependency chain

**Frontend Directory Lock:** Angular npm commands must run from `frontend/prebuild/` - package.json scripts expect this working directory

**Gradle Multi-Project:** Root build.gradle orchestrates all services - individual service builds depend on shared Liberty install

**Docker Compose Dependency:** `dockerStart` depends on `assemble` for all subprojects - must build all services before starting containers