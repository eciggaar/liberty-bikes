import { Component, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { Player } from '../../entity/player';
import { PlayersService } from './players.service';

@Component({
  selector: 'app-player-list',
  templateUrl: './playerlist.component.html',
  styleUrls: ['./playerlist.component.scss'],
  providers: [ PlayersService ],
  standalone: false
})
export class PlayerListComponent implements OnInit {
  players: Player[] = [];

  constructor(
    private playersService: PlayersService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.playersService.messages.subscribe((msg) => {
      const json = msg as any;
      if (json.playerlist) {
        console.log(`Got playerlist ${JSON.stringify(json.playerlist)}`);
        console.log(`Current players array length: ${this.players.length}`);
        
        // Clear and rebuild the array
        const newPlayers: Player[] = [];
        json.playerlist.forEach((player) => {
          const newPlayer = new Player();
          newPlayer.name = player.name;
          newPlayer.status = player.status;
          newPlayer.color = player.color;
          newPlayers.push(newPlayer);
        });
        
        this.ngZone.run(() => {
          this.players = newPlayers;
          console.log(`Updated players array length: ${this.players.length}`);
          console.log(`Players array:`, this.players);
          // Force change detection
          this.cdr.detectChanges();
        });
      }
    }, (err) => {
      console.log(`Error occurred: ${err}`);
    });
  }
}
