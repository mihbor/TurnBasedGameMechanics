package mihbor.lagom.game.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import mihbor.lagom.game.api.GameService;

public class GameServiceTest {

	@Test
	public void testHappyPath() throws Exception {
	    withServer(defaultSetup(), server -> {
	    	GameService service = server.client(GameService.class);
	    	
	        service.proposeGame("Abracadabra").invoke().toCompletableFuture().get(5, SECONDS);

	        service.joinGame("Abracadabra","Alice").invoke().toCompletableFuture().get(5, SECONDS);
	        service.joinGame("Abracadabra","Bob").invoke().toCompletableFuture().get(5, SECONDS);

	        service.startGame("Abracadabra").invoke().toCompletableFuture().get(5, SECONDS);
	        
	        service.endTurn("Abracadabra","Alice",0).invoke().toCompletableFuture().get(5, SECONDS);
	        service.endTurn("Abracadabra","Bob",1).invoke().toCompletableFuture().get(5, SECONDS);
	        service.endTurn("Abracadabra","Alice",2).invoke().toCompletableFuture().get(5, SECONDS);
	    });
	}

}
