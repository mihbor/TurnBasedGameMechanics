package mihbor.lagom.game.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.lightbend.lagom.javadsl.testkit.ServiceTest.TestServer;

import mihbor.lagom.game.api.*;

public class GameServiceTest {
	
	private static TestServer server;
	
	@BeforeClass
	public static void setup(){
		server = startServer(defaultSetup());
	}

	@Test
	public void testHappyPath() throws Exception {
    	//Given
	    	GameService service = server.client(GameService.class);
	    	String game = "Abracadabra";
	    //When
	        GameProposedEvent e1 = service.proposeGame(game).invoke().toCompletableFuture().get(10, SECONDS);
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
	}
	
	@Test
	public void testGetAllGames() throws InterruptedException, ExecutionException, TimeoutException{
    	//Given
	    	GameService service = server.client(GameService.class);
	    	String game1 = "Abra";
	    	String game2 = "cadabra";
	        service.proposeGame(game1).invoke()
	        	.thenCompose(r1 -> service.proposeGame(game2).invoke())
	        	.toCompletableFuture().get(5, SECONDS);
	        
	    //When (retry up to 5 seconds)
	        List<String> results = service.getAllGames().invoke().toCompletableFuture().get(5, SECONDS);
	        int i = 0;
	        while(results.size()<=1 && i < 10000){
	        	Thread.sleep(500);
	        	i+=500;
	        	results = service.getAllGames().invoke().toCompletableFuture().get(5, SECONDS);
	        }
	        if(i>0) System.out.printf("Took %1.1f seconds to get results.\n", i/1000.0);
	    //Then
	        assertEquals(3, results.size());
	}
	
	@Ignore //TODO
	public void testNegative() throws Exception{
		
	}

	@AfterClass
	public static void teardown(){
		if(server != null) server.stop();
	}
}
