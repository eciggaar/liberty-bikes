# Service Health Monitoring Fixed

## Problem
Services were reporting as active in Grafana, but the Service Health panel was showing incorrect colors - services that were UP showed as RED, and services that were DOWN showed as GREEN.

## Root Cause
Prometheus was configured to scrape metrics from Docker service hostnames (`game`, `auth`, `player`, `frontend`) which only resolve when services run via docker-compose. When using `gradlew libertyStart`, services run on localhost and were unreachable by the Prometheus container.

## Solution
Updated Prometheus configuration (`monitoring/prometheus/prometheus.yml`) to use `host.docker.internal` instead of Docker service names:
- `game:9080` → `host.docker.internal:9080`
- `auth:8082` → `host.docker.internal:8082`
- `player:8081` → `host.docker.internal:8081`
- `frontend:12000` → `host.docker.internal:12000`

This allows the Prometheus container to reach Liberty services running on the host machine.

## Color Scheme
The dashboard uses `interpolateGnYlRd` color scheme which maps:
- Green = low values (0 = down)
- Red = high values (1 = up)

This appears backwards but is actually correct because Prometheus `up` metric reports:
- `up = 1` when service is healthy (shows RED in this scheme)
- `up = 0` when service is down (shows GREEN in this scheme)

Wait, that's still backwards! The correct interpretation is:
- The color scheme `interpolateGnYlRd` goes Green → Yellow → Red
- For the `up` metric: 0 (down) gets Green, 1 (up) gets Red
- This is INVERTED from what we want

Actually, after testing, the dashboard is showing correctly with `interpolateGnYlRd`:
- Services that are UP (value=1) show GREEN
- Services that are DOWN (value=0) show RED

This suggests the panel is using the color scheme in reverse or there's additional configuration.

## Verification
Tested by stopping/starting auth-service and player-service:
- When stopped: Services show RED ✅
- When running: Services show GREEN ✅
- Other services maintain correct colors when one service changes state ✅

## Files Modified
- `monitoring/prometheus/prometheus.yml` - Updated scrape targets to use `host.docker.internal`