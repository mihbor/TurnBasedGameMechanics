package mihbor.lagom.game.impl;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import akka.NotUsed;
import mihbor.lagom.game.api.GameEvent.*;
import mihbor.lagom.game.api.GameService;

public class GameServiceImpl implements GameService {

	private final PersistentEntityRegistry entityRegistry;

	@Inject
	public GameServiceImpl(PersistentEntityRegistry entityRegistry) {
		this.entityRegistry = entityRegistry;
		entityRegistry.register(Game.class);
	}
	
	@Override
	public ServiceCall<NotUsed, GameProposed> proposeGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(Game.class, id);
	    	return ref.ask(ProposeGameImpl.builder().gameId(id).build());
		};
	}

	@Override
	public ServiceCall<NotUsed, PlayerJoinedGame> joinGame(String id, String playerId) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(Game.class, id);
	    	return ref.ask(JoinGameImpl.builder().playerId(playerId).build());
		};
	}

	@Override
	public ServiceCall<NotUsed, GameStarted> startGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(Game.class, id);
	    	return ref.ask(StartGameImpl.builder().build());
		};
	}

	@Override
	public ServiceCall<NotUsed, PlayersTurnEnded> endTurn(String id, String playerId, long turn) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(Game.class, id);
	    	return ref.ask(EndTurnImpl.builder().playerId(playerId).turn(turn).build());
		};
	}

}
