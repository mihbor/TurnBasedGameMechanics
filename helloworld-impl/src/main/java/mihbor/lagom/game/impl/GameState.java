package mihbor.lagom.game.impl;

import java.util.LinkedHashSet;

public class GameState {
	public final static GameState EMPTY = new GameState(null, new LinkedHashSet<String>());
	
	/* never mutate fields! */
	final String id;
	final LinkedHashSet<String> playerIds;

	private GameState(String id, LinkedHashSet<String> playerIds) {
		this.id = id;
		this.playerIds = playerIds;
	}
	
	public GameState propose(String id) {
		return new GameState(id, playerIds);
	}
	
	public GameState addPlayer(String playerId){
		LinkedHashSet<String> newPlayerIds = !playerIds.isEmpty() ? new LinkedHashSet<>(playerIds) : new LinkedHashSet<>();
		newPlayerIds.add(playerId);
		return new GameState(id, newPlayerIds);
	}
}
