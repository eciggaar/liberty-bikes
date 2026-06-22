# Player Actions Metric Fix

## Issue
The Grafana dashboard panel "Player actions/sec (last 1 min)" was not updating during gameplay.

## Root Cause
The dashboard was querying a non-existent metric: `application_rate_of_websocket_calls_one_min_rate_per_second`

Investigation revealed:
1. The `@Counted` annotation in `GameRoundWebsocket.onMessage()` creates a counter: `rate_of_websocket_calls_total`
2. MicroProfile Metrics 5.x no longer auto-generates rate metrics from counters (this was a feature in older versions)
3. The metric exists in Prometheus as `rate_of_websocket_calls_total` (confirmed via Prometheus API)

## Solution
Updated the Grafana dashboard query to use PromQL's `rate()` function to calculate the per-second rate over a 1-minute window:

**Before:** `application_rate_of_websocket_calls_one_min_rate_per_second`
**After:** `rate(rate_of_websocket_calls_total[1m])`

This calculates the per-second average rate of WebSocket calls over the last minute, which is exactly what the panel title indicates.

## Why Not Add to Application Scope?
The `@Counted` annotation uses the default (base) scope, which is correct. The metric is properly exposed and scraped by Prometheus. The issue was purely in the dashboard query expecting an auto-generated rate metric that no longer exists in MicroProfile Metrics 5.x.

## Files Modified
- `monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json` - Updated metric query to use PromQL rate() function