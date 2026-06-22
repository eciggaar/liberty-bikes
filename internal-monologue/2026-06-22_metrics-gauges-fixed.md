# Metrics Gauges Fixed - 2026-06-22

## Problem
Gauge metrics for current players, rounds, parties, and queued players were not appearing in the `/metrics?scope=application` endpoint or Grafana dashboard, even though counter metrics were working.

## Root Cause
The `@Gauge` annotations in MicroProfile Metrics 5.x were not being processed. The GameMetrics `@ApplicationScoped` bean was not being eagerly initialized, so its `@PostConstruct` method (which registers gauges programmatically) was never called.

## Solution
1. **Programmatic Gauge Registration**: Changed from `@Gauge` annotations to programmatic registration in `@PostConstruct` using `registry.gauge(name, supplier)`
2. **Force Bean Initialization**: Modified PartyService's `@PostConstruct` to call `gameMetrics.toString()`, which triggers CDI to instantiate the GameMetrics bean and run its `@PostConstruct` method

## Changes Made

### GameMetrics.java
- Added `@PostConstruct` method `init()` that programmatically registers gauges using `registry.gauge()`
- Used method references (e.g., `currentPlayers::get`) as Supplier arguments
- Removed non-functional `@Gauge` annotations

### PartyService.java  
- Modified `createSingletonParty()` to call `gameMetrics.toString()` to force bean initialization

### Frontend
- Fixed null pointer error in `login.component.ts` where `roundID` could be null before calling `.toUpperCase()`

## Verification
```bash
curl http://localhost:9080/metrics?scope=application
```

Now shows:
- `current_num_of_players` (gauge)
- `current_num_of_rounds` (gauge)
- `current_number_of_parties` (gauge) 
- `current_num_of_players_in_queue` (gauge)
- `number_of_parties_total` (counter)
- `rate_of_websocket_calls_total` (counter)

## Grafana Dashboard
The dashboard queries like `application_current_num_of_players_current` should now work, as MicroProfile Metrics 5.x automatically adds `_current` suffix to gauge metrics in Prometheus format.

## Key Learnings
- MicroProfile Metrics 5.x `@Gauge` annotations don't work reliably - use programmatic registration
- `@ApplicationScoped` beans are lazy-loaded - must be explicitly accessed to trigger `@PostConstruct`
- Injecting a bean as a field doesn't instantiate it - must call a method on it
- Use `registry.gauge(name, supplier)` with method references for clean gauge registration