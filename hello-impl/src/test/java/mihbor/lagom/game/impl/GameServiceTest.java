package mihbor.lagom.game.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import mihbor.lagom.game.api.EndTurnCmd;
import mihbor.lagom.game.api.GameService;
import mihbor.lagom.game.api.JoinGameCmd;
import mihbor.lagom.game.api.StartGameCmd;

public class GameServiceTest {

	@Test
	public void testHappyPath() throws Exception {
	    withServer(defaultSetup(), server -> {
	    	GameService service = server.client(GameService.class);
	    	
	        service.proposeGame("Abracadabra").invoke().toCompletableFuture().get(5, SECONDS);

	        service.joinGame("Abracadabra").invoke(JoinGameCmd.of("Alice")).toCompletableFuture().get(5, SECONDS);
	        service.joinGame("Abracadabra").invoke(JoinGameCmd.of("Bob")).toCompletableFuture().get(5, SECONDS);

	        service.startGame("Abracadabra").invoke(StartGameCmd.of()).toCompletableFuture().get(5, SECONDS);
	        
	        service.endTurn("Abracadabra").invoke(EndTurnCmd.of("Alice", 0)).toCompletableFuture().get(5, SECONDS);
	        service.endTurn("Abracadabra").invoke(EndTurnCmd.of("Bob", 1)).toCompletableFuture().get(5, SECONDS);
	        service.endTurn("Abracadabra").invoke(EndTurnCmd.of("Alice", 2)).toCompletableFuture().get(5, SECONDS);
	    });
	}

}
