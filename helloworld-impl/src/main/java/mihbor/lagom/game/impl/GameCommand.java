package mihbor.lagom.game.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameEvent.*;

public interface GameCommand {
	
	public final class ProposeGame implements GameCommand, PersistentEntity.ReplyType<GameProposed> {
		String gameId;
		
		public ProposeGame(String gameId) {
			this.gameId = gameId;
		}
	}
	public final class JoinGame implements GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		String playerId;
	}
	public final class StartGame implements GameCommand, PersistentEntity.ReplyType<GameStarted> {}
	public final class SetPlayerOrder implements GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {
		/* out of scope for now */
	}
	public final class TakeAction implements GameCommand, PersistentEntity.ReplyType<ActionTaken> {
		/* out of scope for now */
	}
	public final class EndTurn implements GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {
		String playerId;
		long turn;
	}
}
