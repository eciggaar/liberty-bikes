# Player Re-registration Fix

## Issue
After playing one game and clicking "Start Game" again, users were redirected to the home page where their name was still displayed. However, clicking "PLAY NOW" resulted in the error:
```
Received error message from server: Unable to add player BASIC:Edward to game. This is probably because the player has not been registered yet
```

## Root Cause
The player service uses in-memory storage (`InMemPlayerDB`). When:
1. A player finishes a game and returns to the home page
2. Their `userId` and `username` remain in sessionStorage
3. They click "PLAY NOW" to join a new game
4. The frontend sends the stored `userId` to the game service
5. The game service queries the player service via `getPlayer(userId)`
6. The player doesn't exist (in-memory DB cleared or service restarted)
7. Error thrown: "Unable to add player... has not been registered yet"

## Solution
Added `ensurePlayerExists()` method to [`LoginComponent`](frontend/prebuild/src/app/login/login.component.ts:477) that:
1. Checks if player credentials exist in sessionStorage
2. Verifies the player exists in the player service
3. If player doesn't exist, re-registers them with their stored credentials
4. Handles errors gracefully with user feedback

This method is called before:
- [`quickJoin()`](frontend/prebuild/src/app/login/login.component.ts:192) - when clicking "PLAY NOW"
- [`joinParty()`](frontend/prebuild/src/app/login/login.component.ts:203) - when joining a party

## Changes Made
- Modified [`frontend/prebuild/src/app/login/login.component.ts`](frontend/prebuild/src/app/login/login.component.ts)
  - Added `ensurePlayerExists()` method (lines 477-509)
  - Updated `quickJoin()` to call `ensurePlayerExists()` (line 195)
  - Updated `joinParty()` to call `ensurePlayerExists()` (line 206)

## Testing
Frontend rebuilt and restarted successfully. The fix ensures players can rejoin games even after:
- Service restarts
- In-memory database clears
- Session persistence across page refreshes