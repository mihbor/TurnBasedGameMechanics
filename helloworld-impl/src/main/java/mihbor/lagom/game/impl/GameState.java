package mihbor.lagom.game.impl;

import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;

public class GameState {
	public final static GameState EMPTY = new GameState(null, new LinkedHashSet<String>(), false);
	
	/* never mutate fields! */
	final String gameId;
	final LinkedHashSet<String> playerIds;
	final boolean isStarted;

	private GameState(String gameId, LinkedHashSet<String> playerIds, boolean isStarted) {
		this.gameId = gameId;
		this.playerIds = playerIds;
		this.isStarted = isStarted;
	}
	
	public GameState gameProposed(String id) {
		if (gameId != null) { // idempotency
			Preconditions.checkArgument(id != null, "id must not be null");
			Preconditions.checkArgument(gameId.equals(id), "illegal attempt to change game id");
			return this;
		} else { // normal case
			return new GameState(id, playerIds, false);
		}
	}
	
	public GameState playerJoinedGame(String playerId) {
		if (playerIds.contains(playerId)) return this; // idempotency
		else { // normal case
			LinkedHashSet<String> newPlayerIds = !playerIds.isEmpty()
				? new LinkedHashSet<>(playerIds)
				: new LinkedHashSet<>();
			newPlayerIds.add(playerId);
			return new GameState(gameId, newPlayerIds, isStarted);
		}
	}
	
	public GameState gameStarted() {
		if (isStarted) return this; // idempotency
		else return new GameState(gameId, playerIds, true); // normal case
	}
}
