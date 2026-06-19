# Standalone Components Migration Roadmap

## Overview
After completing the Angular 21 migration, migrate all components from NgModule-based to standalone components architecture. This is Angular's recommended modern approach and will simplify the application structure.

## Current State
- All components currently use `standalone: false`
- Application uses traditional NgModule architecture with `app.module.ts`
- Components are declared in NgModule's `declarations` array

## Target State
- All components use `standalone: true`
- Remove `app.module.ts` and use standalone bootstrap
- Components directly import their dependencies
- Simplified application structure with better tree-shaking

## Benefits
1. **Better Tree-Shaking**: Only import what you need per component
2. **Simpler Mental Model**: No need to track NgModule dependencies
3. **Lazy Loading**: Easier to lazy load individual components
4. **Modern Angular**: Aligns with Angular's future direction
5. **Reduced Boilerplate**: No need for NgModule declarations

## Migration Steps

### Phase 1: Prepare Components (1-2 days)
1. Identify all component dependencies (imports, providers, etc.)
2. Document current NgModule structure
3. Create migration checklist for each component

### Phase 2: Convert Leaf Components (2-3 days)
Start with components that have no child components:
- `PlayerComponent`
- `SliderComponent`

For each component:
1. Change `standalone: false` to `standalone: true`
2. Add `imports` array with all required modules/components
3. Remove from NgModule `declarations`
4. Test component functionality

### Phase 3: Convert Container Components (3-4 days)
Convert components that contain other components:
- `PlayerListComponent`
- `LeaderboardComponent`
- `ControlsComponent`

For each component:
1. Change to `standalone: true`
2. Import child standalone components
3. Add required modules to `imports`
4. Remove from NgModule `declarations`
5. Test component functionality

### Phase 4: Convert Main Components (2-3 days)
Convert main application components:
- `GameComponent`
- `LoginComponent`

For each component:
1. Change to `standalone: true`
2. Import all child components
3. Add required modules and services
4. Remove from NgModule `declarations`
5. Test full page functionality

### Phase 5: Convert Root Component (1-2 days)
1. Convert `AppComponent` to standalone
2. Update `main.ts` to use `bootstrapApplication()`
3. Provide global services in `bootstrapApplication()` config
4. Remove `app.module.ts`
5. Update `app-routing.module.ts` to standalone routes

### Phase 6: Testing & Cleanup (2-3 days)
1. Run all unit tests
2. Run integration tests
3. Test all user flows
4. Remove unused NgModule files
5. Update documentation

## Technical Details

### Before (NgModule-based)
```typescript
// app.module.ts
@NgModule({
  declarations: [AppComponent, LoginComponent, ...],
  imports: [BrowserModule, FormsModule, ...],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

// app.component.ts
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false
})
export class AppComponent { }
```

### After (Standalone)
```typescript
// main.ts
bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideAnimations()
  ]
});

// app.component.ts
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [RouterOutlet, CommonModule]
})
export class AppComponent { }
```

## Component-Specific Notes

### AppComponent
- Needs: `RouterOutlet`, `SocketService`, `GameService`, `PlayersService`
- Providers should move to `bootstrapApplication()` config

### LoginComponent
- Needs: `CommonModule`, `FormsModule`, `HttpClientModule`, `SliderComponent`
- Complex animations - test thoroughly

### GameComponent
- Needs: `CommonModule`, `PlayerListComponent`, `LeaderboardComponent`, `ControlsComponent`
- Largest component - break into smaller steps

### ControlsComponent
- Needs: `CommonModule`, `FormsModule`, `GameService`
- Canvas manipulation - test touch/mouse events

### PlayerListComponent
- Needs: `CommonModule`, `PlayerComponent`, `PlayersService`
- WebSocket updates - test real-time functionality

### PlayerComponent
- Needs: `CommonModule`
- Simplest component - good starting point

### LeaderboardComponent
- Needs: `CommonModule`, `HttpClientModule`
- Animations - test transitions

### SliderComponent
- Needs: `CommonModule`
- Complex animations - test all states

## Routing Migration

### Before
```typescript
// app-routing.module.ts
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

### After
```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'game/:id', component: GameComponent },
  { path: 'controls/:id', component: ControlsComponent }
];

// main.ts
bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes)]
});
```

## Testing Strategy
1. **Unit Tests**: Update component tests to use standalone testing utilities
2. **Integration Tests**: Test component interactions
3. **E2E Tests**: Verify full user flows work correctly
4. **Performance Tests**: Ensure no performance degradation

## Rollback Plan
- Keep NgModule version in separate branch
- Can revert by changing `standalone: true` back to `false`
- Restore `app.module.ts` from git history

## Timeline
- **Total Estimated Time**: 2-3 weeks
- **Prerequisites**: Angular 21 migration completed
- **Dependencies**: None (can be done independently)

## Success Criteria
- [ ] All components converted to standalone
- [ ] `app.module.ts` removed
- [ ] All tests passing
- [ ] No functionality regressions
- [ ] Build size same or smaller
- [ ] Documentation updated

## References
- [Angular Standalone Components Guide](https://angular.dev/guide/components/importing)
- [Standalone Migration Guide](https://angular.dev/reference/migrations/standalone)
- [Angular CLI Standalone Schematic](https://angular.dev/cli/generate#component)

## Notes
- This migration is optional but recommended
- Angular's future direction is standalone-first
- Can be done incrementally without breaking changes
- Improves code organization and maintainability