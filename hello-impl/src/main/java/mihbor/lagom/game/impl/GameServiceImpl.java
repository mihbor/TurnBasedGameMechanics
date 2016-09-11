package mihbor.lagom.game.impl;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import akka.NotUsed;
import mihbor.lagom.game.api.*;

public class GameServiceImpl implements GameService {

	private final PersistentEntityRegistry entityRegistry;

	@Inject
	public GameServiceImpl(PersistentEntityRegistry entityRegistry) {
		this.entityRegistry = entityRegistry;
		entityRegistry.register(GameEntity.class);
	}
	
	@Override
	public ServiceCall<NotUsed, GameProposedEvent> proposeGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(GameEntity.class, id);
	    	return ref.ask(ProposeGameCmd.of(id));
		};
	}

	@Override
	public ServiceCall<NotUsed, PlayerJoinedGameEvent> joinGame(String id, String playerId) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(GameEntity.class, id);
	    	return ref.ask(JoinGameCmd.of(playerId));
		};
	}

	@Override
	public ServiceCall<NotUsed, GameStartedEvent> startGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(GameEntity.class, id);
	    	return ref.ask(StartGameCmd.of());
		};
	}

	@Override
	public ServiceCall<NotUsed, PlayersTurnEndedEvent> endTurn(String id, String playerId, long turn) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(GameEntity.class, id);
	    	return ref.ask(EndTurnCmd.of(playerId, turn));
		};
	}

}
