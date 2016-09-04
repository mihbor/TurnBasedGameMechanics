package mihbor.lagom.game.impl;

import java.util.Optional;
import java.util.ArrayList;

import com.google.gdata.util.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameCommand.*;
import mihbor.lagom.game.impl.GameEvent.*;

public class Game extends PersistentEntity<GameCommand, GameEvent, GameState> {

	@Override
	public PersistentEntity<GameCommand, GameEvent, GameState>.Behavior initialBehavior(Optional<GameState> snapshot) {
		BehaviorBuilder b = newBehaviorBuilder(snapshot.orElse(GameState.EMPTY));
		proposeGameBehavior(b);
		joinGameBehavior(b);
		startGameBehavior(b);
		endTurnBehavior(b);
		return b.build();
	}

	private void proposeGameBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {

		b.setCommandHandler(
			ProposeGame.class, 
			(cmd, ctx) -> {
				GameProposed gameProposed = new GameProposed(cmd.gameId);
				if(state() == GameState.EMPTY) {
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
				if(!state().hasPlayer(cmd.playerId)) {
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
				Preconditions.checkState(state().getPlayerCount() > 0, "can't start game without at least one player");
				GameStarted gameStarted = new GameStarted(state().gameId);
				if(!state().isStarted) {
					PlayersTurnBegun playersTurnBegun = new PlayersTurnBegun(
						state().gameId,
						state().getNextTurnsPlayersId(),
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

	private void endTurnBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {
		
		b.setCommandHandler(
			EndTurn.class,
			(cmd, ctx) -> {
				if(cmd.turn == state().turn) {
					PlayersTurnEnded playersTurnEnded = new PlayersTurnEnded(state().gameId, cmd.playerId, state().turn);
					return ctx.thenPersist(playersTurnEnded, evt -> ctx.reply(evt));
				} else {
					ctx.invalidCommand("not your turn to end");
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(PlayersTurnEnded.class, evt -> state().playersTurnEnded());
	}

}
