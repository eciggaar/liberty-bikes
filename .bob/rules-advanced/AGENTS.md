# Advanced Mode Rules (Non-Obvious Only)

## Critical Code Patterns

**Shared Liberty Install:** All services share single Liberty at `build/wlp/` via `liberty.install.baseDir = rootProject.buildDir` - don't create per-service installs

**Frontend Build Chain:** Angular in `frontend/prebuild/` → Gradle builds → copies to `frontend/src/main/webapp/` → packages as WAR. Never edit files in `frontend/src/main/webapp/` directly

**Database Fallback Logic:** `PlayerDBProducer.java` tries PostgreSQL first, silently falls back to in-memory if unavailable - no error thrown

**Player Slot Persistence:** `GameBoard.preferredPlayerSlots` static map keeps players in same board positions across rounds - must be static/shared

**WebSocket Lifecycle:** `GameRoundWebsocket.onClose()` deletes round when last client disconnects - rounds are ephemeral per session count

**JWT Keystore Passwords:** Different per service - auth: "secret", player: "secret2", game: "secret" (not "secret" everywhere)

**MicroProfile REST Client URL:** Game→Player service URL set via env var `org_libertybikes_restclient_PlayerService_mp_rest_url`, not in code

**CORS in Game Service Only:** Only game-service has CORS config in server.xml - other services don't need it

**JNDI Game Config:** Game speed/map/cooldown configurable via JNDI in `game-service/server.xml` - not hardcoded

**Test Board Access:** Tests access `GameBoard.board` field directly (public) - not via getters

**Frontend Directory Requirement:** Angular commands MUST run from `frontend/prebuild/`, not root - npm scripts expect this

**Liberty Task Dependencies:** `libertyStart` depends on `libertyStop` and `test` - always stops existing server first, runs tests before starting

## Jakarta EE Migration Gotchas (Java 21 + Liberty 24+)

**javax.naming NOT Migrated:** `javax.naming.InitialContext` stays in Java SE - do NOT migrate to `jakarta.naming` (doesn't exist)

**javax.sql NOT Migrated:** `javax.sql.DataSource` stays in Java SE - do NOT migrate to `jakarta.sql` (JDBC drivers use javax)

**InitialContext API Change:** `InitialContext.doLookup()` removed - use `new InitialContext().lookup()` instead

**MicroProfile Metrics 5.0+ Changes:**
- `Metadata`, `MetadataBuilder`, `MetricType` removed
- `@Counted` no longer has `displayName` attribute
- `registry.counter(String name)` replaces `registry.counter(Metadata)`
- `registry.gauge(String, Supplier<T>)` replaces `registry.gauge(String, Gauge<T>)`

**JJWT 0.12.6 API Changes:**
- `Jwts.claims()` removed - use builder setters directly
- `signWith(SignatureAlgorithm, Key)` → `signWith(Key)` (auto-detects algorithm)
- Split into modules: jjwt-api, jjwt-impl, jjwt-jackson (all required)

**Twitter4j Version:** Must use 4.0.7 (not 4.1.x) - 4.1.x completely rewrote API, incompatible with existing code

**Google API Client 2.x:** Requires separate `google-http-client-jackson2` dependency for Jackson support

## Advanced Mode Capabilities

This mode has access to MCP tools and browser capabilities for enhanced development workflows.