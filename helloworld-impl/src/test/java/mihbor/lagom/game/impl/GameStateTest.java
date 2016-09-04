package mihbor.lagom.game.impl;

import static mihbor.lagom.game.impl.GameState.EMPTY;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class GameStateTest {
	//Experimenting with different style for writing tests

	@Test
	public void testGetPlayerCount() {
		assertTrue(
			//Given
				EMPTY.gameProposed("1")
			//When
				.playerJoinedGame("A")
				.playerJoinedGame("B")
			//Then
				.getPlayerCount() == 2
		);
	}

	@Test
	public void testHasPlayer() {
		//Given
			GameState state = EMPTY.gameProposed("1");
		//When
			state = state
			.playerJoinedGame("A")
			.playerJoinedGame("B");
		//Then
			assertTrue(state.hasPlayer("A"));
			assertTrue(state.hasPlayer("B"));
			assertFalse(state.hasPlayer("C"));
			
		//When
			state = state
			.playerJoinedGame("C");
		//Then
			assertTrue(state.hasPlayer("A"));
			assertTrue(state.hasPlayer("B"));
			assertTrue(state.hasPlayer("C"));
	}

	@Test
	public void testGetPreviousTurnsPlayerId() {
		//Given
			GameState state = EMPTY.gameProposed("1")
				.playerJoinedGame("A")
				.playerJoinedGame("B")
				.gameStarted();
		//When
			state = state
			.playersTurnBegun("A", 0);
		//Then
			assertNull(state.getPreviousTurnsPlayerId());
			
		//When
			state = state
			.playersTurnEnded("A");
		//Then
			assertEquals("A", state.getPreviousTurnsPlayerId());
			
		//When
			state = state
			.playersTurnBegun("B", 1);
		//Then (still)
			assertEquals("A", state.getPreviousTurnsPlayerId());
			
		//When
			state = state
			.playersTurnEnded("B");
		//Then
			assertEquals("B", state.getPreviousTurnsPlayerId());
	}

	@Test
	public void testGetCurrentTurnsPlayersId() {
		//Given
			GameState state = EMPTY.gameProposed("1")
				.playerJoinedGame("A")
				.playerJoinedGame("B")
				.gameStarted();
		//When
			state = state
			.playersTurnBegun("A", 0);
		//Then
			assertEquals("A", state.getCurrentTurnsPlayersId());
			
		//When
			state = state
			.playersTurnEnded("A");
		//Then
			assertNull(state.getCurrentTurnsPlayersId());
			
		//When
			state = state
			.playersTurnBegun("B", 1);
		//Then (still)
			assertEquals("B", state.getCurrentTurnsPlayersId());
	}

	@Test
	public void testGetNextTurnsPlayersId() {
		//Given
			GameState state = EMPTY.gameProposed("1")
		//When
			.playerJoinedGame("A");
		//Then
			assertEquals("A", state.getNextTurnsPlayersId());
			
		//When
			state = state.playerJoinedGame("B");
		//Then (still)
			assertEquals("A", state.getNextTurnsPlayersId());
			
		//When
			state = state
			.gameStarted()
			//don't test in between these at it's irrelevant
			.playersTurnBegun("A", 0);
		//Then
			assertEquals("B", state.getNextTurnsPlayersId());
			
		//When
			state = state
			.playersTurnEnded("A")
			//don't test in between these at it's irrelevant
			.playersTurnBegun("B", 1);
		//Then (still)
			assertEquals("A", state.getNextTurnsPlayersId());
	}

	@Test
	public void testGameProposed() {
		assertNull(EMPTY.gameId);
		GameState state = GameState.EMPTY.gameProposed("123");
		assertNotEquals(EMPTY, state);
		assertEquals("123", state.gameId);
		try{
			state.gameProposed("abc");
			fail("gameProposed should not be possible on a non-empty game");
		} catch (Throwable t) {/*correct*/}
	}

	@Test
	public void testPlayerJoinedGame() {
		//Given
			GameState state = GameState.EMPTY.gameProposed("abc");
		//When
			GameState newState = state.playerJoinedGame("Alice");
		//Then
			assertNotEquals(newState, state);
			assertEquals(1, newState.getPlayerCount());
			
		//When (idempotent)
			GameState newerState = newState.playerJoinedGame("Alice");
		//Then
			assertEquals(newState, newerState);

		//When
			newerState = newState.playerJoinedGame("Bob");
		//Then
			assertNotEquals(newState, newerState);
			assertEquals(2, newerState.getPlayerCount());
	}

	@Test
	public void testGameStarted() {
		//Given
			GameState state = GameState.EMPTY
				.gameProposed("abc")
				.playerJoinedGame("Alice");
			assertFalse(state.isStarted);
		//When
			GameState newState = state.gameStarted();
		//Then
			assertNotEquals(state, newState);
			assertTrue(newState.isStarted);
			
		//When (idempotent)
			GameState newerState = newState.gameStarted();
		//Then
			assertEquals(newState, newerState);
	}

	@Test
	public void testPlayersTurnBegun() {
		//Given
			GameState state = GameState.EMPTY
				.gameProposed("abc")
				.playerJoinedGame("Alice")
				.playerJoinedGame("Bob")
				.gameStarted();
			assertNull(state.turn);
			assertNull(state.currentPlayersIndex);
		//When
			GameState newState = state.playersTurnBegun("Alice", 0);
		//Then
			assertNotEquals(state, newState);
			assertEquals(0L, newState.turn.longValue());
			assertEquals(0, newState.currentPlayersIndex.intValue());
			
		//When
			GameState newerState = newState.playersTurnBegun("Bob", 1);
		//Then
			assertNotEquals(newState, newerState);
			assertEquals(1L, newerState.turn.longValue());
			assertEquals(1, newerState.currentPlayersIndex.intValue());
			
		//When (idempotent)
			GameState newestState = newerState.playersTurnBegun("Bob", 1);
		//Then
			assertEquals(newerState, newestState);
	}

	@Ignore
	public void testPlayersTurnEnded() {
		fail("Not yet implemented");
	}

}
