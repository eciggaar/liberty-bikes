# Complete Fix Summary - Player Actions Metric & Game Startup Issues

## Issues Fixed

### 1. Player Actions Metric Not Updating
**Problem:** Grafana dashboard panel "Player actions/sec (last 1 min)" was not updating during gameplay.

**Root Cause:** Dashboard queried non-existent metric `application_rate_of_websocket_calls_one_min_rate_per_second`. MicroProfile Metrics 5.x no longer auto-generates rate metrics from `@Counted` annotations.

**Solution:** Updated Grafana dashboard to use PromQL `rate()` function:
- **File:** `monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json` line 249
- **Change:** `rate(rate_of_websocket_calls_total[1m])`

### 2. Game Startup 404 Errors After Inactivity
**Problem:** After an hour of inactivity, clicking "PLAY NOW" resulted in:
- 404 for `/round/available`
- 404 for `/party/describe`
- "Player already exists" errors

**Root Causes:**
1. Frontend called `/round/available` but game service has `singleParty=true` mode, which blocks that endpoint
2. Game service missing `beans.xml` for CDI discovery, preventing PartyService from being registered

**Solutions:**

a) **Frontend API Endpoint Fix:**
- **File:** `frontend/prebuild/src/app/login/login.component.ts` line 201
- **Change:** Use `${environment.API_URL_PARTY}/available` instead of `${environment.API_URL_GAME_ROUND}/available`
- Frontend rebuilt and restarted

b) **CDI Discovery Fix:**
- **File:** `game-service/src/main/webapp/WEB-INF/beans.xml` (created)
- **Content:** Jakarta EE 10 beans.xml with `bean-discovery-mode="all"`
- Game service rebuilt and restarted

## Files Modified
1. `monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json` - Fixed metric query
2. `frontend/prebuild/src/app/login/login.component.ts` - Changed to party API
3. `game-service/src/main/webapp/WEB-INF/beans.xml` - Created for CDI discovery

## Verification
- `/party/describe` now returns: `{"isSingleParty":true,"partyId":"VRXV"}`
- Player actions metric will update when WebSocket messages are received during gameplay
- Game can be started after inactivity periods