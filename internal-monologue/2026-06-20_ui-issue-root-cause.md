# UI Issue Root Cause Analysis - 2026-06-20

## Problem
Player list and leaderboard components not displaying data despite backend working correctly.

## Investigation Results

### WebSocket Messages - ✅ Working
HAR file analysis shows:
- WebSocket connection established successfully
- 230 messages exchanged during gameplay
- Messages contain correct `playerlist` data:
```json
{
  "playerlist": [
    {"id": "BASIC:Edward", "name": "Edward", "color": "#ABD155", "status": "Connected", ...},
    {"id": "", "name": "Bot Player", "color": "#6FC3DF", ...},
    ...
  ]
}
```

### Ranking Endpoint - ✅ Working
Direct curl test shows correct data:
```bash
curl http://localhost:8081/rank?limit=10
# Returns Edward with 3 games, 971 rating + 9 sample players
```

### Frontend Architecture
1. **SocketService**: Creates WebSocket, wraps in RxJS Subject
2. **PlayersService**: Maps WebSocket messages to JSON objects
3. **PlayerListComponent**: Subscribes to PlayersService.messages, looks for `playerlist` property
4. **LeaderboardComponent**: HTTP GET to ranking endpoint on init + 12s timer

## Root Cause Hypothesis

### Issue 1: PlayerListComponent Not Receiving Data
The component is declared in `game.component.html` as `<app-player-list></app-player-list>`, which creates a NEW instance with its own PlayersService provider (declared in component decorator).

**Problem**: Each component instance gets its own PlayersService instance, but the SocketService is likely a singleton. The PlayersService constructor subscribes to the socket ONCE when created, but if the socket isn't initialized yet, the subscription might miss messages.

**Angular 22 Change**: Stricter dependency injection and component lifecycle. The `providers: [PlayersService]` in PlayerListComponent creates a new instance per component, not shared.

### Issue 2: LeaderboardComponent Not Displaying
The component makes HTTP request on ngOnInit and sets up 12s timer. Data is received (verified via curl), but not displayed.

**Likely cause**: Angular change detection not triggering after async data arrives, OR the component template binding issue.

## Solution Approach
1. Check if SocketService is properly provided at root level
2. Verify PlayersService subscription timing
3. Check Angular change detection in LeaderboardComponent
4. Consider using `ChangeDetectorRef.detectChanges()` if needed