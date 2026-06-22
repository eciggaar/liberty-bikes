# Games Played Metric Fix - 2026-06-22

## Problem
When playing two games in a row, the "Games played" metric in Grafana remained at 1 instead of incrementing to 2.

## Root Cause Analysis

**Two separate issues were found:**

### Issue 1: Metric Counter Not Incrementing
The `total_num_of_rounds` counter was being incremented in the wrong place and with the wrong approach:

1. **Original location**: Counter increment was in [`GameMetrics.incrementCurrentRounds()`](game-service/src/main/java/org/libertybikes/game/metric/GameMetrics.java:50), which is called from the `GameRound` constructor
2. **Problem**: `GameRound` is a POJO (not a CDI bean), and the constructor is called when round objects are created, not when games actually start
3. **Additional problem**: The `registry` field was null when accessed via CDI lookup because the `@Inject` hadn't completed yet

### Issue 2: Grafana Dashboard Querying Wrong Metric
The Grafana dashboard panel titled "Games played" was querying `current_num_of_rounds` (a gauge showing currently active rounds) instead of `total_num_of_rounds_total` (the counter showing total games played).

## Solution

### Part 1: Fix Metric Counter Increment

1. **Removed duplicate logic** from [`GameMetrics.incrementCurrentRounds()`](game-service/src/main/java/org/libertybikes/game/metric/GameMetrics.java:50-51)
   - This method now ONLY increments the gauge for current active rounds
   - Removed the code trying to increment the total counter

2. **Created dedicated method** [`GameMetrics.incrementTotalRounds()`](game-service/src/main/java/org/libertybikes/game/metric/GameMetrics.java:53-70)
   - Gets `MetricRegistry` directly from CDI with proper `@RegistryType` annotation
   - Increments the `total_num_of_rounds` counter
   - Called from [`GameRound.Starter.run()`](game-service/src/main/java/org/libertybikes/game/core/GameRound.java:606) when game transitions to RUNNING state

### Part 2: Fix Grafana Dashboard Query

Updated [`grafanaDashboardConfig.json:414`](monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json:414) to query the correct metric:
- **Before**: `current_num_of_rounds{mp_scope="application"}`
- **After**: `total_num_of_rounds_total{mp_scope="application"}`

## Why This Works

- **Gauges** track current state → updated in constructor/destructor
- **Counters** track cumulative events → updated when event occurs (game starts)
- By getting the registry directly from CDI with the proper annotation, we avoid the null registry issue
- By querying the counter instead of the gauge, Grafana shows cumulative games played

## Changes Made

1. [`GameMetrics.java:50-51`](game-service/src/main/java/org/libertybikes/game/metric/GameMetrics.java:50-51) - Simplified `incrementCurrentRounds()` to only update gauge
2. [`GameMetrics.java:53-70`](game-service/src/main/java/org/libertybikes/game/metric/GameMetrics.java:53-70) - Added `incrementTotalRounds()` method with proper CDI registry lookup
3. [`GameRound.java:606`](game-service/src/main/java/org/libertybikes/game/core/GameRound.java:606) - Call `incrementTotalRounds()` when game starts
4. [`grafanaDashboardConfig.json:414`](monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json:414) - Updated Grafana query to use correct metric

## Testing Results
- Metric endpoint shows counter incrementing: 1.0 → 2.0 after two games ✓
- Grafana dashboard now queries the correct metric ✓