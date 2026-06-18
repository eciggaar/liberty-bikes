# AGENTS.md Creation Summary

## Analysis Completed

Analyzed Liberty Bikes codebase - a microservices game application with:
- **Stack:** Java/Jakarta EE, Open Liberty, MicroProfile, Angular 10, Gradle
- **Services:** auth-service, player-service, game-service, frontend
- **Architecture:** WebSocket-based multiplayer game with JWT auth, PostgreSQL (optional)

## Key Non-Obvious Discoveries

1. **Shared Liberty Install:** All services share single Liberty at `build/wlp/` via `liberty.install.baseDir = rootProject.buildDir`
2. **Frontend Build Chain:** Angular in `frontend/prebuild/` → Gradle → `frontend/src/main/webapp/` → WAR
3. **Database Fallback:** Player service silently falls back to in-memory if PostgreSQL unavailable
4. **Player Slot Persistence:** Static `preferredPlayerSlots` map maintains positions across rounds
5. **WebSocket Lifecycle:** Rounds auto-delete when last client disconnects
6. **JWT Keystore Passwords:** Different per service (auth: "secret", player: "secret2", game: "secret")
7. **CORS Only in Game Service:** Only game-service has CORS config
8. **JNDI Game Config:** Speed/map/cooldown configurable via JNDI in server.xml
9. **Frontend Directory Requirement:** Angular commands MUST run from `frontend/prebuild/`
10. **Liberty Task Dependencies:** `libertyStart` depends on `libertyStop` and `test`

## Files Created

- `AGENTS.md` - Main project guidance (73 lines)
- `.bob/rules-code/AGENTS.md` - Code mode specific rules (29 lines)
- `.bob/rules-advanced/AGENTS.md` - Advanced mode rules with MCP access (33 lines)
- `.bob/rules-ask/AGENTS.md` - Documentation context for Ask mode (33 lines)
- `.bob/rules-plan/AGENTS.md` - Architectural constraints for Plan mode (37 lines)

All files focus exclusively on non-obvious, project-specific information discovered through code analysis.