package mihbor.lagom.game.impl;

import static org.junit.Assert.*;

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
			assertTrue(reply instanceof GameProposed);
			assertEquals("abc", ((GameProposed) reply).gameId);
			
			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertTrue(event instanceof GameProposed);
			assertEquals("abc", ((GameProposed) event).gameId);
			
			assertEquals("abc", outcome.state().gameId);
			// assertEquals(Collections.emptyList(), outcome.issues()); there are serializer warnings
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
			assertTrue(reply instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Alice", ((PlayerJoinedGame) reply).playerId);

			assertEquals(1, outcome.events().size());
			Object event = outcome.events().get(0);
			assertTrue(event instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) event).gameId);
			assertEquals("Alice", ((PlayerJoinedGame) event).playerId);
			
			assertEquals(1, outcome.state().getPlayerCount());

		//When
			outcome = driver.run(new JoinGame("Bob"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertTrue(reply instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) reply).playerId);
			
			assertEquals(1, outcome.events().size());
			event = outcome.events().get(0);
			assertTrue(event instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) event).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) event).playerId);
			
			assertEquals(2, outcome.state().getPlayerCount());

		//When (idempotent)
			outcome = driver.run(new JoinGame("Bob"));
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertTrue(reply instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) reply).playerId);
			
			assertEquals(0, outcome.events().size());

			assertEquals(2, outcome.state().getPlayerCount());
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
			assertTrue(reply instanceof GameStarted);
			assertEquals("abc", ((GameStarted) reply).gameId);
			
			assertEquals(2, outcome.events().size());
			Object event1 = outcome.events().get(0);
			Object event2 = outcome.events().get(1);
			assertTrue(event1 instanceof GameStarted);
			assertTrue(event2 instanceof PlayersTurnBegun);
			assertEquals("abc", ((GameStarted) event1).gameId);
			assertEquals("abc", ((PlayersTurnBegun) event2).gameId);
			assertEquals("Alice", ((PlayersTurnBegun) event2).playerId);
			
			assertTrue(outcome.state().isStarted);

		//When (idempotent)
			outcome = driver.run(new StartGame());
		//Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().get(0);
			assertTrue(reply instanceof GameStarted);
			assertEquals("abc", ((GameStarted) reply).gameId);
			
			assertEquals(0, outcome.events().size());
	}
}