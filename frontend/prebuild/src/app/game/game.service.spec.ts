import { TestBed, inject } from '@angular/core/testing';
import { Subject } from 'rxjs';

import { GameService } from './game.service';
import { SocketService } from '../net/socket.service';

describe('GameService', () => {
  let mockSocketService: any;

  beforeEach(() => {
    mockSocketService = {
      socket: new Subject<MessageEvent>(),
      url: '',
      send: jasmine.createSpy('send'),
      close: jasmine.createSpy('close')
    };
    
    TestBed.configureTestingModule({
      providers: [
        GameService,
        { provide: SocketService, useValue: mockSocketService }
      ]
    });
  });

  it('should be created', inject([GameService], (service: GameService) => {
    expect(service).toBeTruthy();
  }));
});
