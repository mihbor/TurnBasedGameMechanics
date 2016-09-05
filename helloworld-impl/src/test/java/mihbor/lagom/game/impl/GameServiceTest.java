package mihbor.lagom.game.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;

import org.junit.Test;

import mihbor.lagom.game.api.GameService;

public class GameServiceTest {

	@Test
	public void test() throws Exception {
	    withServer(defaultSetup(), server -> {
	    	GameService service = server.client(GameService.class);
	    });
	}

}
