package mihbor.lagom.game.impl;

import org.immutables.value.Value;

import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import mihbor.lagom.game.impl.GameEvent.*;

public interface GameCommand extends Jsonable {

	@Value.Immutable
	@ImmutableStyle
	public interface AbstractProposeGame extends GameCommand, PersistentEntity.ReplyType<GameProposed> {
		String getGameId();
	}

	@Value.Immutable
	@ImmutableStyle
	public interface AbstractJoinGame extends GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {
		String getPlayerId();
	}

	@Value.Immutable
	@ImmutableStyle
	public interface AbstractStartGame extends GameCommand, PersistentEntity.ReplyType<GameStarted> {
	}

	@Value.Immutable
	@ImmutableStyle
	public interface AbstractSetPlayerOrder extends GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {
		/* out of scope for now */
	}

	@Value.Immutable
	@ImmutableStyle
	public interface AbstractTakeAction extends GameCommand, PersistentEntity.ReplyType<ActionTaken> {
		/* out of scope for now */
	}

	@Value.Immutable
	@ImmutableStyle
	public interface AbstractEndTurn extends GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {
		String getPlayerId();
		long getTurn();
	}
}
