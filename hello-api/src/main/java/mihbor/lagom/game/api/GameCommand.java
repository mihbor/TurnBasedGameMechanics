package mihbor.lagom.game.api;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Style(typeImmutable="*Cmd", allParameters=true)
public interface GameCommand extends Jsonable {

	@Value.Immutable @JsonSerialize
	public interface ProposeGame extends GameCommand, ReplyType<GameProposedEvent> {
		String getGameId();
	}

	@Value.Immutable @JsonSerialize
	public interface JoinGame extends GameCommand, ReplyType<PlayerJoinedGameEvent> {
		String getPlayerId();
	}
	
	@Value.Immutable(singleton=true) @JsonSerialize
	public interface StartGame extends GameCommand, ReplyType<GameStartedEvent> {
	}

	@Value.Immutable @JsonSerialize
	public interface SetPlayerOrder extends GameCommand, ReplyType<PlayerOrderSetEvent> {
		/* out of scope for now */
	}

	@Value.Immutable @JsonSerialize
	public interface TakeAction extends GameCommand, ReplyType<ActionTakenEvent> {
		/* out of scope for now */
	}

	@Value.Immutable @JsonSerialize
	public interface EndTurn extends GameCommand, ReplyType<PlayersTurnEndedEvent> {
		String getPlayerId();
		long getTurn();
	}

}
