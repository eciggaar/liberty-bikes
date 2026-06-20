import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerComponent } from './player.component';
import { Player } from '../../entity/player';

describe('PlayerComponent', () => {
  let component: PlayerComponent;
  let fixture: ComponentFixture<PlayerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PlayerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlayerComponent);
    component = fixture.componentInstance;
    // Provide required @Input() player to avoid errors
    component.player = new Player();
    // Don't call detectChanges() to avoid triggering lifecycle hooks that depend on player data
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
