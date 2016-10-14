package mihbor.lagom.game.impl;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.NotUsed;
import mihbor.lagom.game.api.*;
import mihbor.lagom.game.impl.GameEntity;

public class GameServiceImpl implements GameService {

	private final PersistentEntityRegistry entityRegistry;
	private final CassandraSession cassandraSession;

	@Inject
	public GameServiceImpl(
		PersistentEntityRegistry entityRegistry,
	    CassandraSession cassandraSession) {
		
		this.entityRegistry = entityRegistry;
		this.cassandraSession = cassandraSession;
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
	public ServiceCall<JoinGameCmd, PlayerJoinedGameEvent> joinGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(GameEntity.class, id);
	    	return ref.ask(request);
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
	public ServiceCall<EndTurnCmd, PlayersTurnEndedEvent> endTurn(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(GameEntity.class, id);
	    	return ref.ask(request);
		};
	}

	@Override
	public ServiceCall<NotUsed, String> getAllGames() {
		return request -> cassandraSession
			.selectOne("SELECT id FROM hello_impl.game;")
			.thenApply(o -> o.get().getString("id"));
	}

}
