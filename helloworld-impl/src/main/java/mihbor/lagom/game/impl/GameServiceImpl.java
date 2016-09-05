package mihbor.lagom.game.impl;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import akka.Done;
import akka.NotUsed;
import mihbor.lagom.game.api.GameService;
import mihbor.lagom.game.impl.GameCommand.*;

public class GameServiceImpl implements GameService {

	private final PersistentEntityRegistry entityRegistry;

	@Inject
	public GameServiceImpl(PersistentEntityRegistry entityRegistry) {
		this.entityRegistry = entityRegistry;
		entityRegistry.register(Game.class);
	}
	
	@Override
	public ServiceCall<NotUsed, Done> proposeGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(Game.class, id);
	    	return ref.ask(new ProposeGame(id)).thenApply(evt -> Done.getInstance());
		};
	}

	@Override
	public ServiceCall<NotUsed, Done> startGame(String id) {

		return request -> {
    		// Create the game entity for the given ID
	    	PersistentEntityRef<GameCommand> ref = entityRegistry.refFor(Game.class, id);
	    	return ref.ask(new StartGame()).thenApply(evt -> Done.getInstance());
		};
	}


}
