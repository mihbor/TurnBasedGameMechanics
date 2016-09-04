package mihbor.lagom.game.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class GameStateTest {
	//Experimenting with different style for writing tests

	@Test
	public void testGetPlayerCount() {
		assertTrue(
			//Given
				GameState.EMPTY.gameProposed("1")
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
			GameState state = GameState.EMPTY.gameProposed("1");
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
			GameState state = GameState.EMPTY.gameProposed("1")
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
			GameState state = GameState.EMPTY.gameProposed("1")
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
			GameState state = GameState.EMPTY.gameProposed("1")
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
			.playersTurnBegun("A", 0);
		//Then
			assertEquals("B", state.getNextTurnsPlayersId());
			
		//When
			state = state
			.playersTurnEnded("A")
			.playersTurnBegun("B", 1);
		//Then (still)
			assertEquals("A", state.getNextTurnsPlayersId());
	}

	@Test
	public void testGameProposed() {
		fail("Not yet implemented");
	}

	@Test
	public void testPlayerJoinedGame() {
		fail("Not yet implemented");
	}

	@Test
	public void testGameStarted() {
		fail("Not yet implemented");
	}

	@Test
	public void testPlayersTurnBegun() {
		fail("Not yet implemented");
	}

	@Test
	public void testPlayersTurnEnded() {
		fail("Not yet implemented");
	}

}
