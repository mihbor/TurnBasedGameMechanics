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
import mihbor.lagom.game.impl.GameCommand.*;

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
			Outcome<GameEvent, GameState> outcome = driver.run(new ProposeGame("abc"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameProposed) reply).gameId);
			
			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertEquals("abc", ((GameProposed) event).gameId);
			
			assertEquals("abc", outcome.state().gameId);
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testJoinGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			driver.run(new ProposeGame("abc"));
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(new JoinGame("Alice"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Alice", ((PlayerJoinedGame) reply).playerId);

			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertEquals("abc", ((PlayerJoinedGame) event).gameId);
			assertEquals("Alice", ((PlayerJoinedGame) event).playerId);
			
			assertEquals(1, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(new JoinGame("Bob"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) reply).playerId);
			
			assertEquals(1, outcome.events().size());
			event = outcome.events().get(0);
			assertEquals("abc", ((PlayerJoinedGame) event).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) event).playerId);
			
			assertEquals(2, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(new JoinGame("Bob"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) reply).playerId);
			
			assertEquals(0, outcome.events().size());

			assertEquals(2, outcome.state().getPlayerCount());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testStartGame() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			driver.run(new ProposeGame("abc"), new JoinGame("Alice"), new JoinGame("Betty"));
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(new StartGame());
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameStarted) reply).gameId);
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertEquals("abc", ((GameStarted) event1).gameId);
			assertEquals("abc", ((PlayersTurnBegun) event2).gameId);
			assertEquals("Alice", ((PlayersTurnBegun) event2).playerId);
			assertEquals(0, ((PlayersTurnBegun) event2).turn);
			
			assertTrue(outcome.state().isStarted);
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(new StartGame());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((GameStarted) reply).gameId);
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}

	@Test
	public void testEndTurn() {
		//Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			driver.run(new ProposeGame("abc"), new JoinGame("Alice"), new JoinGame("Bob"), new StartGame());
		//When
			Outcome<GameEvent, GameState> outcome = driver.run(new EndTurn("Alice", 0));
		//Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEnded) reply).gameId);
			assertEquals("Alice", ((PlayersTurnEnded) reply).playerId);
			assertEquals(0, ((PlayersTurnEnded) reply).turn);
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertTrue(event1 instanceof PlayersTurnEnded);
			assertTrue(event2 instanceof PlayersTurnBegun);
			assertEquals("abc", ((PlayersTurnEnded) event1).gameId);
			assertEquals("Alice", ((PlayersTurnEnded) event1).playerId);
			assertEquals(0, ((PlayersTurnEnded) event1).turn);
			assertEquals("abc", ((PlayersTurnBegun) event2).gameId);
			assertEquals("Bob", ((PlayersTurnBegun) event2).playerId);
			assertEquals(1, ((PlayersTurnBegun) event2).turn);
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(new EndTurn("Bob", 1));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEnded) reply).gameId);
			assertEquals("Bob", ((PlayersTurnEnded) reply).playerId);
			assertEquals(1, ((PlayersTurnEnded) reply).turn);
			
			assertEquals(2, outcome.events().size());
			event1 = outcome.events().get(0);
			event2 = outcome.events().get(1);
			assertTrue(event1 instanceof PlayersTurnEnded);
			assertTrue(event2 instanceof PlayersTurnBegun);
			assertEquals("abc", ((PlayersTurnEnded) event1).gameId);
			assertEquals("Bob", ((PlayersTurnEnded) event1).playerId);
			assertEquals(1, ((PlayersTurnEnded) event1).turn);
			assertEquals("abc", ((PlayersTurnBegun) event2).gameId);
			assertEquals("Alice", ((PlayersTurnBegun) event2).playerId);
			assertEquals(2, ((PlayersTurnBegun) event2).turn);
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (idempotent)
			outcome = driver.run(new EndTurn("Bob", 1));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("abc", ((PlayersTurnEnded) reply).gameId);
			assertEquals("Bob", ((PlayersTurnEnded) reply).playerId);
			assertEquals(1, ((PlayersTurnEnded) reply).turn);
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(new EndTurn("Bob", 0));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(new EndTurn("Bob", 2));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When (negative)
			outcome = driver.run(new EndTurn("Alice", 1));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("not your turn to end", ((InvalidCommandException)reply).getMessage());
			
			assertEquals(0, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());

		//When
			outcome = driver.run(new EndTurn("Alice", 2));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertEquals("Alice", ((PlayersTurnEnded) reply).playerId);
			
			assertEquals(2, outcome.events().size());
			
			assertEquals(Collections.emptyList(), outcome.issues());
	}
}