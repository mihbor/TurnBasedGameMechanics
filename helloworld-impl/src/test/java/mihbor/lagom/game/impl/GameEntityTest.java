package mihbor.lagom.game.impl;

import static org.junit.Assert.*;

import java.util.Collections;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity.InvalidCommandException;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

import mihbor.lagom.game.impl.GameEvent.*;

public class GameEntityTest {

	static ActorSystem system;

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		JavaTestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testProposeGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(ProposeGameImpl.builder().gameId("abc").build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameProposed) reply).getGameId());
			
			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertEquals("abc", ((GameProposed) event).getGameId());
			
			assertEquals("abc", outcome.state().gameId);
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testJoinGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			driver.run(ProposeGameImpl.builder().gameId("abc").build());
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(JoinGameImpl.builder().playerId("Alice").build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGame) reply).getGameId());
			assertEquals("Alice", ((PlayerJoinedGame) reply).getPlayerId());

			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertEquals("abc", ((PlayerJoinedGame) event).getGameId());
			assertEquals("Alice", ((PlayerJoinedGame) event).getPlayerId());
			
			assertEquals(1, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(JoinGameImpl.builder().playerId("Bob").build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGame) reply).getGameId());
			assertEquals("Bob", ((PlayerJoinedGame) reply).getPlayerId());
			
			assertEquals(1, outcome.events().size());
			event = outcome.events().get(0);
			assertEquals("abc", ((PlayerJoinedGame) event).getGameId());
			assertEquals("Bob", ((PlayerJoinedGame) event).getPlayerId());
			
			assertEquals(2, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(JoinGameImpl.builder().playerId("Bob").build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGame) reply).getGameId());
			assertEquals("Bob", ((PlayerJoinedGame) reply).getPlayerId());
			
			assertEquals(0, outcome.events().size());

			assertEquals(2, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testStartGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			
			driver.run(ProposeGameImpl.builder().gameId("abc").build(), 
				JoinGameImpl.builder().playerId("Alice").build(), 
				JoinGameImpl.builder().playerId("Betty").build());
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(StartGameImpl.builder().build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameStarted) reply).getGameId());
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertEquals("abc", ((GameStarted) event1).getGameId());
			assertEquals("abc", ((PlayersTurnBegun) event2).getGameId());
			assertEquals("Alice", ((PlayersTurnBegun) event2).getPlayerId());
			assertEquals(0, ((PlayersTurnBegun) event2).getTurn());
			
			assertTrue(outcome.state().isStarted);
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(StartGameImpl.builder().build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameStarted) reply).getGameId());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testEndTurn() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			driver.run(ProposeGameImpl.builder().gameId("abc").build(), 
				JoinGameImpl.builder().playerId("Alice").build(), 
				JoinGameImpl.builder().playerId("Bob").build(), 
				StartGameImpl.builder().build());
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(EndTurnImpl.builder().playerId("Alice").turn(0).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEnded) reply).getGameId());
			assertEquals("Alice", ((PlayersTurnEnded) reply).getPlayerId());
			assertEquals(0, ((PlayersTurnEnded) reply).getTurn());
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertTrue(event1 instanceof PlayersTurnEnded);
			assertTrue(event2 instanceof PlayersTurnBegun);
			assertEquals("abc", ((PlayersTurnEnded) event1).getGameId());
			assertEquals("Alice", ((PlayersTurnEnded) event1).getPlayerId());
			assertEquals(0, ((PlayersTurnEnded) event1).getTurn());
			assertEquals("abc", ((PlayersTurnBegun) event2).getGameId());
			assertEquals("Bob", ((PlayersTurnBegun) event2).getPlayerId());
			assertEquals(1, ((PlayersTurnBegun) event2).getTurn());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(EndTurnImpl.builder().playerId("Bob").turn(1).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEnded) reply).getGameId());
			assertEquals("Bob", ((PlayersTurnEnded) reply).getPlayerId());
			assertEquals(1, ((PlayersTurnEnded) reply).getTurn());
			
			assertEquals(2, outcome.events().size());
			event1 = outcome.events().get(0);
			event2 = outcome.events().get(1);
			assertTrue(event1 instanceof PlayersTurnEnded);
			assertTrue(event2 instanceof PlayersTurnBegun);
			assertEquals("abc", ((PlayersTurnEnded) event1).getGameId());
			assertEquals("Bob", ((PlayersTurnEnded) event1).getPlayerId());
			assertEquals(1, ((PlayersTurnEnded) event1).getTurn());
			assertEquals("abc", ((PlayersTurnBegun) event2).getGameId());
			assertEquals("Alice", ((PlayersTurnBegun) event2).getPlayerId());
			assertEquals(2, ((PlayersTurnBegun) event2).getTurn());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(EndTurnImpl.builder().playerId("Bob").turn(1).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEnded) reply).getGameId());
			assertEquals("Bob", ((PlayersTurnEnded) reply).getPlayerId());
			assertEquals(1, ((PlayersTurnEnded) reply).getTurn());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(EndTurnImpl.builder().playerId("Bob").turn(0).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(EndTurnImpl.builder().playerId("Bob").turn(2).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(EndTurnImpl.builder().playerId("Alice").turn(1).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(EndTurnImpl.builder().playerId("Alice").turn(2).build());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("Alice", ((PlayersTurnEnded) reply).getPlayerId());
			
			assertEquals(2, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}
}