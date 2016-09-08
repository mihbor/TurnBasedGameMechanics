package mihbor.lagom.game.impl;

import org.immutables.value.Value;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import mihbor.lagom.game.impl.GameEvent.*;

@Value.Style(typeImmutable="*Impl")
public interface GameCommand extends Jsonable {

	@Value.Immutable
	public interface ProposeGame extends GameCommand, PersistentEntity.ReplyType<GameProposed> {
		String getGameId();
	}

	@Value.Immutable
	public interface JoinGame extends GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		String getPlayerId();
	}
	
	@Value.Immutable
	public interface StartGame extends GameCommand, PersistentEntity.ReplyType<GameStarted> {
	}

	@Value.Immutable
	public interface SetPlayerOrder extends GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {
		/* out of scope for now */
	}

	@Value.Immutable
	public interface TakeAction extends GameCommand, PersistentEntity.ReplyType<ActionTaken> {
		/* out of scope for now */
	}

	@Value.Immutable
	public interface EndTurn extends GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {
		String getPlayerId();
		long getTurn();
	}
}
