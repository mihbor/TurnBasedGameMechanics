package mihbor.lagom.game.impl;

import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;

public class GameState {
	public final static GameState EMPTY = new GameState(null, new LinkedHashSet<String>(), false, null);
	
	/* never mutate fields! */
	final String gameId;
	final LinkedHashSet<String> playerIds;
	final boolean isStarted;
	final Long turn;

	private GameState(String gameId, LinkedHashSet<String> playerIds, boolean isStarted, Long turn) {
		this.gameId = gameId;
		this.playerIds = playerIds;
		this.isStarted = isStarted;
		this.turn = turn;
	}

	public int playerCount() {
		return playerIds != null ? playerIds.size() : 0;
	}
	
	public GameState gameProposed(String id) {
		if (this != EMPTY) { // idempotency
			Preconditions.checkArgument(id != null, "id must not be null");
			Preconditions.checkArgument(gameId.equals(id), "game id doesn't match");
			Preconditions.checkState(!isStarted, "game must not have started yet");
			Preconditions.checkState(turn==null, "turn must not have been set yet");
			return this;
		} else { // normal case
			return new GameState(id, playerIds, false, null);
		}
	}
	
	public GameState playerJoinedGame(String playerId) {
		if (playerIds.contains(playerId)) return this; // idempotency
		else { // normal case
			LinkedHashSet<String> newPlayerIds = !playerIds.isEmpty()
				? new LinkedHashSet<>(playerIds)
				: new LinkedHashSet<>();
			newPlayerIds.add(playerId);
			return new GameState(gameId, newPlayerIds, isStarted, turn);
		}
	}
	
	public GameState gameStarted() {
		if (isStarted) return this; // idempotency
		else return new GameState(gameId, playerIds, true, 0L); // normal case
	}

	public GameState playersTurnBegun(String playerId, long turn) {
		if (this.turn == turn) return this; // idempotency, this will also happen on 0th turn
		return new GameState(gameId, playerIds, true, turn); // normal case
	}
}
