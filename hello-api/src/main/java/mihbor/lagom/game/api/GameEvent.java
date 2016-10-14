package mihbor.lagom.game.api;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Style(typeImmutable="*Event", allParameters=true)
public interface GameEvent extends AggregateEvent<GameEvent>, Jsonable {

	@Value.Immutable @JsonDeserialize
	public interface GameProposed extends GameEvent {
		String getGameId();
	}

	@Value.Immutable @JsonDeserialize
	public interface PlayerJoinedGame extends GameEvent {
		String getGameId();
		String getPlayerId();
	}

	@Value.Immutable @JsonDeserialize
	public interface GameStarted extends GameEvent {
		String getGameId();
	}

	@Value.Immutable @JsonDeserialize
	public interface PlayerOrderSet extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable @JsonDeserialize
	public interface PlayersTurnBegun extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable @JsonDeserialize
	public interface PlayersTurnEnded extends GameEvent {
		String getGameId();
		String getPlayerId();
		long getTurn();
	}

	@Value.Immutable @JsonDeserialize
	public interface ActionTaken extends GameEvent {
		/* out of scope for now */
	}

	@Value.Immutable @JsonDeserialize
	public interface GameFinished extends GameEvent {
		/* out of scope for now */
	}
	
	AggregateEventTag<GameEvent> TAG = AggregateEventTag.of(GameEvent.class);
	
	@Override
	default public AggregateEventTag<GameEvent> aggregateTag() {
		return TAG;
	}
}
