package mihbor.lagom.game.impl;

import java.util.Optional;
import java.util.Arrays;

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
		commonEventHandlers(b);
		
		return b.build();
	}

	private void proposeGameBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {

		b.setCommandHandler(
			ProposeGame.class, 
			(cmd, ctx) -> {
				GameProposed gameProposed = new GameProposed(cmd.getGameId());
				if(state() == GameState.EMPTY) {
					return ctx.thenPersist(gameProposed, evt -> ctx.reply(evt));
				} else { // already proposed, we're idempotent, so reply GameProposed
					assert state().gameId == cmd.getGameId(); // this must hold as this is the identifier for this entity!
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
				PlayerJoinedGame playerJoined = new PlayerJoinedGame(state().gameId, cmd.getPlayerId());
				// idempotency again
				if(!state().hasPlayer(cmd.getPlayerId())) {
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
						0
					);
					return ctx.thenPersistAll(
						Arrays.asList(gameStarted, playersTurnBegun),
						() -> ctx.reply(gameStarted)
					);
				} else {
					ctx.reply(gameStarted);
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(GameStarted.class, evt -> state().gameStarted());
	}

	private void endTurnBehavior(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {
		
		b.setCommandHandler(
			EndTurn.class,
			(cmd, ctx) -> {
				if(cmd.getTurn() == state().turn && cmd.getPlayerId().equals(state().getCurrentTurnsPlayersId())) {
					PlayersTurnEnded playersTurnEnded = new PlayersTurnEnded(
						state().gameId, 
						cmd.getPlayerId(), 
						state().turn
					);
					PlayersTurnBegun playersTurnBegun = new PlayersTurnBegun(
						state().gameId,
						state().getNextTurnsPlayersId(),
						state().turn+1
					);
					return ctx.thenPersistAll(
						Arrays.asList(playersTurnEnded, playersTurnBegun), 
						() -> ctx.reply(playersTurnEnded)
					);
				} else if(cmd.getTurn() == state().turn-1 && cmd.getPlayerId().equals(state().getPreviousTurnsPlayerId())) { //idempotency
					ctx.reply(new PlayersTurnEnded(state().gameId, cmd.getPlayerId(), cmd.getTurn()));
					return ctx.done();
				} else {
					ctx.invalidCommand("not your turn to end");
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(PlayersTurnEnded.class, evt -> state().playersTurnEnded(evt.playerId));
	}

	private void commonEventHandlers(PersistentEntity<GameCommand, GameEvent, GameState>.BehaviorBuilder b) {
		
		// this event is generated by both StartGame and EndTurn commands:
		b.setEventHandler(PlayersTurnBegun.class, evt -> state().playersTurnBegun(evt.playerId, evt.turn));
	}
}
