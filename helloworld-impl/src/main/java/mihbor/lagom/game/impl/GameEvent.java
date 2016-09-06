package mihbor.lagom.game.impl;

import javax.annotation.concurrent.Immutable;

import com.lightbend.lagom.serialization.Jsonable;

public interface GameEvent extends Jsonable {

	@Immutable
	public final class GameProposed implements GameEvent {
		final String gameId;
		
		public GameProposed(String gameId) {
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
			GameProposed other = (GameProposed) obj;
			if (gameId == null) {
				if (other.gameId != null) return false;
			} else if (!gameId.equals(other.gameId)) return false;
			return true;
		}

	}

	@Immutable
	public final class PlayerJoinedGame implements GameEvent {
		final String gameId;
		final String playerId;
		
		public PlayerJoinedGame(String gameId, String playerId) {
			this.gameId = gameId;
			this.playerId = playerId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
			result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PlayerJoinedGame other = (PlayerJoinedGame) obj;
			if (gameId == null) {
				if (other.gameId != null) return false;
			} else if (!gameId.equals(other.gameId)) return false;
			if (playerId == null) {
				if (other.playerId != null) return false;
			} else if (!playerId.equals(other.playerId)) return false;
			return true;
		}
	}

	@Immutable
	public final class GameStarted implements GameEvent {
		final String gameId;
		
		public GameStarted(String gameId) {
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
			GameStarted other = (GameStarted) obj;
			if (gameId == null) {
				if (other.gameId != null) return false;
			} else if (!gameId.equals(other.gameId)) return false;
			return true;
		}
	}

	@Immutable
	public final class PlayerOrderSet implements GameEvent {
		/* out of scope for now */
	}

	@Immutable
	public final class PlayersTurnBegun implements GameEvent {
		final String gameId;
		final String playerId;
		final long turn;

		public PlayersTurnBegun(String gameId, String playerId, long turn) {
			this.gameId = gameId;
			this.playerId = playerId;
			this.turn = turn;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
			result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
			result = prime * result + (int) (turn ^ (turn >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PlayersTurnBegun other = (PlayersTurnBegun) obj;
			if (gameId == null) {
				if (other.gameId != null) return false;
			} else if (!gameId.equals(other.gameId)) return false;
			if (playerId == null) {
				if (other.playerId != null) return false;
			} else if (!playerId.equals(other.playerId)) return false;
			if (turn != other.turn) return false;
			return true;
		}
	}

	@Immutable
	public final class PlayersTurnEnded implements GameEvent {
		final String gameId;
		final String playerId;
		final long turn;

		public PlayersTurnEnded(String gameId, String playerId, long turn) {
			this.gameId = gameId;
			this.playerId = playerId;
			this.turn = turn;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
			result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
			result = prime * result + (int) (turn ^ (turn >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PlayersTurnEnded other = (PlayersTurnEnded) obj;
			if (gameId == null) {
				if (other.gameId != null) return false;
			} else if (!gameId.equals(other.gameId)) return false;
			if (playerId == null) {
				if (other.playerId != null) return false;
			} else if (!playerId.equals(other.playerId)) return false;
			if (turn != other.turn) return false;
			return true;
		}
	}

	@Immutable
	public final class ActionTaken implements GameEvent {
		/* out of scope for now */
	}

	@Immutable
	public final class GameFinished implements GameEvent {
		/* out of scope for now */
	}

}
