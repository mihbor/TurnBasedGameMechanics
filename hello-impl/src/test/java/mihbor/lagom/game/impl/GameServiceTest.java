package mihbor.lagom.game.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import mihbor.lagom.game.api.*;

public class GameServiceTest {

	@Test
	public void testHappyPath() throws Exception {
	    withServer(defaultSetup(), server -> {
	    	//Given
		    	GameService service = server.client(GameService.class);
		    	String game = "Abracadabra";
		    //When
		        GameProposedEvent e1 = service.proposeGame(game).invoke().toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e1.getGameId());
		    //When
		        PlayerJoinedGameEvent e2 = service.joinGame(game).invoke(JoinGameCmd.of("Alice")).toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e2.getGameId());
		        assertEquals("Alice", e2.getPlayerId());
		    //When
		        PlayerJoinedGameEvent e3 = service.joinGame(game).invoke(JoinGameCmd.of("Bob")).toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e3.getGameId());
		        assertEquals("Bob", e3.getPlayerId());
		    //When
		        GameStartedEvent e4 = service.startGame(game).invoke().toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e4.getGameId());
		    //When
		        PlayersTurnEndedEvent e5 = service.endTurn(game).invoke(EndTurnCmd.of("Alice", 0)).toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e5.getGameId());
		        assertEquals("Alice", e5.getPlayerId());
		        assertEquals(0, e5.getTurn());
		    //When
		        PlayersTurnEndedEvent e6 = service.endTurn(game).invoke(EndTurnCmd.of("Bob", 1)).toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e6.getGameId());
		        assertEquals("Bob", e6.getPlayerId());
		        assertEquals(1, e6.getTurn());
		    //When
		        PlayersTurnEndedEvent e7 = service.endTurn(game).invoke(EndTurnCmd.of("Alice", 2)).toCompletableFuture().get(5, SECONDS);
		    //Then
		        assertEquals(game, e7.getGameId());
		        assertEquals("Alice", e7.getPlayerId());
		        assertEquals(2, e7.getTurn());
	    });
	}
	
	@Ignore //TODO
	public void testNegative() throws Exception{
		
	}

}
