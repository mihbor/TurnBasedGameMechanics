package mihbor.lagom.game.impl;

import javax.annotation.concurrent.Immutable;

import org.immutables.value.Value;

import com.lightbend.lagom.serialization.Jsonable;

@Value.Style(typeImmutable="*Impl")
public interface GameEvent extends Jsonable {

	@Value.Immutable
	public interface GameProposed extends GameEvent {
		String getGameId();
	}

	@Value.Immutable
	public interface PlayerJoinedGame extends GameEvent {
		String getGameId();
		String getPlayerId();
	}

	@Value.Immutable
	public interface GameStarted extends GameEvent {
		String getGameId();
	}

	@Value.Immutable
	public interface PlayerOrderSet extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable
	public interface PlayersTurnBegun extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable
	public interface PlayersTurnEnded extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable
	public interface ActionTaken extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable
	public interface GameFinished extends GameEvent {
		/* out of scope for now */
	}

}
