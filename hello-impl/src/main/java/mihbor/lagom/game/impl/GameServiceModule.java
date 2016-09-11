package mihbor.lagom.game.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import mihbor.lagom.game.api.GameService;

public class GameServiceModule extends AbstractModule implements ServiceGuiceSupport {

	@Override
	protected void configure() {
		 bindServices(serviceBinding(GameService.class, GameServiceImpl.class));
	}

}
