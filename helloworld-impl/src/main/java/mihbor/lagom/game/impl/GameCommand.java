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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ProposeGame other = (ProposeGame) obj;
			if (gameId == null) {
				if (other.gameId != null) return false;
			} else if (!gameId.equals(other.gameId)) return false;
			return true;
		}

	}
	
	@Immutable
	public final class JoinGame implements GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		final String playerId;
		
		public JoinGame(String playerId) {
			this.playerId = playerId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			JoinGame other = (JoinGame) obj;
			if (playerId == null) {
				if (other.playerId != null) return false;
			} else if (!playerId.equals(other.playerId)) return false;
			return true;
		}
	}
	
	@Immutable
	public final class StartGame implements GameCommand, PersistentEntity.ReplyType<GameStarted> {

		@Override
		public int hashCode() {
			return 1;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			return true;
		}
	}

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
			result = prime * result + (int) (turn ^ (turn >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			EndTurn other = (EndTurn) obj;
			if (playerId == null) {
				if (other.playerId != null) return false;
			} else if (!playerId.equals(other.playerId)) return false;
			if (turn != other.turn) return false;
			return true;
		}
	}
}
