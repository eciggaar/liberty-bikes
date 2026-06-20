# Test Hanging Root Cause Analysis - 2026-06-19

## Problem
Frontend unit tests were hanging indefinitely with no error output.

## Root Causes (CONFIRMED via DevTools)

### 1. Bootstrap JavaScript Requiring jQuery
**Error:** `Uncaught TypeError: Bootstrap's JavaScript requires jQuery`
**Location:** `angular.json` line 76-78 in test configuration
**Cause:** Test config loaded `bootstrap.min.js` but jQuery wasn't available in test environment
**Fix:** Removed Bootstrap from test scripts array

### 2. webpack require.context Not Available
**Error:** `Uncaught TypeError: __webpack_require__(...).context is not a function`
**Location:** `src/test.ts` line 24
**Cause:** Angular 22 no longer supports webpack's `require.context` API in test environment
**Fix:** Removed webpack-specific test discovery code - Angular 22 handles this automatically

## Evidence from DevTools Console

```
Uncaught TypeError: Bootstrap's JavaScript requires jQuery. jQuery must be included before Bootstrap's JavaScript.
    at Object.jQueryDetection (:9876/webpack:/node_modules/bootstrap/dist/js/bootstrap.min.js:6:2524)

Uncaught TypeError: __webpack_require__(...).context is not a function
    at Module.7832 (:9876/webpack:/src/test.ts:24:25)
```

## Fixes Applied

### Fix 1: Remove Bootstrap from Tests
```json
// angular.json - test.options.scripts
"scripts": []  // Was: ["node_modules/bootstrap/dist/js/bootstrap.min.js"]
```

### Fix 2: Simplify test.ts
```typescript
// Removed:
// declare const __karma__: any;
// declare const require: any;
// __karma__.loaded = function () {};
// const context = require.context('./', true, /\.spec\.ts$/);
// context.keys().map(context);
// __karma__.start();

// Angular 22 handles test discovery automatically
```

## Result

**Before:** Tests hung indefinitely, no output, browser disconnected after 30s

**After:** Tests run successfully!
- 13 tests executed
- 2 passing
- 11 failing (with clear error messages)
- Total time: 0.187 seconds

## Remaining Test Failures

Now we have actual test failures to fix:

1. **Missing SocketService provider** (9 failures)
   - GameService, PlayerlistComponent, PlayersService need SocketService mock

2. **Missing animations provider** (1 failure)
   - LeaderboardComponent needs `provideAnimations()` or `provideNoopAnimations()`

3. **Undefined player data** (1 failure)
   - PlayerComponent test needs mock player data

These are normal test setup issues, not blocking problems.

## Key Learnings

1. **Always use non-headless Chrome first** for debugging - DevTools console shows actual errors
2. **Bootstrap in tests is problematic** - UI libraries with DOM manipulation cause issues
3. **Angular 22 changed test bootstrapping** - No more manual `require.context` needed
4. **webpack APIs don't work in Angular 22 tests** - Framework handles module loading

## Credit

Thanks to Claude's systematic debugging approach:
1. Check version alignment (zone.js, @angular/core, @angular/cli)
2. Run with non-headless Chrome to see DevTools
3. Look for TestBed configuration issues
4. Check for import-time errors

This led directly to finding the root causes.