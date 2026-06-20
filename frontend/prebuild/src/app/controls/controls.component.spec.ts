import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Subject } from 'rxjs';

import { ControlsComponent } from './controls.component';
import { SocketService } from '../net/socket.service';

describe('ControlsComponent', () => {
  let component: ControlsComponent;
  let fixture: ComponentFixture<ControlsComponent>;
  let mockSocketService: any;

  beforeEach(waitForAsync(() => {
    // Mock SocketService required by GameService (injected in component constructor)
    mockSocketService = {
      socket: new Subject<MessageEvent>(),
      url: '',
      send: jasmine.createSpy('send'),
      close: jasmine.createSpy('close')
    };

    TestBed.configureTestingModule({
      declarations: [ ControlsComponent ],
      imports: [
        HttpClientTestingModule,  // Required: component uses HttpClient
        RouterTestingModule       // Required: component uses Router
      ],
      providers: [
        { provide: SocketService, useValue: mockSocketService }  // Required: GameService depends on SocketService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
