
# Liberty Bikes Migration Plan: Java 21 + MicroProfile 7 + Liberty 26 + Angular 21

## Executive Summary

This document outlines a comprehensive migration plan for the Liberty Bikes application using a **phased hybrid approach**:
- **Phase 1**: Backend migration (Java 8→21, MicroProfile 3.0→7.0, Liberty 19→26)
- **Phase 2**: Frontend migration (Angular 10→21)

**Current State:**
- Java: 1.8
- Angular: 10.0.4
- TypeScript: 3.9.7
- MicroProfile: 3.0
- Jakarta EE: 8.0.0
- Open Liberty: 19.0.0.9
- Gradle: 5.6.2
- Liberty Gradle Plugin: 2.6.5

**Target State:**
- Java: 21 LTS
- Angular: 21
- TypeScript: 5.7+
- MicroProfile: 7.0
- Jakarta EE: 11.0
- Open Liberty: 26.0.0.x
- Gradle: 8.12+
- Liberty Gradle Plugin: 3.x

**Timeline:** 8-12 weeks (Phase 1: 6-8 weeks, Phase 2: 2-4 weeks)

---

## Table of Contents

1. [Current State Analysis](#1-current-state-analysis)
2. [Migration Strategy Overview](#2-migration-strategy-overview)
3. [Phase 1: Backend Migration](#3-phase-1-backend-migration)
4. [Phase 2: Frontend Migration](#4-phase-2-frontend-migration)
5. [Conflict Analysis](#5-conflict-analysis)
6. [Deprecated Features & Modernization](#6-deprecated-features--modernization)
7. [Comprehensive Test Plan](#7-comprehensive-test-plan)
8. [Timeline & Phases](#8-timeline--phases)
9. [Rollback Procedures](#9-rollback-procedures)
10. [Environment Considerations](#10-environment-considerations)
11. [Risk Assessment](#11-risk-assessment)
12. [Documentation Updates](#12-documentation-updates)

---

## 1. Current State Analysis

### 1.1 Technology Stack Inventory

**Backend Services (3 microservices):**
- `auth-service`: Authentication via GitHub, Google, Twitter OAuth
- `player-service`: Player data management with PostgreSQL fallback
- `game-service`: WebSocket-based game logic with AI players

**Key Dependencies:**
```
Java 8 (sourceCompatibility = 1.8)
├── MicroProfile 3.0
│   ├── mpConfig-1.3
│   ├── mpFaultTolerance-2.0
│   ├── mpMetrics-2.0
│   ├── mpOpenAPI-1.1
│   ├── mpRestClient-1.3
│   └── mpJwt-1.1
├── Jakarta EE 8.0.0
│   ├── jaxrs-2.1
│   ├── cdi-2.0
│   ├── jsonb-1.0
│   ├── websocket-1.1
│   └── appSecurity-3.0
├── Open Liberty 19.0.0.9
├── JJWT 0.9.1 (deprecated)
├── PostgreSQL Driver 42.2.8
└── JUnit 4.12
```

**Frontend:**
```
Angular 10.0.4
├── TypeScript 3.9.7
├── RxJS 6.6.0
├── Bootstrap 4.5.0
├── ng-bootstrap 7.0.0
└── Zone.js 0.10.3
```

**Build Tools:**
- Gradle 5.6.2
- Liberty Gradle Plugin 2.6.5
- Angular CLI 10.0.3

### 1.2 Codebase Characteristics

**Java Code Patterns:**
- Uses `javax.*` namespace (pre-Jakarta EE 9)
- JAX-RS endpoints with `@Path`, `@GET`, `@POST`
- CDI with `@Inject`, `@ApplicationScoped`
- WebSocket with `@ServerEndpoint`
- MicroProfile Config with `@ConfigProperty`
- JWT authentication with custom keystore management
- SSE (Server-Sent Events) for party queue

**Angular Code Patterns:**
- Component-based architecture
- Services with dependency injection
- RxJS observables for async operations
- WebSocket communication via native API
- Bootstrap for UI components
- TSLint for code quality (deprecated)

### 1.3 Critical Dependencies

**External Libraries:**
- `io.jsonwebtoken:jjwt:0.9.1` - **CRITICAL**: Deprecated, must migrate to 0.12.x
- `org.postgresql:postgresql:42.2.8` - Update to 42.7.x
- `com.google.api-client:google-api-client:1.30.4` - Update to 2.x
- `org.twitter4j:twitter4j-core:4.0.7` - May need replacement

---

## 2. Migration Strategy Overview

### 2.1 Phased Hybrid Approach

**Phase 1: Backend Migration (6-8 weeks)**
- Migrate Java 8 → 21
- Migrate MicroProfile 3.0 → 7.0
- Migrate Liberty 19 → 26
- Update Jakarta EE 8 → 11 (namespace change: `javax.*` → `jakarta.*`)
- Modernize build system (Gradle 5.6.2 → 8.12+)
- Update all backend dependencies

**Phase 2: Frontend Migration (2-4 weeks)**
- Migrate Angular 10 → 21 (incremental: 10→12→13→15→17→18→19→21)
- Update TypeScript 3.9.7 → 5.7+
- Replace deprecated tools (TSLint → ESLint)
- Update all frontend dependencies

### 2.2 Why Phased Hybrid?

**Advantages:**
1. **Backend stability first**: Ensures API contracts remain stable during frontend migration
2. **Reduced complexity**: Isolates backend and frontend concerns
3. **Easier testing**: Can validate backend changes independently
4. **Lower risk**: Frontend can continue working with migrated backend
5. **Parallel work possible**: Different teams can work on each phase

**Considerations:**
- Frontend must remain compatible with migrated backend during Phase 1
- API contracts must not break during backend migration
- WebSocket protocol must remain stable

---

## 3. Phase 1: Backend Migration

### 3.1 Java 8 → 21 Migration

#### 3.1.1 Major Changes Across Versions

**Java 9 (Modules)**
- Module system introduced (not required for this project)
- JDK internal APIs restricted
- Deprecation of some APIs

**Java 11 LTS**
- Removal of Java EE modules (already using Jakarta EE)
- `var` keyword for local variables
- New String methods
- HTTP Client API

**Java 17 LTS**
- Sealed classes
- Pattern matching for `instanceof`
- Records
- Text blocks
- Switch expressions

**Java 21 LTS**
- Virtual threads (Project Loom)
- Pattern matching for switch
- Record patterns
- Sequenced collections
- String templates (preview)

#### 3.1.2 Required Code Changes

**1. Update Gradle Configuration**

File: [`build.gradle`](build.gradle)
```gradle
// BEFORE
sourceCompatibility = 1.8

// AFTER
sourceCompatibility = 21
targetCompatibility = 21

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

**2. No Direct Code Changes Required**

The Liberty Bikes codebase uses standard Java 8 features that are fully compatible with Java 21:
- Lambda expressions ✓
- Stream API ✓
- Optional ✓
- Date/Time API ✓

**3. Potential Modernization Opportunities** (Optional, preserves functionality)

```java
// BEFORE (Java 8)
Map<String, String> claims = new HashMap<>();
claims.put("id", playerId);
claims.put("name", playerName);

// AFTER (Java 21 - using Map.of for immutable maps)
Map<String, String> claims = Map.of(
    "id", playerId,
    "name", playerName
);

// BEFORE (Java 8)
if (player instanceof AIPlayer) {
    AIPlayer aiPlayer = (AIPlayer) player;
    aiPlayer.makeMove();
}

// AFTER (Java 21 - pattern matching)
if (player instanceof AIPlayer aiPlayer) {
    aiPlayer.makeMove();
}

// BEFORE (Java 8)
String query = "SELECT * FROM players " +
               "WHERE id = ? " +
               "AND status = ?";

// AFTER (Java 21 - text blocks)
String query = """
    SELECT * FROM players
    WHERE id = ?
    AND status = ?
    """;
```

### 3.2 Jakarta EE 8 → 11 Migration

#### 3.2.1 Namespace Change: `javax.*` → `jakarta.*`

**Critical Breaking Change**: All `javax.*` imports must change to `jakarta.*`

**Affected Packages:**
- `javax.enterprise.*` → `jakarta.enterprise.*`
- `javax.inject.*` → `jakarta.inject.*`
- `javax.ws.rs.*` → `jakarta.ws.rs.*`
- `javax.websocket.*` → `jakarta.websocket.*`
- `javax.json.*` → `jakarta.json.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.annotation.*` → `jakarta.annotation.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.naming.*` → `jakarta.naming.*`
- `javax.sql.*` → `jakarta.sql.*`

**Migration Strategy:**
1. Use automated tools: Eclipse Transformer or OpenRewrite
2. Manual find-replace across all Java files
3. Update imports in 38 affected files (identified in search)

**Example Changes:**

File: [`auth-service/src/main/java/org/libertybikes/auth/service/AuthTypes.java`](auth-service/src/main/java/org/libertybikes/auth/service/AuthTypes.java)
```java
// BEFORE
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

// AFTER
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
```

#### 3.2.2 Update Dependencies

File: [`build.gradle`](build.gradle)
```gradle
// BEFORE
dependencies {
    providedCompile group: 'org.eclipse.microprofile', name: 'microprofile', version: '3.0'
    providedCompile group: 'jakarta.platform', name: 'jakarta.jakartaee-api', version: '8.0.0'
    testCompile group: 'junit', name: 'junit', version: '4.12'
    testCompile group: 'org.eclipse', name: 'yasson', version: '1.0.5'
    libertyRuntime group: 'io.openliberty', name: 'openliberty-runtime', version: '19.0.0.9'
}

// AFTER
dependencies {
    providedCompile group: 'org.eclipse.microprofile', name: 'microprofile', version: '7.0'
    providedCompile group: 'jakarta.platform', name: 'jakarta.jakartaee-api', version: '11.0.0'
    testImplementation group: 'org.junit.jupiter', name: 'junit-jupiter', version: '5.11.0'
    testImplementation group: 'org.eclipse', name: 'yasson', version: '3.0.4'
    libertyRuntime group: 'io.openliberty', name: 'openliberty-runtime', version: '26.0.0.1'
}
```

### 3.3 MicroProfile 3.0 → 7.0 Migration

#### 3.3.1 Feature Version Updates

**Liberty server.xml Changes:**

File: [`auth-service/src/main/liberty/config/server.xml`](auth-service/src/main/liberty/config/server.xml)
```xml
<!-- BEFORE -->
<featureManager>
    <feature>appSecurity-3.0</feature>
    <feature>beanValidation-2.0</feature>
    <feature>cdi-2.0</feature>
    <feature>jaxrs-2.1</feature>
    <feature>jndi-1.0</feature>
    <feature>jsonb-1.0</feature>
    <feature>mpConfig-1.3</feature>
    <feature>mpJwt-1.1</feature>
    <feature>mpRestClient-1.3</feature>
    <feature>mpOpenAPI-1.1</feature>
    <feature>mpMetrics-2.0</feature>
</featureManager>

<!-- AFTER (Liberty 26 with MicroProfile 7.0) -->
<featureManager>
    <feature>appSecurity-6.0</feature>
    <feature>beanValidation-3.1</feature>
    <feature>cdi-4.1</feature>
    <feature>restfulWS-4.0</feature>
    <feature>jndi-1.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpConfig-3.1</feature>
    <feature>mpJwt-2.1</feature>
    <feature>mpRestClient-4.0</feature>
    <feature>mpOpenAPI-4.0</feature>
    <feature>mpMetrics-5.1</feature>
    <feature>mpTelemetry-2.0</feature> <!-- New: replaces mpOpenTracing -->
</featureManager>
```

File: [`game-service/src/main/liberty/config/server.xml`](game-service/src/main/liberty/config/server.xml)
```xml
<!-- BEFORE -->
<featureManager>
    <feature>appSecurity-3.0</feature>
    <feature>cdi-2.0</feature>
    <feature>concurrent-1.0</feature>
    <feature>jaxrs-2.1</feature>
    <feature>jndi-1.0</feature>
    <feature>jsonb-1.0</feature>
    <feature>mpConfig-1.3</feature>
    <feature>mpFaultTolerance-2.0</feature>
    <feature>mpMetrics-2.0</feature>
    <feature>mpOpenAPI-1.1</feature>
    <feature>mpRestClient-1.3</feature>
    <feature>websocket-1.1</feature>
</featureManager>

<!-- AFTER -->
<featureManager>
    <feature>appSecurity-6.0</feature>
    <feature>cdi-4.1</feature>
    <feature>concurrent-3.1</feature>
    <feature>restfulWS-4.0</feature>
    <feature>jndi-1.0</feature>
    <feature>jsonb-3.0</feature>
    <feature>mpConfig-3.1</feature>
    <feature>mpFaultTolerance-4.1</feature>
    <feature>mpMetrics-5.1</feature>
    <feature>mpOpenAPI-4.0</feature>
    <feature>mpRestClient-4.0</feature>
    <feature>websocket-2.2</feature>
</featureManager>
```

#### 3.3.2 MicroProfile API Changes

**MicroProfile Config 1.3 → 3.1**
- No breaking changes for basic usage
- New features: Config profiles, property expressions
- `@ConfigProperty` remains compatible

**MicroProfile JWT 1.1 → 2.1**
- Updated to use Jakarta EE namespaces
- JWT claims remain compatible
- Keystore configuration unchanged

**MicroProfile Rest Client 1.3 → 4.0**
- Updated to Jakarta REST (JAX-RS 4.0)
- `@RestClient` injection remains compatible
- URL configuration via `@ConfigProperty` unchanged

**MicroProfile OpenAPI 1.1 → 4.0**
- Updated to Jakarta REST
- Annotations remain compatible
- UI endpoint unchanged

**MicroProfile Metrics 2.0 → 5.1**
- Micrometer-based implementation
- Basic metrics remain compatible
- Custom metrics may need updates

**MicroProfile Fault Tolerance 2.0 → 4.1**
- `@Retry`, `@Timeout`, `@CircuitBreaker` remain compatible
- Updated to Jakarta CDI

#### 3.3.3 Code Changes Required

**Minimal changes needed** - Most MicroProfile APIs are backward compatible after namespace change.

Example: [`game-service/src/main/java/org/libertybikes/game/round/service/PartyService.java`](game-service/src/main/java/org/libertybikes/game/round/service/PartyService.java)
```java
// BEFORE
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

// AFTER
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

// No other changes needed - MicroProfile annotations remain the same
```

### 3.4 Critical Dependency Updates

#### 3.4.1 JJWT Library Migration

**CRITICAL**: `io.jsonwebtoken:jjwt:0.9.1` is deprecated and incompatible with Java 21.

File: [`auth-service/build.gradle`](auth-service/build.gradle), [`game-service/build.gradle`](game-service/build.gradle), [`player-service/build.gradle`](player-service/build.gradle)
```gradle
// BEFORE
dependencies {
    compile group: 'io.jsonwebtoken', name: 'jjwt', version: '0.9.1'
}

// AFTER
dependencies {
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
}
```

**Code Changes Required:**

File: [`auth-service/src/main/java/org/libertybikes/auth/service/JwtAuth.java`](auth-service/src/main/java/org/libertybikes/auth/service/JwtAuth.java)
```java
// BEFORE
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

String jwt = Jwts.builder()
    .setClaims(claims)
    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
    .signWith(SignatureAlgorithm.RS256, signingKey)
    .compact();

// AFTER
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

String jwt = Jwts.builder()
    .claims(claims)
    .expiration(new Date(System.currentTimeMillis() + 86400000))
    .signWith(signingKey, Jwts.SIG.RS256)
    .compact();
```

#### 3.4.2 PostgreSQL Driver Update

File: [`player-service/build.gradle`](player-service/build.gradle)
```gradle
// BEFORE
dependencies {
    postgresql group: 'org.postgresql', name: 'postgresql', version: '42.2.8'
}

// AFTER
dependencies {
    postgresql group: 'org.postgresql', name: 'postgresql', version: '42.7.4'
}
```

**No code changes required** - Driver is backward compatible.

#### 3.4.3 Google API Client Update

File: [`auth-service/build.gradle`](auth-service/build.gradle)
```gradle
// BEFORE
dependencies {
    compile group: 'com.google.api-client', name: 'google-api-client', version: '1.30.4'
}

// AFTER
dependencies {
    implementation group: 'com.google.api-client', name: 'google-api-client', version: '2.7.0'
}
```

**Potential code changes** - Review Google OAuth implementation for API changes.

### 3.5 Build System Updates

#### 3.5.1 Gradle Migration

File: [`gradle/wrapper/gradle-wrapper.properties`](gradle/wrapper/gradle-wrapper.properties)
```properties
# BEFORE
distributionUrl=https\://services.gradle.org/distributions/gradle-5.6.2-bin.zip

# AFTER
distributionUrl=https\://services.gradle.org/distributions/gradle-8.12-bin.zip
```

File: [`build.gradle`](build.gradle)
```gradle
// BEFORE
buildscript {
    dependencies {
        classpath 'net.wasdev.wlp.gradle.plugins:liberty-gradle-plugin:2.6.5'
    }
}

plugins {
    id 'com.avast.gradle.docker-compose' version "0.9.1"
}

// AFTER
plugins {
    id 'io.openliberty.tools.gradle.Liberty' version '3.8.2'
    id 'com.avast.gradle.docker-compose' version '0.17.10'
}

// Update dependency configurations
dependencies {
    // Replace 'compile' with 'implementation'
    // Replace 'testCompile' with 'testImplementation'
    // Replace 'providedCompile' with 'compileOnly'
}
```

#### 3.5.2 JUnit 4 → 5 Migration

File: [`build.gradle`](build.gradle)
```gradle
// BEFORE
dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.12'
}

// AFTER
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.0'
}

test {
    useJUnitPlatform()
}
```

**Test Code Changes:**

File: [`game-service/src/test/java/org/libertybikes/game/core/GameBoardTest.java`](game-service/src/test/java/org/libertybikes/game/core/GameBoardTest.java)
```java
// BEFORE
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameBoardTest {
    @Before
    public void setup() { }
    
    @Test
    public void testSomething() { }
}

// AFTER
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {
    @BeforeEach
    void setup() { }
    
    @Test
    void testSomething() { }
}
```

### 3.6 Docker Configuration Updates

File: [`docker-compose.yml`](docker-compose.yml)
```yaml
# BEFORE
version: '2.1'
services:
  postgres:
    image: postgres:11-alpine

# AFTER
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
```

---

## 4. Phase 2: Frontend Migration

### 4.1 Angular 10 → 21 Migration Strategy

**Incremental Migration Path:**
Angular 10 → 12 → 13 → 15 → 17 → 18 → 19 → 21

**Why Incremental?**
- Angular requires sequential major version upgrades
- Each version has breaking changes
- `ng update` handles most migrations automatically

### 4.2 Migration Steps

#### 4.2.1 Angular 10 → 12

```bash
cd frontend/prebuild
ng update @angular/core@12 @angular/cli@12 --force
ng update @angular/material@12 --force
npm install
```

**Key Changes:**
- View Engine removed, Ivy is default
- `@angular/localize` required for i18n
- Stricter TypeScript checks

#### 4.2.2 Angular 12 → 13

```bash
ng update @angular/core@13 @angular/cli@13 --force
```

**Key Changes:**
- View Engine completely removed
- IE11 support dropped
- RxJS 7.4+ required

#### 4.2.3 Angular 13 → 15

```bash
ng update @angular/core@15 @angular/cli@15 --force
```

**Key Changes:**
- Standalone components introduced (optional)
- `@angular/router` improvements
- TypeScript 4.8+ required

#### 4.2.4 Angular 15 → 17

```bash
ng update @angular/core@17 @angular/cli@17 --force
```

**Key Changes:**
- Signals introduced
- Control flow syntax (`@if`, `@for`)
- Deferrable views

#### 4.2.5 Angular 17 → 18

```bash
ng update @angular/core@18 @angular/cli@18 --force
```

**Key Changes:**
- Zoneless change detection (experimental)
- Material 3 components
- Enhanced server-side rendering

#### 4.2.6 Angular 18 → 19

```bash
ng update @angular/core@19 @angular/cli@19 --force
```

**Key Changes:**
- Improved hydration
- Standalone APIs stabilized

#### 4.2.7 Angular 19 → 21

```bash
ng update @angular/core@21 @angular/cli@21 --force
```

**Key Changes:**
- Latest features and optimizations
- TypeScript 5.7+ required

### 4.3 TypeScript Migration

File: [`frontend/prebuild/tsconfig.json`](frontend/prebuild/tsconfig.json)
```json
// BEFORE
{
  "compilerOptions": {
    "target": "es2015",
    "module": "esnext",
    "lib": ["es2017", "dom"]
  }
}

// AFTER
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ES2022",
    "lib": ["ES2023", "dom"],
    "useDefineForClassFields": false,
    "strict": true
  }
}
```

### 4.4 Replace TSLint with ESLint

**Remove TSLint:**
```bash
npm uninstall tslint codelyzer
```

**Install ESLint:**
```bash
ng add @angular-eslint/schematics
```

File: `.eslintrc.json` (new)
```json
{
  "root": true,
  "overrides": [
    {
      "files": ["*.ts"],
      "extends": [
        "eslint:recommended",
        "plugin:@typescript-eslint/recommended",
        "plugin:@angular-eslint/recommended",
        "plugin:@angular-eslint/template/process-inline-templates"
      ]
    },
    {
      "files": ["*.html"],
      "extends": ["plugin:@angular-eslint/template/recommended"]
    }
  ]
}
```

### 4.5 Update Dependencies

File: [`frontend/prebuild/package.json`](frontend/prebuild/package.json)
```json
{
  "dependencies": {
    "@angular/animations": "^21.0.0",
    "@angular/common": "^21.0.0",
    "@angular/compiler": "^21.0.0",
    "@angular/core": "^21.0.0",
    "@angular/forms": "^21.0.0",
    "@angular/platform-browser": "^21.0.0",
    "@angular/platform-browser-dynamic": "^21.0.0",
    "@angular/router": "^21.0.0",
    "@ng-bootstrap/ng-bootstrap": "^17.0.0",
    "bootstrap": "^5.3.3",
    "rxjs": "^7.8.1",
    "tslib": "^2.8.1",
    "zone.js": "~0.15.0"
  },
  "devDependencies": {
    "@angular-devkit/build-angular": "^21.0.0",
    "@angular/cli": "^21.0.0",
    "@angular/compiler-cli": "^21.0.0",
    "@angular-eslint/builder": "^18.0.0",
    "@angular-eslint/eslint-plugin": "^18.0.0",
    "@angular-eslint/eslint-plugin-template": "^18.0.0",
    "@angular-eslint/schematics": "^18.0.0",
    "@angular-eslint/template-parser": "^18.0.0",
    "@typescript-eslint/eslint-plugin": "^8.0.0",
    "@typescript-eslint/parser": "^8.0.0",
    "eslint": "^9.0.0",
    "jasmine-core": "~5.4.0",
    "karma": "~6.4.4",
    "karma-chrome-launcher": "~3.2.0",
    "karma-coverage": "~2.2.1",
    "karma-jasmine": "~5.1.0",
    "karma-jasmine-html-reporter": "~2.1.0",
    "typescript": "~5.7.2"
  }
}
```

### 4.6 Bootstrap 4 → 5 Migration

**Breaking Changes:**
- jQuery removed (already not used in Angular)
- Utility classes renamed
- Form controls redesigned

File: [`frontend/prebuild/src/styles.scss`](frontend/prebuild/src/styles.scss)
```scss
/* Update Bootstrap import */
@import "~bootstrap/scss/bootstrap";

/* Update utility classes */
/* BEFORE: .ml-3, .mr-3 */
/* AFTER: .ms-3, .me-3 */

/* BEFORE: .float-left, .float-right */
/* AFTER: .float-start, .float-end */
```

### 4.7 Angular Component Updates

**No major code changes required** for basic components, but consider modernization:

File: [`frontend/prebuild/src/app/game/game.component.ts`](frontend/prebuild/src/app/game/game.component.ts)
```typescript
// BEFORE (Angular 10)
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  constructor(private gameService: GameService) { }
  
  ngOnInit() {
    this.gameService.connect();
  }
}

// AFTER (Angular 21 - Optional modernization with signals)
import { Component, OnInit, signal } from '@angular/core';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  gameState = signal<GameState | null>(null);
  
  constructor(private gameService: GameService) { }
  
  ngOnInit() {
    this.gameService.connect();
  }
}
```

### 4.8 Update Angular CLI Configuration

File: [`frontend/prebuild/angular.json`](frontend/prebuild/angular.json)
```json
{
  "projects": {
    "frontend": {
      "architect": {
        "build": {
          "builder": "@angular-devkit/build-angular:application",
          "options": {
            "outputPath": "dist",
            "index": "src/index.html",
            "browser": "src/main.ts",
            "polyfills": ["zone.js"],
            "tsConfig": "tsconfig.app.json",
            "assets": ["src/assets", "src/favicon.ico"],
            "styles": ["src/styles.scss"],
            "scripts": []
          }
        }
      }
    }
  }
}
```

---

## 5. Conflict Analysis

### 5.1 Java 21 + MicroProfile 7 + Liberty 26 Compatibility

**✅ COMPATIBLE**
- Java 21 is fully supported by Liberty 26
- MicroProfile 7.0 requires Jakarta EE 11, which Liberty 26 supports
- All MicroProfile 7.0 specifications are implemented in Liberty 26

**Verified Compatibility Matrix:**
```
Liberty 26.0.0.x
├── Java 21 ✓
├── Jakarta EE 11 ✓
├── MicroProfile 7.0 ✓
│   ├── Config 3.1 ✓
│   ├── Fault Tolerance 4.1 ✓
│   ├── Health 4.0 ✓
│   ├── Metrics 5.1 ✓
│   ├── JWT 2.1 ✓
│   ├── OpenAPI 4.0 ✓
│   ├── Rest Client 4.0 ✓
│   └── Telemetry 2.0 ✓
└── WebSocket 2.2 ✓
```

### 5.2 Angular 21 + TypeScript 5.7 Compatibility

**✅ COMPATIBLE**
- Angular 21 requires TypeScript 5.6-5.7
- All Angular 21 dependencies support TypeScript 5.7

### 5.3 Cross-Stack Compatibility

**Backend ↔ Frontend Communication:**
- REST API: No changes to HTTP protocol
- WebSocket: Protocol remains compatible
- JSON serialization: No breaking changes
- JWT tokens: Format remains compatible (after JJWT update)

**✅ NO CONFLICTS IDENTIFIED**

### 5.4 Transitive Dependency Conflicts

**Potential Conflicts:**

1. **Jackson versions** (JSON processing)
   - Liberty 26 uses Jackson 2.17.x
   - JJWT 0.12.6 uses Jackson 2.15.x
   - **Resolution**: Use Liberty's Jackson version (compatible)

2. **SLF4J versions** (Logging)
   - Liberty 26 uses SLF4J 2.0.x
   - Some libraries may use 1.7.x
   - **Resolution**: Exclude transitive SLF4J dependencies

3. **Servlet API**
   - Liberty 26 uses Jakarta Servlet 6.1
   - Ensure no `javax.servlet` dependencies remain
   - **Resolution**: Use Jakarta namespace exclusively

**Gradle Dependency Resolution:**
```gradle
configurations.all {
    resolutionStrategy {
        force 'com.fasterxml.jackson.core:jackson-databind:2.17.2'
        force 'org.slf4j:slf4j-api:2.0.16'
    }
    
    exclude group: 'javax.servlet', module: 'javax.servlet-api'
}
```

---

## 6. Deprecated Features & Modernization

### 6.1 Deprecated Features in Current Codebase

#### 6.1.1 Java/Jakarta EE

**1. `javax.*` namespace** → `jakarta.*`
- **Status**: CRITICAL - Must change
- **Impact**: All 38 Java files with `javax.*` imports
- **Action**: Automated find-replace or Eclipse Transformer

**2. JJWT 0.9.1** → 0.12.6
- **Status**: CRITICAL - Security and compatibility
- **Impact**: JWT creation and validation in auth-service
- **Action**: Update API calls (see Section 3.4.1)

**3. JUnit 4** → JUnit 5
- **Status**: Recommended
- **Impact**: Test files in all services
- **Action**: Update annotations and assertions

**4. Gradle `compile` configuration** → `implementation`
- **Status**: Required for Gradle 8
- **Impact**: All build.gradle files
- **Action**: Replace deprecated configurations

#### 6.1.2 Angular/Frontend

**1. TSLint** → ESLint
- **Status**: CRITICAL - TSLint is deprecated
- **Impact**: Code quality checks
- **Action**: Migrate to ESLint (see Section 4.4)

**2. Protractor** → Cypress/Playwright
- **Status**: Recommended - Protractor is deprecated
- **Impact**: E2E tests
- **Action**: Migrate to modern E2E framework

**3. `ng-bootstrap` 7.0** → 17.0
- **Status**: Required for Angular 21
- **Impact**: Bootstrap components
- **Action**: Update and test components

**4. RxJS 6.6** → 7.8
- **Status**: Required for Angular 21
- **Impact**: Observable patterns
- **Action**: Update operators (mostly automatic)

### 6.2 Modernization Opportunities

#### 6.2.1 Java Modernizations (Preserve Functionality)

**1. Pattern Matching for instanceof**
```java
// BEFORE
if (player instanceof AIPlayer) {
    AIPlayer ai = (AIPlayer) player;
    ai.makeMove();
}

// AFTER
if (player instanceof AIPlayer ai) {
    ai.makeMove();
}
```

**2. Text Blocks for Multi-line Strings**
```java
// BEFORE
String sql = "SELECT p.id, p.name, p.status " +
             "FROM players p " +
             "WHERE p.active = true " +
             "ORDER BY p.score DESC";

// AFTER
String sql = """
    SELECT p.id, p.name, p.status
    FROM players p
    WHERE p.active = true
    ORDER BY p.score DESC
    """;
```

**3. Switch Expressions**
```java
// BEFORE
String status;
switch (player.getState()) {
    case ALIVE:
        status = "active";
        break;
    case DEAD:
        status = "eliminated";
        break;
    default:
        status = "unknown";
}

// AFTER
String status = switch (player.getState()) {
    case ALIVE -> "active";
    case DEAD -> "eliminated";
    default -> "unknown";
};
```

**4. Records for DTOs**
```java
// BEFORE
public class PlayerStats {
    private final String id;
    private final int wins;
    private final int losses;
    
    public PlayerStats(String id, int wins, int losses) {
        this.id = id;
        this.wins = wins;
        this.losses = losses;
    }
    
    // getters, equals, hashCode, toString
}

// AFTER
public record PlayerStats(String id, int wins, int losses) { }
```

**5. Virtual Threads for Concurrent Operations**
```java
// BEFORE
ExecutorService executor = Executors.newFixedThreadPool(10);

// AFTER (Java 21)
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

#### 6.2.2 Angular Modernizations (Preserve Functionality)

**1. Standalone Components** (Optional)
```typescript
// BEFORE
@NgModule({
  declarations: [GameComponent],
  imports: [CommonModule]
})
export class GameModule { }

// AFTER
@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game.component.html'
})
export class GameComponent { }
```

**2. Signals for State Management** (Optional)
```typescript
// BEFORE
export class GameComponent {
  players: Player[] = [];
  
  updatePlayers(newPlayers: Player[]) {
    this.players = newPlayers;
  }
}

// AFTER
export class GameComponent {
  players = signal<Player[]>([]);
  
  updatePlayers(newPlayers: Player[]) {
    this.players.set(newPlayers);
  }
}
```

**3. Control Flow Syntax** (Optional)
```html
<!-- BEFORE -->
<div *ngIf="player">
  <span *ngFor="let item of items">{{ item }}</span>
</div>

<!-- AFTER -->
@if (player) {
  <div>
    @for (item of items; track item.id) {
      <span>{{ item }}</span>
    }
  </div>
}
```

### 6.3 Modernization Priority Matrix

| Feature | Priority | Risk | Effort | Benefit |
|---------|----------|------|--------|---------|
| `javax.*` → `jakarta.*` | CRITICAL | Low | Medium | Required |
| JJWT 0.9.1 → 0.12.6 | CRITICAL | Medium | Low | Security |
| JUnit 4 → 5 | HIGH | Low | Low | Modern testing |
| TSLint → ESLint | CRITICAL | Low | Low | Required |
| Pattern matching | MEDIUM | Low | Low | Readability |
| Text blocks | LOW | Low | Low | Readability |
| Switch expressions | LOW | Low | Low | Conciseness |
| Records | MEDIUM | Low | Medium | Immutability |
| Virtual threads | LOW | Medium | Medium | Performance |
| Standalone components | LOW | Low | High | Optional |
| Signals | LOW | Low | High | Optional |
| Control flow syntax | LOW | Low | Medium | Optional |

**Recommendation**: Focus on CRITICAL and HIGH priority items. LOW priority modernizations are optional and can be done incrementally post-migration.

---

## 7. Comprehensive Test Plan

### 7.1 Testing Strategy

**Test Pyramid:**
```
        /\
       /E2E\
      /------\
     /Integration\
    /------------\
   /    Unit      \
  /----------------\
```

**Coverage Goals:**
- Unit tests: 80%+ code coverage
- Integration tests: All API endpoints
- E2E tests: Critical user workflows
- Regression tests: All existing functionality
- Performance tests: No degradation
- Security tests: Authentication/authorization

### 7.2 Phase 1: Backend Testing

#### 7.2.1 Unit Testing

**Java Unit Tests (JUnit 5):**

File: `game-service/src/test/java/org/libertybikes/game/core/GameBoardTest.java`
```java
@Test
void testPlayerMovement() {
    GameBoard board = new GameBoard();
    Player player = new Player("test-id", "TestPlayer", DIRECTION.UP);
    board.addPlayer(player);
    
    board.movePlayer(player, DIRECTION.RIGHT);
    
    assertEquals(DIRECTION.RIGHT, player.direction);
}

@Test
void testCollisionDetection() {
    GameBoard board = new GameBoard();
    Player p1 = new Player("p1", "Player1", DIRECTION.UP);
    Player p2 = new Player("p2", "Player2", DIRECTION.DOWN);
    
    board.addPlayer(p1);
    board.addPlayer(p2);
    
    // Simulate collision
    p1.x = 100;
    p1.y = 100;
    p2.x = 100;
    p2.y = 100;
    
    assertTrue(board.checkCollision(p1, p2));
}
```

**Test Coverage Requirements:**
- `auth-service`: JWT generation, OAuth callbacks, token validation
- `player-service`: CRUD operations, ranking calculations, database fallback
- `game-service`: Game logic, collision detection, WebSocket handling, AI players

#### 7.2.2 Integration Testing

**REST API Tests:**

```java
@Test
void testPlayerCreation() {
    Response response = given()
        .contentType("application/json")
        .body("""
            {
                "id": "test-player",
                "name": "Test Player"
            }
            """)
        .when()
        .post("/player-service/players")
        .then()
        .statusCode(201)
        .extract().response();
    
    assertEquals("test-player", response.jsonPath().getString("id"));
}

@Test
void testAuthenticationFlow() {
    // Test GitHub OAuth callback
    Response response = given()
        .queryParam("code", "test-code")
        .when()
        .get("/auth-service/github/callback")
        .then()
        .statusCode(302)
        .extract().response();
    
    assertNotNull(response.getCookie("jwt"));
}
```

**MicroProfile Tests:**

```java
@Test
void testRestClientCommunication() {
    // Test game-service calling player-service
    PlayerService playerService = RestClientBuilder.newBuilder()
        .baseUrl(new URL("http://localhost:8081"))
        .build(PlayerService.class);
    
    Player player = playerService.getPlayer("test-id");
    assertNotNull(player);
}

@Test
void testFaultTolerance() {
    // Test circuit breaker
    for (int i = 0; i < 10; i++) {
        try {
            playerService.getPlayer("invalid-id");
        } catch (Exception e) {
            // Expected
        }
    }
    
    // Circuit should be open now
    assertThrows(CircuitBreakerOpenException.class, 
        () -> playerService.getPlayer("test-id"));
}
```

**WebSocket Tests:**

```java
@Test
void testWebSocketConnection() throws Exception {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    Session session = container.connectToServer(
        TestWebSocketClient.class,
        URI.create("ws://localhost:8080/game-service/round/test-round")
    );
    
    assertTrue(session.isOpen());
    
    // Send message
    session.getBasicRemote().sendText("""
        {
            "direction": "UP"
        }
        """);
    
    // Verify response
    String response = TestWebSocketClient.getLastMessage();
    assertNotNull(response);
}
```

#### 7.2.3 Database Testing

**PostgreSQL Integration:**

```java
@Test
void testDatabaseConnection() {
    DataSource ds = InitialContext.doLookup("jdbc/postgresql");
    assertNotNull(ds);
    
    try (Connection conn = ds.getConnection()) {
        assertTrue(conn.isValid(5));
    }
}

@Test
void testPlayerPersistence() {
    Player player = new Player("db-test", "DB Test Player");
    playerDB.create(player);
    
    Player retrieved = playerDB.read("db-test");
    assertEquals("DB Test Player", retrieved.getName());
}

@Test
void testDatabaseFallback() {
    // Simulate database unavailable
    System.setProperty("DB_HOST", "invalid-host");
    
    PlayerDB db = new PlayerDBProducer().getPlayerDB();
    assertTrue(db instanceof InMemPlayerDB);
}
```

### 7.3 Phase 2: Frontend Testing

#### 7.3.1 Unit Testing (Jasmine/Karma)

**Component Tests:**

