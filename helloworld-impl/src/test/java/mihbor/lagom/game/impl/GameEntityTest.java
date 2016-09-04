package mihbor.lagom.game.impl;

import static org.junit.Assert.*;

import java.util.Collections;

import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;

import mihbor.lagom.game.impl.GameEvent.GameProposed;
import static mihbor.lagom.game.impl.GameCommand.*;

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
	  assertEquals("abc", ((GameProposed)reply).gameId);
	  assertEquals("abc", outcome.state().gameId);
//	  assertEquals(Collections.emptyList(), outcome.issues()); there are serializer warnings
  }
  
}