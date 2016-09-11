package mihbor.lagom.game.api;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Style(typeImmutable="*Impl", allParameters=true)
public interface GameEvent extends Jsonable {

	@Value.Immutable @JsonDeserialize(as=GameProposedImpl.class)
	public interface GameProposed extends GameEvent {
		String getGameId();
	}

	@Value.Immutable @JsonDeserialize(as=PlayerJoinedGameImpl.class)
	public interface PlayerJoinedGame extends GameEvent {
		String getGameId();
		String getPlayerId();
	}

	@Value.Immutable @JsonDeserialize(as=GameStartedImpl.class)
	public interface GameStarted extends GameEvent {
		String getGameId();
	}

	@Value.Immutable @JsonDeserialize(as=PlayerOrderSetImpl.class)
	public interface PlayerOrderSet extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable @JsonDeserialize(as=PlayersTurnBegunImpl.class)
	public interface PlayersTurnBegun extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable @JsonDeserialize(as=PlayersTurnEndedImpl.class)
	public interface PlayersTurnEnded extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable @JsonDeserialize(as=ActionTakenImpl.class)
	public interface ActionTaken extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable @JsonDeserialize(as=GameFinishedImpl.class)
	public interface GameFinished extends GameEvent {
		/* out of scope for now */
	}

}
