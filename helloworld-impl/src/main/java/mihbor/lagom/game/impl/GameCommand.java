package mihbor.lagom.game.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameEvent.*;

public interface GameCommand {
	
	public final class ProposeGame implements GameCommand, PersistentEntity.ReplyType<GameProposed> {
		String id;
	}
	public final class JoinGame implements GameCommand, PersistentEntity.ReplyType<PlayerJoinedGame> {}
	public final class StartGame implements GameCommand, PersistentEntity.ReplyType<GameStarted> {}
	public final class SetPlayerOrder implements GameCommand, PersistentEntity.ReplyType<PlayerOrderSet> {}
	public final class TakeAction implements GameCommand, PersistentEntity.ReplyType<ActionTaken> {}
	public final class EndTurn implements GameCommand, PersistentEntity.ReplyType<PlayersTurnEnded> {}
}
