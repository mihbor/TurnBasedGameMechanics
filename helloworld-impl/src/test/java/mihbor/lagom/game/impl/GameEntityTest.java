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
			Object reply = outcome.getReplies().iterator().next();
			assertTrue(reply instanceof GameProposed);
			assertEquals("abc", ((GameProposed) reply).gameId);
			assertEquals("abc", outcome.state().gameId);
			// assertEquals(Collections.emptyList(), outcome.issues()); there are serializer warnings
	}

	@Test
	public void testJoinGame() {
		// Given
			PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = 
				new PersistentEntityTestDriver<>(system, new Game(), null);
			driver.run(new ProposeGame("abc"));
		// When
			Outcome<GameEvent, GameState> outcome = driver.run(new JoinGame("Alice"));
		// Then
			assertEquals(1, outcome.getReplies().size());
			Object reply = outcome.getReplies().iterator().next();
			assertTrue(reply instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Alice", ((PlayerJoinedGame) reply).playerId);
			assertEquals(1, outcome.state().getPlayerCount());

		// When
			outcome = driver.run(new JoinGame("Bob"));
		// Then
			assertEquals(1, outcome.getReplies().size());
			reply = outcome.getReplies().iterator().next();
			assertTrue(reply instanceof PlayerJoinedGame);
			assertEquals("abc", ((PlayerJoinedGame) reply).gameId);
			assertEquals("Bob", ((PlayerJoinedGame) reply).playerId);
			assertEquals(2, outcome.state().getPlayerCount());
	}

}