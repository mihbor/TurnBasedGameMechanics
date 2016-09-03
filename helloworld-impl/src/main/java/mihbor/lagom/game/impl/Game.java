package mihbor.lagom.game.impl;

import java.util.Optional;
import java.util.ArrayList;

import com.google.gdata.util.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameCommand.JoinGame;
import mihbor.lagom.game.impl.GameCommand.ProposeGame;
import mihbor.lagom.game.impl.GameCommand.StartGame;
import mihbor.lagom.game.impl.GameEvent.GameProposed;
import mihbor.lagom.game.impl.GameEvent.GameStarted;
import mihbor.lagom.game.impl.GameEvent.PlayerJoinedGame;
import mihbor.lagom.game.impl.GameEvent.PlayersTurnBegun;

public class Game extends PersistentEntity<GameCommand, GameEvent, GameState> {

	@Override
	public PersistentEntity<GameCommand, GameEvent, GameState>.Behavior initialBehavior(Optional<GameState> snapshot) {
		BehaviorBuilder b = newBehaviorBuilder(snapshot.orElse(GameState.EMPTY));
		proposeGameBehavior(b);
		joinGameBehavior(b);
		startGameBehavior(b);
		return b.build();
	}

	private void proposeGameBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {

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
	}

	private void joinGameBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {
		
		b.setCommandHandler(
			JoinGame.class, 
			(cmd, ctx) -> {
				PlayerJoinedGame playerJoined = new PlayerJoinedGame(state().gameId, cmd.playerId);
				// idempotency again
				if(!state().playerIds.contains(cmd.playerId)) {
					return ctx.thenPersist(playerJoined, evt -> ctx.reply(evt));
				} else {
					ctx.reply(playerJoined);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(PlayerJoinedGame.class, evt -> state().playerJoinedGame(evt.playerId));
	}
	
	private void startGameBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {
		
		b.setCommandHandler(
			StartGame.class, 
			(cmd, ctx) -> {
				Preconditions.checkState(state().playerCount() < 1, "can't start game without at least one player");
				GameStarted gameStarted = new GameStarted(state().gameId);
				if(!state().isStarted) {
					PlayersTurnBegun playersTurnBegun = new PlayersTurnBegun(
						state().gameId,
						state().playerIds.iterator().next(),
						0L
					);
					return ctx.thenPersistAll(
						new ArrayList<GameEvent>() {{ 
							add(gameStarted); 
							add(playersTurnBegun);
						}},
						() -> ctx.reply(gameStarted)
					);
				} else {
					ctx.reply(gameStarted);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(GameStarted.class, evt -> state().gameStarted());
		b.setEventHandler(PlayersTurnBegun.class, evt -> state().playersTurnBegun(evt.playerId, evt.turn));
	}

}
