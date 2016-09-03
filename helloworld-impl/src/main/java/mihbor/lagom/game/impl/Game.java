package mihbor.lagom.game.impl;

import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameCommand.JoinGame;
import mihbor.lagom.game.impl.GameCommand.ProposeGame;
import mihbor.lagom.game.impl.GameCommand.StartGame;
import mihbor.lagom.game.impl.GameEvent.GameProposed;
import mihbor.lagom.game.impl.GameEvent.GameStarted;
import mihbor.lagom.game.impl.GameEvent.PlayerJoinedGame;

public class Game extends PersistentEntity<GameCommand, GameEvent, GameState> {

	@Override
	public PersistentEntity<GameCommand, GameEvent, GameState>.Behavior initialBehavior(Optional<GameState> snapshot) {
		BehaviorBuilder b = newBehaviorBuilder(snapshot.orElse(GameState.EMPTY));

		b.setCommandHandler(
			ProposeGame.class, 
			(cmd, ctx) -> {
				GameProposed gameProposed = new GameProposed(cmd.gameId);
				if(state().gameId == null) {
					return ctx.thenPersist(gameProposed, evt -> ctx.reply(evt));
				} else { // already proposed, we're idempotent, so reply GameProposed
					assert state().gameId == cmd.gameId; // this must hold as this is the identifier for this entity!
					ctx.reply(gameProposed);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(GameProposed.class, evt -> state().gameProposed(evt.gameId));
		
		
		b.setCommandHandler(
			JoinGame.class, 
			(cmd, ctx) -> {
				PlayerJoinedGame playerJoined = new PlayerJoinedGame(cmd.gameId, cmd.playerId);
				// idempotency again
				if(!state().playerIds.contains(cmd.playerId)) {
					return ctx.thenPersist(playerJoined, evt -> ctx.reply(evt));
				} else {
					assert state().gameId == cmd.gameId;
					ctx.reply(playerJoined);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(PlayerJoinedGame.class, evt -> state().playerJoinedGame(evt.playerId));
		
		b.setCommandHandler(
			StartGame.class, 
			(cmd, ctx) -> {
				GameStarted gameStarted = new GameStarted(cmd.gameId);
				if(!state().isStarted){
					return ctx.thenPersist(gameStarted, evt -> ctx.reply(evt));
				} else {
					ctx.reply(gameStarted);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(GameStarted.class, evt -> state().gameStarted());
		
		return b.build();
	}

}
