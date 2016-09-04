package mihbor.lagom.game.impl;

import static mihbor.lagom.game.impl.GameCommand.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Optional;

import javax.validation.constraints.AssertTrue;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity.InvalidCommandException;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import mihbor.lagom.game.impl.GameEvent.GameProposed;

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
	  PersistentEntityTestDriver<GameCommand, GameEvent, GameState> driver = new PersistentEntityTestDriver<>(
		  system, new Game(), null);
	  Outcome<GameEvent, GameState> outcome = driver.run(new ProposeGame("abc"));
	  assertEquals(1, outcome.getReplies().size());
	  assertTrue(outcome.getReplies().iterator().next() instanceof GameProposed);
	  assertEquals("abc", ((GameProposed)outcome.getReplies().iterator().next()).gameId);
  }
  
}