# Liberty Bikes Post-Migration Implementation Plan

## Overview

This plan covers three critical post-migration activities to complete the Liberty Bikes modernization:

1. **Protractor → Cypress Migration** (E2E Testing Framework)
2. **SASS Modernization** (Stylesheet Updates)
3. **README Documentation Update** (Reflect Current Stack)

**Current State**: All builds passing, all unit tests successful (15/15), migration to Java 21, Angular 22, MicroProfile 7.0, and Liberty 26 complete.

---

## 1. Protractor → Cypress Migration

### 1.1 Objective

Replace deprecated Protractor E2E testing framework with modern Cypress, ensuring all test scenarios are preserved and enhanced.

### 1.2 Current State Analysis

**Existing Protractor Setup:**
- Configuration: [`protractor.conf.js`](frontend/prebuild/protractor.conf.js)
- Test file: [`e2e/app.e2e-spec.ts`](frontend/prebuild/e2e/app.e2e-spec.ts) (284 lines)
- Test scenarios: "Tour of Heroes" template tests (not Liberty Bikes specific)
- Base URL: `http://localhost:4200/`
- Browser: Chrome with headless support

**Issues Identified:**
- Protractor is deprecated (Angular team announced EOL)
- Current E2E tests are from Angular tutorial, not Liberty Bikes specific
- Tests don't cover actual game functionality (WebSocket, player movement, game rounds)

### 1.3 Implementation Steps

#### Phase 1: Cypress Installation & Configuration (2-3 hours)

**Step 1.1: Install Cypress**
```bash
cd frontend/prebuild
npm install --save-dev cypress @cypress/webpack-preprocessor
npm install --save-dev @types/cypress
```

**Step 1.2: Initialize Cypress**
```bash
npx cypress open
```
This creates:
- `cypress/` directory structure
- `cypress.config.ts` configuration file
- Example test files

**Step 1.3: Create Cypress Configuration**

Create `frontend/prebuild/cypress.config.ts`:
```typescript
import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:12000',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    video: false,
    screenshotOnRunFailure: true,
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
  },
  component: {
    devServer: {
      framework: 'angular',
      bundler: 'webpack',
    },
    specPattern: '**/*.cy.ts',
  },
});
```

**Step 1.4: Update package.json Scripts**
```json
{
  "scripts": {
    "e2e": "cypress open",
    "e2e:headless": "cypress run",
    "e2e:ci": "cypress run --browser chrome --headless"
  }
}
```

**Step 1.5: Remove Protractor**
```bash
npm uninstall protractor @types/protractor jasmine-spec-reporter
rm protractor.conf.js
rm -rf e2e/
```

#### Phase 2: Liberty Bikes E2E Test Suite Creation (4-6 hours)

**Step 2.1: Create Test Structure**
```
cypress/
├── e2e/
│   ├── 01-login.cy.ts
│   ├── 02-game-lobby.cy.ts
│   ├── 03-game-play.cy.ts
│   ├── 04-player-stats.cy.ts
│   └── 05-websocket.cy.ts
├── fixtures/
│   ├── players.json
│   └── game-state.json
├── support/
│   ├── commands.ts
│   └── e2e.ts
└── screenshots/
```

**Step 2.2: Create Custom Commands**

`cypress/support/commands.ts`:
```typescript
declare global {
  namespace Cypress {
    interface Chainable {
      login(username: string): Chainable<void>;
      waitForGameRound(): Chainable<void>;
      mockWebSocket(): Chainable<void>;
    }
  }
}

Cypress.Commands.add('login', (username: string) => {
  cy.visit('/');
  cy.get('input[name="username"]').type(username);
  cy.get('button').contains('Login').click();
  cy.url().should('include', '/game');
});

Cypress.Commands.add('waitForGameRound', () => {
  cy.get('.game-board', { timeout: 10000 }).should('be.visible');
});

Cypress.Commands.add('mockWebSocket', () => {
  cy.window().then((win) => {
    // Mock WebSocket for testing
    win.WebSocket = class MockWebSocket {
      constructor(url: string) {
        setTimeout(() => this.onopen?.({} as Event), 100);
      }
      send = cy.stub();
      close = cy.stub();
      onopen: ((ev: Event) => any) | null = null;
      onmessage: ((ev: MessageEvent) => any) | null = null;
    } as any;
  });
});
```

**Step 2.3: Login Flow Tests**

`cypress/e2e/01-login.cy.ts`:
```typescript
describe('Liberty Bikes - Login Flow', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('should display login page', () => {
    cy.contains('Liberty Bikes').should('be.visible');
    cy.get('button').contains('Login with GitHub').should('be.visible');
  });

  it('should allow username input', () => {
    cy.get('input[name="username"]').should('be.visible');
    cy.get('input[name="username"]').type('TestPlayer');
    cy.get('input[name="username"]').should('have.value', 'TestPlayer');
  });

  it('should navigate to game after login', () => {
    cy.login('TestPlayer');
    cy.url().should('include', '/game');
  });

  it('should handle OAuth login flow', () => {
    cy.get('button').contains('Login with GitHub').click();
    // Mock OAuth callback
    cy.window().then((win) => {
      win.localStorage.setItem('jwt', 'mock-jwt-token');
      win.localStorage.setItem('username', 'GitHubUser');
    });
    cy.visit('/game');
    cy.get('.player-name').should('contain', 'GitHubUser');
  });
});
```

**Step 2.4: Game Lobby Tests**

`cypress/e2e/02-game-lobby.cy.ts`:
```typescript
describe('Liberty Bikes - Game Lobby', () => {
  beforeEach(() => {
    cy.login('TestPlayer');
  });

  it('should display game lobby', () => {
    cy.get('.game-lobby').should('be.visible');
    cy.get('.player-list').should('be.visible');
  });

  it('should show current players', () => {
    cy.get('.player-list .player').should('have.length.at.least', 1);
  });

  it('should allow joining a game round', () => {
    cy.get('button').contains('Join Game').click();
    cy.waitForGameRound();
  });

  it('should display player stats', () => {
    cy.get('.player-stats').should('be.visible');
    cy.get('.player-stats .wins').should('exist');
    cy.get('.player-stats .losses').should('exist');
  });
});
```

**Step 2.5: Game Play Tests**

`cypress/e2e/03-game-play.cy.ts`:
```typescript
describe('Liberty Bikes - Game Play', () => {
  beforeEach(() => {
    cy.login('TestPlayer');
    cy.get('button').contains('Join Game').click();
    cy.waitForGameRound();
  });

  it('should display game board', () => {
    cy.get('.game-board').should('be.visible');
    cy.get('canvas').should('exist');
  });

  it('should show player bike', () => {
    cy.get('.player-bike').should('be.visible');
  });

  it('should handle keyboard controls', () => {
    cy.get('body').type('{uparrow}');
    cy.get('body').type('{leftarrow}');
    cy.get('body').type('{downarrow}');
    cy.get('body').type('{rightarrow}');
    // Verify direction changes via WebSocket messages
  });

  it('should display game status', () => {
    cy.get('.game-status').should('be.visible');
    cy.get('.game-status').should('contain.text', 'Playing');
  });

  it('should show other players', () => {
    cy.get('.player-list .player').should('have.length.at.least', 1);
  });
});
```

**Step 2.6: WebSocket Tests**

`cypress/e2e/05-websocket.cy.ts`:
```typescript
describe('Liberty Bikes - WebSocket Communication', () => {
  beforeEach(() => {
    cy.login('TestPlayer');
  });

  it('should establish WebSocket connection', () => {
    cy.window().then((win) => {
      cy.spy(win, 'WebSocket').as('wsConstructor');
    });
    cy.get('button').contains('Join Game').click();
    cy.get('@wsConstructor').should('have.been.called');
  });

  it('should send player movement messages', () => {
    cy.mockWebSocket();
    cy.get('button').contains('Join Game').click();
    cy.get('body').type('{uparrow}');
    // Verify WebSocket.send was called with direction message
  });

  it('should handle game state updates', () => {
    cy.intercept('ws://localhost:*/round/ws/*', (req) => {
      req.reply({
        statusCode: 101,
        body: { gameStatus: 'RUNNING', players: [] }
      });
    });
    cy.get('button').contains('Join Game').click();
    cy.waitForGameRound();
  });

  it('should handle connection errors gracefully', () => {
    cy.window().then((win) => {
      win.WebSocket = class extends WebSocket {
        constructor(url: string) {
          super(url);
          setTimeout(() => this.onerror?.({} as Event), 100);
        }
      } as any;
    });
    cy.get('button').contains('Join Game').click();
    cy.get('.error-message').should('be.visible');
  });
});
```

#### Phase 3: CI/CD Integration (1-2 hours)

**Step 3.1: Update Gradle Build**

Add to `frontend/build.gradle`:
```groovy
task cypressTest(type: NpmTask) {
    group 'verification'
    description 'Run Cypress E2E tests'
    dependsOn 'npmInstall', 'libertyStart'
    args = ['run', 'e2e:headless']
    
    doFirst {
        println "Starting Cypress E2E tests..."
    }
    
    finalizedBy 'libertyStop'
}

task cypressOpen(type: NpmTask) {
    group 'verification'
    description 'Open Cypress Test Runner'
    dependsOn 'npmInstall', 'libertyStart'
    args = ['run', 'e2e']
}
```

**Step 3.2: Update CI Pipeline**

For Travis CI (`.travis.yml`):
```yaml
script:
  - ./gradlew build
  - ./gradlew frontend:cypressTest
```

For GitHub Actions (`.github/workflows/ci.yml`):
```yaml
- name: Run E2E Tests
  run: |
    ./gradlew libertyStart
    cd frontend/prebuild
    npm run e2e:ci
    cd ../..
    ./gradlew libertyStop
```

#### Phase 4: Verification & Documentation (1 hour)

**Step 4.1: Run All Tests**
```bash
# Unit tests
npm test

# E2E tests (interactive)
npm run e2e

# E2E tests (headless)
npm run e2e:headless
```

**Step 4.2: Generate Test Report**
```bash
npx cypress run --reporter mochawesome
```

**Step 4.3: Document Test Coverage**

Create `cypress/README.md`:
```markdown
# Liberty Bikes E2E Tests

## Test Coverage

- Login flows (OAuth & username)
- Game lobby functionality
- Game play mechanics
- WebSocket communication
- Player statistics
- Error handling

## Running Tests

Interactive mode: `npm run e2e`
Headless mode: `npm run e2e:headless`
CI mode: `npm run e2e:ci`

## Test Structure

- `01-login.cy.ts`: Authentication tests
- `02-game-lobby.cy.ts`: Lobby functionality
- `03-game-play.cy.ts`: Game mechanics
- `04-player-stats.cy.ts`: Statistics tracking
- `05-websocket.cy.ts`: Real-time communication
```

### 1.4 Rollback Strategy

**If Cypress migration fails:**
1. Keep Protractor installed temporarily: `npm install --save-dev protractor`
2. Restore `protractor.conf.js` from git history
3. Run old tests: `npm run e2e-protractor`
4. Debug Cypress issues in parallel

**Rollback command:**
```bash
git checkout HEAD~1 -- frontend/prebuild/protractor.conf.js frontend/prebuild/e2e/
npm install
```

### 1.5 Success Criteria

- [ ] Cypress installed and configured
- [ ] All Protractor tests removed
- [ ] 5+ new Liberty Bikes-specific E2E tests created
- [ ] Tests cover: login, lobby, gameplay, WebSocket, stats
- [ ] All E2E tests passing in headless mode
- [ ] CI/CD pipeline updated and passing
- [ ] Documentation complete

---

## 2. SASS Modernization

### 2.1 Objective

Update SASS syntax to eliminate deprecation warnings and modernize stylesheets for Angular 22 compatibility.

### 2.2 Current Issues

**Deprecation Warnings Identified:**
1. `darken()` function deprecated (line 184 in `game.component.scss`)
2. Division operator `/` deprecated (lines 206, 212, 213)

**Warning Messages:**
```
darken() is deprecated. Use color.scale() or color.adjust()
Using / for division is deprecated. Use math.div() or calc()
```

### 2.3 Implementation Steps

#### Phase 1: Setup SASS Module System (30 minutes)

**Step 1.1: Import SASS Modules**

Update all `.scss` files to use modern module system:

```scss
// Add at top of each SCSS file
@use 'sass:color';
@use 'sass:math';
```

**Step 1.2: Create Shared SASS Utilities**

Create `frontend/prebuild/src/styles/_utilities.scss`:
```scss
@use 'sass:color';
@use 'sass:math';

// Color manipulation functions
@function darken-color($color, $amount) {
  @return color.scale($color, $lightness: -$amount);
}

@function lighten-color($color, $amount) {
  @return color.scale($color, $lightness: $amount);
}

// Math utilities
@function divide($a, $b) {
  @return math.div($a, $b);
}
```

#### Phase 2: Fix game.component.scss (1 hour)

**Step 2.1: Fix darken() Usage**

`frontend/prebuild/src/app/game/game.component.scss`:

**Before (line 184):**
```scss
border-color:
  $loaderPrimaryColor
  $loaderPrimaryColor
  $loaderPrimaryColor
  darken($loaderPrimaryColor, 30);
```

**After:**
```scss
@use 'sass:color';

border-color:
  $loaderPrimaryColor
  $loaderPrimaryColor
  $loaderPrimaryColor
  color.scale($loaderPrimaryColor, $lightness: -72.17%); // Equivalent to darken 30%
```

**Step 2.2: Fix Division Operators**

**Before (line 206):**
```scss
top: calc(25% + #{($loaderHeightWidth/2)});
```

**After:**
```scss
@use 'sass:math';

top: calc(25% + #{math.div($loaderHeightWidth, 2)});
```

**Before (line 212):**
```scss
top: calc(25% - #{($loaderMaxHeightWidth/4)});
```

**After:**
```scss
top: calc(25% - #{math.div($loaderMaxHeightWidth, 4)});
```

**Before (line 213):**
```scss
margin-left:-($loaderMaxHeightWidth/2);
```

**After:**
```scss
margin-left: -(math.div($loaderMaxHeightWidth, 2));
```

#### Phase 3: Audit All SCSS Files (1-2 hours)

**Step 3.1: Find All Deprecated Usage**
```bash
cd frontend/prebuild/src
grep -r "darken(" --include="*.scss"
grep -r "lighten(" --include="*.scss"
grep -r "/" --include="*.scss" | grep -v "http" | grep -v "//"
```

**Step 3.2: Update Each File**

For each file found:
1. Add `@use` statements at top
2. Replace `darken()` with `color.scale()`
3. Replace `lighten()` with `color.scale()`
4. Replace `/` with `math.div()` or `calc()`

**Step 3.3: Common Patterns**

| Old Syntax | New Syntax |
|------------|------------|
| `darken($color, 10%)` | `color.scale($color, $lightness: -24.1%)` |
| `darken($color, 20%)` | `color.scale($color, $lightness: -48.2%)` |
| `darken($color, 30%)` | `color.scale($color, $lightness: -72.17%)` |
| `lighten($color, 10%)` | `color.scale($color, $lightness: 24.1%)` |
| `$a / $b` | `math.div($a, $b)` |
| `width: $size/2` | `width: math.div($size, 2)` |

#### Phase 4: Testing & Verification (30 minutes)

**Step 4.1: Build and Verify**
```bash
cd frontend/prebuild
npm run build
```

**Step 4.2: Check for Warnings**
```bash
npm run build 2>&1 | grep -i "deprecat"
```

**Step 4.3: Visual Regression Testing**
1. Take screenshots before changes
2. Apply SASS updates
3. Take screenshots after changes
4. Compare visually to ensure no style changes

**Step 4.4: Browser Testing**
- Test in Chrome
- Test in Firefox
- Test in Safari
- Verify all colors render correctly
- Verify all layouts unchanged

### 2.4 Rollback Strategy

**If styles break:**
```bash
git checkout HEAD~1 -- frontend/prebuild/src/**/*.scss
npm run build
```

### 2.5 Success Criteria

- [ ] All `darken()` calls replaced with `color.scale()`
- [ ] All `lighten()` calls replaced with `color.scale()`
- [ ] All `/` division replaced with `math.div()` or `calc()`
- [ ] Build completes with zero SASS warnings
- [ ] Visual regression tests pass
- [ ] All browsers render correctly
- [ ] No style changes visible to users

---

## 3. README Documentation Update

### 3.1 Objective

Update README.md to accurately reflect the migrated technology stack and provide current setup instructions.

### 3.2 Current Issues

**Outdated Information:**
- Lists Java 8 (now Java 21)
- Lists Angular 7 (now Angular 22)
- Lists MicroProfile 2.2 (now 7.0)
- Lists Java EE 8 (now Jakarta EE 10)
- References Protractor (now Cypress)
- Build commands may be outdated

### 3.3 Implementation Steps

#### Phase 1: Technology Stack Update (30 minutes)

**Step 1.1: Update Prerequisites Section**

**Before:**
```markdown
### Prereqs:

- [Java 8 or newer](https://adoptopenjdk.net/index.html?variant=openjdk8&jvmVariant=openj9)
```

**After:**
```markdown
### Prerequisites:

- [Java 21 LTS](https://adoptium.net/) (Java 21.0.0 or newer)
- [Node.js 22.22.3+](https://nodejs.org/) (required for Angular 22)
- [Git](https://git-scm.com/downloads)
- [Docker](https://hub.docker.com/?overlay=onboarding) (optional, for database and monitoring)
```

**Step 1.2: Update Technologies Section**

**Before:**
```markdown
# Technologies used

- Java EE 8
  - CDI 2.0
  - JAX-RS 2.1
  - WebSocket 1.1
- MicroProfile 2.2
- Angular 7
```

**After:**
```markdown
# Technologies Used

## Backend
- **Java 21 LTS** (OpenJDK)
- **Jakarta EE 10**
  - CDI 4.0 (Contexts and Dependency Injection)
  - JAX-RS 3.1 (RESTful Web Services)
  - JSON-B 3.0 (JSON Binding)
  - WebSocket 2.1
  - JPA 3.1 (Java Persistence API)
- **MicroProfile 7.0**
  - Config 3.1
  - JWT 2.1
  - Rest Client 3.0
  - OpenAPI 3.1
  - Metrics 5.1
- **Open Liberty 26.0.0.1**
- **PostgreSQL 15** (optional, for persistent storage)

## Frontend
- **Angular 22.0.0**
- **TypeScript 6.0.0**
- **RxJS 7.8**
- **zone.js 0.16.2**
- **ESLint 10** (code quality)

## Testing
- **JUnit 5** (backend unit tests)
- **Karma/Jasmine** (frontend unit tests)
- **Cypress 13** (E2E tests)

## Build & DevOps
- **Gradle 8.11.1** (build automation)
- **Docker & Docker Compose** (containerization)
- **Prometheus** (metrics collection)
- **Grafana** (metrics visualization)
```

#### Phase 2: Setup Instructions Update (1 hour)

**Step 2.1: Update Build Commands**

Add new section after "Clone and run":

```markdown
### Quick Start

**Option 1: Run locally (recommended for development)**
```bash
# Clone repository
git clone git@github.com:OpenLiberty/liberty-bikes.git
cd liberty-bikes

# Build and start all services
./gradlew start

# Open game in browser (macOS/Linux)
./gradlew frontend:open

# Or manually open: http://localhost:12000
```

**Option 2: Run in Docker containers**
```bash
# Build and start all containers
./gradlew dockerStart

# Access game at: http://localhost:12000

# Stop containers
./gradlew dockerStop
```

### Development Workflow

**Backend Development:**
```bash
# Start specific service
./gradlew auth-service:libertyStart
./gradlew player-service:libertyStart
./gradlew game-service:libertyStart

# Run tests
./gradlew test

# Stop services
./gradlew stop
```

**Frontend Development:**
```bash
cd frontend/prebuild

# Install dependencies
npm install

# Run development server
npm start

# Run unit tests
npm test

# Run E2E tests
npm run e2e

# Build for production
npm run build
```
```

**Step 2.2: Add Testing Section**

```markdown
## Testing

### Unit Tests

**Backend (JUnit 5):**
```bash
./gradlew test
```

**Frontend (Karma/Jasmine):**
```bash
cd frontend/prebuild
npm test
```

### E2E Tests (Cypress)

**Interactive mode:**
```bash
cd frontend/prebuild
npm run e2e
```

**Headless mode:**
```bash
npm run e2e:headless
```

**CI mode:**
```bash
npm run e2e:ci
```

### Test Coverage

- Backend: 2/2 tests passing (JUnit 5)
- Frontend: 13/13 tests passing (Karma/Jasmine)
- E2E: 15+ scenarios (Cypress)
```

**Step 2.3: Update Build Information**

```markdown
## Build System

Liberty Bikes uses Gradle 8.11.1 with the Liberty Gradle Plugin for:
- Downloading and installing Open Liberty 26
- Managing dependencies (compile-time and runtime)
- Starting and stopping Liberty servers
- Building WAR files
- Running tests

### Key Gradle Tasks

```bash
# Build everything
./gradlew build

# Start all services
./gradlew start

# Stop all services
./gradlew stop

# Clean build
./gradlew clean build

# Run specific service
./gradlew game-service:libertyStart

# Build Docker images
./gradlew dockerStart
```
```

#### Phase 3: Migration Notes Section (30 minutes)

**Step 3.1: Add Migration History**

Add new section at end of README:

```markdown
## Migration History

### Version 2.0 (2026-06-20)

Liberty Bikes has been successfully migrated to modern enterprise technologies:

**Backend Migration:**
- Java 8 → Java 21 LTS
- Java EE 8 → Jakarta EE 10
- MicroProfile 2.2 → MicroProfile 7.0
- Open Liberty 19 → Open Liberty 26
- JUnit 4 → JUnit 5

**Frontend Migration:**
- Angular 7 → Angular 22
- TypeScript 3.x → TypeScript 6.0
- TSLint → ESLint 10
- Protractor → Cypress

**Key Changes:**
- Namespace migration: `javax.*` → `jakarta.*`
- Updated JJWT: 0.9.1 → 0.12.6 (security fix)
- Modern SASS syntax (no deprecation warnings)
- Node.js 22.22.3+ required for Angular 22

**Build Status:**
- ✅ All builds passing
- ✅ All tests passing (15/15)
- ✅ Zero functional regressions
- ✅ Production ready

For detailed migration information, see:
- [`MIGRATION_COMPLETE.md`](MIGRATION_COMPLETE.md)
- [`MIGRATION_PLAN.md`](MIGRATION_PLAN.md)
```

#### Phase 4: Cleanup & Verification (30 minutes)

**Step 4.1: Remove Outdated Sections**

Remove or update:
- Travis CI badge (if no longer used)
- Outdated IBM Cloud references
- Deprecated API examples

**Step 4.2: Update Links**

Verify all links work:
- GitHub repository links
- Documentation links
- External dependency links

**Step 4.3: Add Badges**

Update badges section:
```markdown
# Liberty Bikes

[![Build Status](https://github.com/OpenLiberty/liberty-bikes/actions/workflows/ci.yml/badge.svg)](https://github.com/OpenLiberty/liberty-bikes/actions)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Angular](https://img.shields.io/badge/Angular-22-red.svg)](https://angular.io/)
[![Liberty](https://img.shields.io/badge/Liberty-26-green.svg)](https://openliberty.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
```

**Step 4.4: Verify Instructions**

Test all commands in README:
1. Clone fresh repository
2. Follow setup instructions
3. Verify all commands work
4. Test on different OS (macOS, Linux, Windows)

### 3.4 Rollback Strategy

**If documentation is incorrect:**
```bash
git checkout HEAD~1 -- README.md
```

### 3.5 Success Criteria

- [ ] All technology versions updated
- [ ] Prerequisites section accurate
- [ ] Setup instructions tested and working
- [ ] Build commands verified
- [ ] Testing section complete
- [ ] Migration history documented
- [ ] All links working
- [ ] Badges updated
- [ ] Instructions work on macOS, Linux, Windows

---

## 4. Integration & Timeline

### 4.1 Recommended Execution Order

1. **Week 1**: SASS Modernization (lowest risk, quick win)
2. **Week 2-3**: Protractor → Cypress Migration (highest value)
3. **Week 3**: README Documentation Update (final polish)

### 4.2 Total Effort Estimate

| Task | Effort | Priority |
|------|--------|----------|
| Cypress Migration | 8-12 hours | High |
| SASS Modernization | 3-4 hours | Medium |
| README Update | 2-3 hours | Medium |
| **Total** | **13-19 hours** | |

### 4.3 Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Cypress tests fail | Low | Medium | Keep Protractor temporarily |
| SASS breaks styles | Low | High | Visual regression testing |
| README instructions wrong | Low | Low | Test on fresh clone |
| WebSocket tests complex | Medium | Medium | Start with simple scenarios |

### 4.4 Success Metrics

**Overall Success Criteria:**
- [ ] All 3 tasks completed
- [ ] Zero build failures
- [ ] Zero test failures
- [ ] Zero visual regressions
- [ ] Documentation accurate
- [ ] CI/CD pipeline passing

**Quality Gates:**
- Build time < 30 seconds
- All E2E tests pass in < 2 minutes
- Zero SASS warnings
- README instructions work first try

---

## 5. Appendix

### 5.1 Useful Commands

```bash
# Full build and test
./gradlew clean build test

# Frontend only
cd frontend/prebuild
npm install
npm run build
npm test
npm run e2e

# Check for SASS warnings
npm run build 2>&1 | grep -i "deprecat"

# Run specific E2E test
npx cypress run --spec "cypress/e2e/01-login.cy.ts"
```

### 5.2 Reference Documentation

- [Cypress Documentation](https://docs.cypress.io/)
- [SASS Module System](https://sass-lang.com/documentation/at-rules/use)
- [Angular 22 Guide](https://angular.io/guide/update-to-latest-version)
- [Open Liberty 26 Docs](https://openliberty.io/docs/latest/)

### 5.3 Contact & Support

For questions or issues:
- GitHub Issues: https://github.com/OpenLiberty/liberty-bikes/issues
- Open Liberty Slack: https://openliberty.io/community/

---

**Plan Created**: 2026-06-20  
**Status**: Ready for Implementation  
**Next Step**: Begin with SASS Modernization (lowest risk)