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
import mihbor.lagom.game.api.*;

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
				new PersistentEntityTestDriver<>(system, new GameEntity(), null);
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(ProposeGameCmd.of("abc"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameProposedEvent) reply).getGameId());
			
			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertEquals("abc", ((GameProposedEvent) event).getGameId());
			
			assertEquals("abc", outcome.state().gameId);
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testJoinGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new GameEntity(), null);
			driver.run(ProposeGameCmd.of("abc"));
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(JoinGameCmd.of("Alice"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGameEvent) reply).getGameId());
			assertEquals("Alice", ((PlayerJoinedGameEvent) reply).getPlayerId());

			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertEquals("abc", ((PlayerJoinedGameEvent) event).getGameId());
			assertEquals("Alice", ((PlayerJoinedGameEvent) event).getPlayerId());
			
			assertEquals(1, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(JoinGameCmd.of("Bob"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGameEvent) reply).getGameId());
			assertEquals("Bob", ((PlayerJoinedGameEvent) reply).getPlayerId());
			
			assertEquals(1, outcome.events().size());
			event = outcome.events().get(0);
			assertEquals("abc", ((PlayerJoinedGameEvent) event).getGameId());
			assertEquals("Bob", ((PlayerJoinedGameEvent) event).getPlayerId());
			
			assertEquals(2, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(JoinGameCmd.of("Bob"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGameEvent) reply).getGameId());
			assertEquals("Bob", ((PlayerJoinedGameEvent) reply).getPlayerId());
			
			assertEquals(0, outcome.events().size());

			assertEquals(2, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testStartGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new GameEntity(), null);
			
			driver.run(ProposeGameCmd.of("abc"), 
				JoinGameCmd.of("Alice"), 
				JoinGameCmd.of("Betty"));
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(StartGameCmd.of());
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameStartedEvent) reply).getGameId());
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertEquals("abc", ((GameStartedEvent) event1).getGameId());
			assertEquals("abc", ((PlayersTurnBegunEvent) event2).getGameId());
			assertEquals("Alice", ((PlayersTurnBegunEvent) event2).getPlayerId());
			assertEquals(0, ((PlayersTurnBegunEvent) event2).getTurn());
			
			assertTrue(outcome.state().isStarted);
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(StartGameCmd.of());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameStartedEvent) reply).getGameId());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testEndTurn() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new GameEntity(), null);
			driver.run(ProposeGameCmd.of("abc"), 
				JoinGameCmd.of("Alice"), 
				JoinGameCmd.of("Bob"), 
				StartGameCmd.of());
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(EndTurnCmd.of("Alice", 0));
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEndedEvent) reply).getGameId());
			assertEquals("Alice", ((PlayersTurnEndedEvent) reply).getPlayerId());
			assertEquals(0, ((PlayersTurnEndedEvent) reply).getTurn());
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertTrue(event1 instanceof PlayersTurnEndedEvent);
			assertTrue(event2 instanceof PlayersTurnBegunEvent);
			assertEquals("abc", ((PlayersTurnEndedEvent) event1).getGameId());
			assertEquals("Alice", ((PlayersTurnEndedEvent) event1).getPlayerId());
			assertEquals(0, ((PlayersTurnEndedEvent) event1).getTurn());
			assertEquals("abc", ((PlayersTurnBegunEvent) event2).getGameId());
			assertEquals("Bob", ((PlayersTurnBegunEvent) event2).getPlayerId());
			assertEquals(1, ((PlayersTurnBegunEvent) event2).getTurn());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(EndTurnCmd.of("Bob", 1));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEndedEvent) reply).getGameId());
			assertEquals("Bob", ((PlayersTurnEndedEvent) reply).getPlayerId());
			assertEquals(1, ((PlayersTurnEndedEvent) reply).getTurn());
			
			assertEquals(2, outcome.events().size());
			event1 = outcome.events().get(0);
			event2 = outcome.events().get(1);
			assertTrue(event1 instanceof PlayersTurnEndedEvent);
			assertTrue(event2 instanceof PlayersTurnBegunEvent);
			assertEquals("abc", ((PlayersTurnEndedEvent) event1).getGameId());
			assertEquals("Bob", ((PlayersTurnEndedEvent) event1).getPlayerId());
			assertEquals(1, ((PlayersTurnEndedEvent) event1).getTurn());
			assertEquals("abc", ((PlayersTurnBegunEvent) event2).getGameId());
			assertEquals("Alice", ((PlayersTurnBegunEvent) event2).getPlayerId());
			assertEquals(2, ((PlayersTurnBegunEvent) event2).getTurn());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(EndTurnCmd.of("Bob", 1));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEndedEvent) reply).getGameId());
			assertEquals("Bob", ((PlayersTurnEndedEvent) reply).getPlayerId());
			assertEquals(1, ((PlayersTurnEndedEvent) reply).getTurn());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(EndTurnCmd.of("Bob", 0));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(EndTurnCmd.of("Bob", 2));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(EndTurnCmd.of("Alice", 1));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(EndTurnCmd.of("Alice", 2));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("Alice", ((PlayersTurnEndedEvent) reply).getPlayerId());
			
			assertEquals(2, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}
}