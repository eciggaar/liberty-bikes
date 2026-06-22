# Metrics Issue Analysis - 2026-06-22

## Problem
Grafana dashboard shows zeros for player metrics despite games being played. Only annotation-based metrics (`@Counted`) work, but programmatic gauge registration fails.

## Root Cause
MicroProfile Metrics 5.x changed the API for programmatic metric registration:
1. `CDI.current().select(MetricRegistry.class).get()` fails with NullPointerException because it requires `@RegistryType` qualifier
2. `@Gauge` annotations on instance methods in `@ApplicationScoped` beans should work, but the bean must be instantiated

## Attempted Solutions
1. ❌ Programmatic gauge registration with `registry.gauge(name, supplier)` - API signature mismatch
2. ❌ Using `CDI.current()` without qualifier - NullPointerException  
3. ❌ `@ApplicationScoped` bean with `@Gauge` methods - bean not instantiated
4. ✅ Injected `GameMetrics` into `PartyService` to force instantiation - **should work but gauges still not appearing**

## Current Status
- `@Counted` metrics work fine (Party, WebSocket)
- `@Gauge` annotations defined but not registered
- GameMetrics bean is injected into PartyService but gauges don't appear

## Next Steps
Need to verify:
1. Are `@Gauge` methods being called/registered?
2. Is there a CDI proxy issue preventing gauge registration?
3. Do we need `@Startup` or eager initialization?

## Alternative Approach
Consider using MicroProfile Metrics programmatic API correctly with proper `@RegistryType` injection or switch to pure annotation-based approach with a dedicated metrics bean.