# Ask Mode Documentation Rules (Non-Obvious Only)

## Project Structure Context

**Frontend Source Separation:** Angular source in `frontend/prebuild/` is separate from deployed WAR content in `frontend/src/main/webapp/` - the latter is generated, not source

**Multi-Service Architecture:** Four separate Liberty services (auth, player, game, frontend) each with own `src/main/liberty/config/server.xml` - not a monolith

**Shared Liberty Installation:** All services share single Liberty runtime at `build/wlp/` - counterintuitive for multi-service setup

**Database Optional:** Player service works without PostgreSQL (falls back to in-memory) - DB is optional, not required

**Service Communication:** Game service calls player service via MicroProfile REST Client with URL from environment variable, not service discovery

**WebSocket Endpoint:** Game rounds accessed via WebSocket at `/round/ws/{roundId}` - not REST API

**JNDI Configuration:** Game behavior (speed, map, cooldown) configured via JNDI in server.xml, not application.properties or code

**Keystore Passwords:** Each service has different keystore password in server.xml - not centralized config

**Frontend Build Location:** Angular build commands must run from `frontend/prebuild/` subdirectory, not project root

**Test Location:** Java tests in `src/test/java/` follow standard Maven layout, but Angular tests must run from `frontend/prebuild/`

## Documentation Gaps

**Player Slot Persistence:** Static map `GameBoard.preferredPlayerSlots` maintains player positions across rounds - not documented in code comments

**Round Lifecycle:** Rounds automatically deleted when last WebSocket client disconnects - implicit behavior in `GameRoundWebsocket.onClose()`

**CORS Configuration:** Only game-service has CORS enabled - other services don't accept cross-origin requests