package mihbor.lagom.game.impl;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Style(typeImmutable="*Impl", allParameters=true)
public interface GameEvent extends Jsonable {

	@Value.Immutable @JsonSerialize
	public interface GameProposed extends GameEvent {
		String getGameId();
	}

	@Value.Immutable @JsonSerialize
	public interface PlayerJoinedGame extends GameEvent {
		String getGameId();
		String getPlayerId();
	}

	@Value.Immutable @JsonSerialize
	public interface GameStarted extends GameEvent {
		String getGameId();
	}

	@Value.Immutable @JsonSerialize
	public interface PlayerOrderSet extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable @JsonSerialize
	public interface PlayersTurnBegun extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable @JsonSerialize
	public interface PlayersTurnEnded extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable @JsonSerialize
	public interface ActionTaken extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable @JsonSerialize
	public interface GameFinished extends GameEvent {
		/* out of scope for now */
	}

}
