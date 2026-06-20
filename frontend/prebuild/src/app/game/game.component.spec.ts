import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Meta } from '@angular/platform-browser';
import { Subject } from 'rxjs';

import { GameComponent } from './game.component';
import { GameService } from './game.service';
import { SocketService } from '../net/socket.service';

describe('GameComponent', () => {
  let component: GameComponent;
  let fixture: ComponentFixture<GameComponent>;
  let mockSocketService: any;

  beforeEach(waitForAsync(() => {
    // Mock SocketService required by GameService
    mockSocketService = {
      socket: new Subject<MessageEvent>(),
      url: '',
      send: jasmine.createSpy('send'),
      close: jasmine.createSpy('close')
    };

    TestBed.configureTestingModule({
      declarations: [ GameComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        GameService,
        Meta,
        { provide: SocketService, useValue: mockSocketService }  // Required: GameService depends on SocketService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameComponent);
    component = fixture.componentInstance;
    // Don't call detectChanges() here as it triggers ngOnInit which requires more setup
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
