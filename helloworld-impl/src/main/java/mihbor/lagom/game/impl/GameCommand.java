package mihbor.lagom.game.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameEvent.*;

public interface GameCommand {
	
	public final class ProposeGame implements GameCommand, PersistentEntity.ReplyType<GameProposed> {
		final String gameId;
		
		public ProposeGame(String gameId) {
			this.gameId = gameId;
		}
	}
	
	public final class JoinGame implements GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		final String playerId;
		
		public JoinGame(String playerId) {
			this.playerId = playerId;
		}
	}
	
	public final class StartGame implements GameCommand, PersistentEntity.ReplyType<GameStarted> {}
	
	public final class SetPlayerOrder implements GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {
		/* out of scope for now */
	}
	
	public final class TakeAction implements GameCommand, PersistentEntity.ReplyType<ActionTaken> {
		/* out of scope for now */
	}
	
	public final class EndTurn implements GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {
		final String playerId;
		final long turn;
		
		public EndTurn(String playerId, long turn) {
			this.playerId = playerId;
			this.turn = turn;
		}
	}
}
