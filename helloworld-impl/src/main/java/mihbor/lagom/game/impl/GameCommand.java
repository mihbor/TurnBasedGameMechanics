package mihbor.lagom.game.impl;

import javax.annotation.concurrent.Immutable;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import mihbor.lagom.game.impl.GameEvent.*;

public interface GameCommand extends Jsonable {

	@Immutable
	public final class ProposeGame implements GameCommand, PersistentEntity.ReplyType<GameProposed> {
		final String gameId;
		
		public ProposeGame(String gameId) {
			this.gameId = gameId;
		}

		@Override
		public boolean equals(Object that) {
			if (this == that) return true;
			if (that == null) return false;
			return that instanceof ProposeGame && equalTo((ProposeGame) that);
		}
		private boolean equalTo(ProposeGame that) {
			return gameId == null && that.gameId == null || gameId.equals(that.gameId);
		}

		@Override
		public int hashCode() {
			int h = 31;
			if (gameId != null) h = h * 17 + gameId.hashCode();
			return h;
		}
	}
	
	@Immutable
	public final class JoinGame implements GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		final String playerId;
		
		public JoinGame(String playerId) {
			this.playerId = playerId;
		}
	}
	
	@Immutable
	public final class StartGame implements GameCommand, PersistentEntity.ReplyType<GameStarted> {}

	@Immutable
	public final class SetPlayerOrder implements GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {
		/* out of scope for now */
	}

	@Immutable
	public final class TakeAction implements GameCommand, PersistentEntity.ReplyType<ActionTaken> {
		/* out of scope for now */
	}

	@Immutable
	public final class EndTurn implements GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {
		final String playerId;
		final long turn;
		
		public EndTurn(String playerId, long turn) {
			this.playerId = playerId;
			this.turn = turn;
		}
	}
}
