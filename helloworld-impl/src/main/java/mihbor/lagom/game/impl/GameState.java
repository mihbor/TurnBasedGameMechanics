package mihbor.lagom.game.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

public class GameState {
	public final static GameState EMPTY = new GameState(null, new LinkedHashSet<String>(), false, null, null);
	
	/* never mutate fields! */
	final String gameId;
	private final LinkedHashSet<String> playerIds;
	final boolean isStarted;
	final Long turn;
	final Integer currentPlayersIndex;

	private GameState(String gameId, LinkedHashSet<String> playerIds, boolean isStarted, Long turn, Integer currentPlayersIndex) {
		this.gameId = gameId;
		this.playerIds = playerIds;
		this.isStarted = isStarted;
		this.turn = turn;
		this.currentPlayersIndex = currentPlayersIndex;
	}

	public int getPlayerCount() {
		return playerIds != null ? playerIds.size() : 0;
	}

	public boolean hasPlayer(String playerId) {
		if (playerIds == null) return false;
		else return playerIds.contains(playerId);
	}
	
	public String getCurrentTurnsPlayersId() {
		Preconditions.checkState(getPlayerCount() > 0, "not players joined yet");
		if(turn == null || turn == 0) return playerIds.iterator().next();
		else return playerIds.stream().skip(currentPlayersIndex).findFirst().get();
	}

	public String getNextTurnsPlayersId() {
		Preconditions.checkState(getPlayerCount() > 0, "not players joined yet");
		if(turn == null || (currentPlayersIndex+1) == getPlayerCount()) return playerIds.iterator().next();
		else return playerIds.stream().skip(currentPlayersIndex+1).findFirst().get();
	}
	
	public GameState gameProposed(String id) {
		Preconditions.checkState(this==EMPTY, "only call propose game on the EMPTY singleton");
		return new GameState(id, playerIds, isStarted, turn, currentPlayersIndex);
	}
	
	public GameState playerJoinedGame(String playerId) {
		if (playerIds.contains(playerId)) return this; // idempotency
		else { // normal case
			LinkedHashSet<String> newPlayerIds = !playerIds.isEmpty()
				? new LinkedHashSet<>(playerIds)
				: new LinkedHashSet<>();
			newPlayerIds.add(playerId);
			return new GameState(gameId, newPlayerIds, isStarted, turn, currentPlayersIndex);
		}
	}
	
	public GameState gameStarted() {
		if (isStarted) return this; // idempotency
		else return new GameState(gameId, playerIds, true, turn, currentPlayersIndex); // normal case
	}

	public GameState playersTurnBegun(String playerId, long turn) {
		Preconditions.checkState(getPlayerCount() > 0, "not players joined yet");
		if (this.turn == turn) return this; // idempotency, this will also happen on 0th turn
		else return new GameState(gameId, playerIds, isStarted, turn, getPlayersIndex(playerId)); // normal case
	}

	private int getPlayersIndex(String playerId) {
		Preconditions.checkArgument(hasPlayer(playerId), "this player hasn't joined");
		int i = 0;
		for(String id : playerIds) {
			if (id.equals(playerId)) return i;
			else i++;
		}
		throw new AssertionError("hasPlayer == true but not found");
	}

	public GameState playersTurnEnded() {
		return this;
	}
}
