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
	    public boolean equals(Object that) {
	      if (this == that) return true;
	      if (that == null) return false;
	      return that instanceof GameProposed && equalTo((GameProposed) that);
	    }
	    private boolean equalTo(GameProposed that) {
	      return gameId == null && that.gameId == null || gameId.equals(that.gameId);
	    }

	    @Override
	    public int hashCode() {
	      int h = 31;
	      if(gameId != null) h = h * 17 + gameId.hashCode();
	      return h;
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
	}

	@Immutable
	public final class GameStarted implements GameEvent {
		final String gameId;
		
		public GameStarted(String gameId) {
			this.gameId = gameId;
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
