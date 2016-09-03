package mihbor.lagom.game.impl;

import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameCommand.JoinGame;
import mihbor.lagom.game.impl.GameCommand.ProposeGame;
import mihbor.lagom.game.impl.GameEvent.GameProposed;
import mihbor.lagom.game.impl.GameEvent.PlayerJoinedGame;

public class Game extends PersistentEntity<GameCommand, GameEvent, GameState> {

	@Override
	public PersistentEntity<GameCommand, GameEvent, GameState>.Behavior initialBehavior(Optional<GameState> snapshot) {
		BehaviorBuilder b = newBehaviorBuilder(snapshot.orElse(GameState.EMPTY));

		b.setCommandHandler(
			ProposeGame.class, 
			(cmd, ctx) -> {
				GameProposed gameProposed = new GameProposed(cmd.gameId);
				if(state().id == null) {
					return ctx.thenPersist(gameProposed, evt -> ctx.reply(evt));
				} else { // already proposed, we're idempotent, so reply GameProposed
					assert state().id == cmd.gameId; // this must hold as this is the identifier for this entity!
					ctx.reply(gameProposed);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(
			GameProposed.class, evt -> state().gameProposed(evt.gameId)
		);
		
		b.setCommandHandler(
			JoinGame.class, 
			(cmd, ctx) -> {
				PlayerJoinedGame playerJoined = new PlayerJoinedGame(cmd.gameId, cmd.playerId);
				// idempotency again
				if(!state().playerIds.contains(cmd.playerId)) {
					return ctx.thenPersist(playerJoined);
				} else {
					assert state().id == cmd.gameId;
					ctx.reply(playerJoined);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(
			PlayerJoinedGame.class,
			evt -> state().playerJoinedGame(evt.playerId)
		);
		
		return b.build();
	}

}
