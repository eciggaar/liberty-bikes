import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';

import { PlayerListComponent } from './playerlist.component';
import { SocketService } from '../../net/socket.service';

describe('PlayerlistComponent', () => {
  let component: PlayerListComponent;
  let fixture: ComponentFixture<PlayerListComponent>;
  let mockSocketService: any;

  beforeEach(waitForAsync(() => {
    mockSocketService = {
      socket: new Subject<MessageEvent>(),
      url: '',
      send: jasmine.createSpy('send'),
      close: jasmine.createSpy('close')
    };
    
    TestBed.configureTestingModule({
      declarations: [ PlayerListComponent ],
      providers: [
        { provide: SocketService, useValue: mockSocketService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlayerListComponent);
    component = fixture.componentInstance;
    // Don't call detectChanges() to avoid triggering lifecycle hooks
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
