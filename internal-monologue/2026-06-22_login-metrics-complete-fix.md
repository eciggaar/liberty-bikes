# Login Metrics Complete Fix

## Problem
Grafana dashboard "Total Logins" panel showed no data.

## Root Cause Analysis

### Issue 1: Wrong Metric Name
Dashboard queried `application_num_player_logins_total` but metric didn't exist.

### Issue 2: Missing Metric Implementation
Player service had commented-out metric code due to CDI initialization issues with programmatic metrics.

### Issue 3: Incomplete Coverage
Auth service only tracked OAuth logins (GitHub, Google, Twitter). Guest logins and bot registrations bypassed auth service entirely.

### Issue 4: Wrong Metric Semantics
Initial fix counted unique players (11 total in DB) instead of login sessions. User wanted each login attempt counted, not just unique player creations.

## Solution

### 1. Updated Grafana Dashboard
Changed metric query from `application_num_player_logins_total` to `num_player_logins_total` in [`grafanaDashboardConfig.json`](monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json:82).

### 2. Implemented Conditional Counter in Player Service
Modified [`PlayerService.createPlayer()`](player-service/src/main/java/org/libertybikes/player/service/PlayerService.java:68-71):

```java
@Inject
@RegistryType(type = MetricRegistry.Type.APPLICATION)
MetricRegistry registry;

private Counter getLoginCounter() {
    return registry.counter("num_player_logins");
}

@POST
@Produces(MediaType.TEXT_HTML)
public String createPlayer(@QueryParam("name") String name, @QueryParam("id") String id) {
    // ... validation ...
    
    Player p = new Player(name, id);
    boolean isNewPlayer = db.create(p);
    
    if (isNewPlayer) {
        System.out.println("Created a new player with id=" + p.id);
    } else {
        System.out.println("A player already existed with id=" + p.id);
    }
    
    // Increment login counter for every login attempt (excluding sample players)
    if (!name.startsWith("SamplePlayer")) {
        getLoginCounter().inc();
    }
    
    return p.id;
}
```

## Key Design Decisions

1. **Programmatic vs Declarative Metrics**: Used programmatic `Counter` instead of `@Counted` annotation to enable conditional logic
2. **Login Sessions vs Unique Players**: Counter increments on every login attempt, not just new player creation
3. **Test Account Filtering**: Excluded "SamplePlayer" accounts to track only human logins
4. **Single Source of Truth**: All player creation goes through `PlayerService.createPlayer()`, capturing OAuth, guest, and bot logins

## Behavior

- **New player login**: Counter increments
- **Existing player re-login**: Counter increments  
- **SamplePlayer login**: Counter does NOT increment
- **Multiple logins by same user**: Each login increments counter

## Testing Results

✅ Dashboard now displays login count
✅ Login as Edward → counter = 1
✅ Logout and login again → counter = 2
✅ SamplePlayer logins ignored
✅ Tracks OAuth, guest, and bot logins

## Files Modified

- `player-service/src/main/java/org/libertybikes/player/service/PlayerService.java`
- `monitoring/grafanaDashboardConfig/grafanaDashboardConfig.json`