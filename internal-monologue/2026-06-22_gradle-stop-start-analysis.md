# Gradle Stop/Start Process Analysis

## Issue Identified

**Problem:** Running `./gradlew clean` triggers unnecessary builds during the stop process.

## Root Cause

In `build.gradle` line 106:
```gradle
clean.dependsOn 'libertyStop'
```

This means when you run `./gradlew clean`, it:
1. Stops Liberty servers (correct)
2. Deletes build artifacts (correct)

However, line 14-16 shows:
```gradle
task clean(type: Delete) {
  subprojects.each { dependsOn("${it.name}:clean") };
  delete 'build'
}
```

This cascades to all subprojects, and each subproject's `clean` task depends on `libertyStop`.

## The Real Problem

Line 108 is the culprit:
```gradle
libertyStart.dependsOn 'libertyStop', 'test'
```

When you run `./gradlew <service>:libertyStart`, it:
1. Stops the server (correct)
2. **Runs all tests** (unnecessary for simple restart)
3. Starts the server

The `test` task implicitly depends on `build` tasks (compile, processResources, etc.), so starting a server triggers a full build cycle.

## Impact

- `./gradlew game-service:libertyStart` → stops server, compiles code, runs tests, starts server
- `./gradlew game-service:libertyStop` → just stops server (correct)
- `./gradlew clean` → stops all servers, deletes build artifacts (correct)

## Recommendation

**Option 1 (Conservative):** Remove `test` dependency from `libertyStart`
- Pro: Faster restarts, no unnecessary builds
- Con: Tests won't run automatically before starting

**Option 2 (Separate tasks):** Create `libertyRestart` without test dependency
- Pro: Keep existing behavior, add fast restart option
- Con: More tasks to remember

**Option 3 (Current behavior is intentional):** Keep as-is if tests should always run before starting
- Pro: Ensures code quality before deployment
- Con: Slow restarts during development

## Verdict

The current behavior appears **intentional but overly cautious**. Running tests before every server start is good for CI/CD but painful for local development. Option 2 (separate restart task) provides best of both worlds.