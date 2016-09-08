package mihbor.lagom.game.impl;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import mihbor.lagom.game.impl.GameEvent.*;

@Value.Style(typeImmutable="*Impl", allParameters=true)
public interface GameCommand extends Jsonable {

	@Value.Immutable @JsonSerialize
	public interface ProposeGame extends GameCommand, PersistentEntity.ReplyType<GameProposed> {
		@Value.Parameter String getGameId();
	}

	@Value.Immutable @JsonSerialize
	public interface JoinGame extends GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		String getPlayerId();
	}
	
	@Value.Immutable(singleton=true) @JsonSerialize
	public interface StartGame extends GameCommand, PersistentEntity.ReplyType<GameStarted> {
	}

	@Value.Immutable @JsonSerialize
	public interface SetPlayerOrder extends GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {
		/* out of scope for now */
	}

	@Value.Immutable @JsonSerialize
	public interface TakeAction extends GameCommand, PersistentEntity.ReplyType<ActionTaken> {
		/* out of scope for now */
	}

	@Value.Immutable @JsonSerialize
	public interface EndTurn extends GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {
		String getPlayerId();
		long getTurn();
	}
	
}
