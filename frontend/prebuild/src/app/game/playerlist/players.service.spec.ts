import { TestBed, inject } from '@angular/core/testing';
import { Subject } from 'rxjs';

import { PlayersService } from './players.service';
import { SocketService } from '../../net/socket.service';

describe('PlayersService', () => {
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
        PlayersService,
        { provide: SocketService, useValue: mockSocketService }
      ]
    });
  });

  it('should be created', inject([PlayersService], (service: PlayersService) => {
    expect(service).toBeTruthy();
  }));
});
