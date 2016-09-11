package mihbor.lagom.game.impl;

import java.beans.Transient;
import java.util.LinkedHashSet;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;

@Immutable
public class GameState implements CompressedJsonable {
	/* immutable fields */
	final String gameId;
	final protected LinkedHashSet<String> playerIds;
	final boolean isStarted;
	final Long turn;
	final Integer currentPlayersIndex;
	final String previousTurnsPlayerId;
	
	public final static GameState EMPTY = new GameState(null, new LinkedHashSet<String>(), false, null, null, null){
		@Override
		public GameState gameProposed(String id) {
			return new GameState(id, playerIds, isStarted, turn, currentPlayersIndex, previousTurnsPlayerId);
		}
	};

	@JsonCreator
	private GameState(
		@JsonProperty("gameId") String gameId, 
		@JsonProperty("playerIds") LinkedHashSet<String> playerIds, 
		@JsonProperty("isStarted") boolean isStarted, 
		@JsonProperty("turn") Long turn, 
		@JsonProperty("currentPlayersIndex") Integer currentPlayersIndex, 
		@JsonProperty("previousTurnsPlayerId") String previousTurnsPlayerId
	) {
		this.gameId = gameId;
		this.playerIds = playerIds;
		this.isStarted = isStarted;
		this.turn = turn;
		this.currentPlayersIndex = currentPlayersIndex;
		this.previousTurnsPlayerId = previousTurnsPlayerId;
	}
	
	public int getPlayerCount() {
		return playerIds != null ? playerIds.size() : 0;
	}

	public boolean hasPlayer(String playerId) {
		if (playerIds == null) return false;
		else return playerIds.contains(playerId);
	}

	public String getPreviousTurnsPlayerId() {
		return previousTurnsPlayerId;
	}
	
	@Transient
	public String getCurrentTurnsPlayersId() {
		Preconditions.checkState(getPlayerCount() > 0, "no players joined yet");
		if(currentPlayersIndex == null) return null;
		else return playerIds.stream().skip(currentPlayersIndex).findFirst().get();
	}

	@Transient
	public String getNextTurnsPlayersId() {
		Preconditions.checkState(getPlayerCount() > 0, "no players joined yet");
		if(!isStarted) //special case
			return playerIds.iterator().next();
		else if(currentPlayersIndex == null) 
			return null;
		else if(currentPlayersIndex+1 == getPlayerCount()) 
			return playerIds.iterator().next(); //cycle back to beginning player
		else 
			return playerIds.stream().skip(currentPlayersIndex+1).findFirst().get();
	}

	private int getPlayersIndex(String playerId) {
		Preconditions.checkArgument(hasPlayer(playerId), "this player hasn't joined");
		int i = 0;
		for(String id : playerIds) {
			if(id.equals(playerId)) return i;
			else i++;
		}
		throw new AssertionError("hasPlayer == true but not found");
	}
	
	/** Only the EMPTY singleton provides implementation for this */
	public GameState gameProposed(String id) {
		throw new IllegalStateException("only call this on the EMPTY singleton");
	}
	
	public GameState playerJoinedGame(String playerId) {
		if(playerIds.contains(playerId)) return this; // idempotency
		else { // normal case
			LinkedHashSet<String> newPlayerIds = !playerIds.isEmpty()
				? new LinkedHashSet<>(playerIds)
				: new LinkedHashSet<>();
			newPlayerIds.add(playerId);
			return new GameState(gameId, newPlayerIds, isStarted, turn, currentPlayersIndex, previousTurnsPlayerId);
		}
	}
	
	public GameState gameStarted() {
		if(isStarted) return this; // idempotency
		else return new GameState(gameId, playerIds, true, turn, currentPlayersIndex, previousTurnsPlayerId); // normal case
	}

	public GameState playersTurnBegun(String playerId, long turn) {
		Preconditions.checkState(getPlayerCount() > 0, "not players joined yet");
		if(this.turn != null && this.turn == turn
			&& currentPlayersIndex != null && getPlayersIndex(playerId) == currentPlayersIndex
		) return this; // idempotency, this will also happen on 0th turn
		else return new GameState(gameId, playerIds, isStarted, turn, getPlayersIndex(playerId), previousTurnsPlayerId); // normal case
	}

	public GameState playersTurnEnded(String playerId) {
		Preconditions.checkNotNull(playerId);
		if(playerId.equals(previousTurnsPlayerId)) return this; //idempotency
		else return new GameState(gameId, playerIds, isStarted, turn, null, playerId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentPlayersIndex == null) ? 0 : currentPlayersIndex.hashCode());
		result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
		result = prime * result + (isStarted ? 1231 : 1237);
		result = prime * result + ((playerIds == null) ? 0 : playerIds.hashCode());
		result = prime * result + ((previousTurnsPlayerId == null) ? 0 : previousTurnsPlayerId.hashCode());
		result = prime * result + ((turn == null) ? 0 : turn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		GameState other = (GameState) obj;
		if (currentPlayersIndex == null) {
			if (other.currentPlayersIndex != null) return false;
		} else if (!currentPlayersIndex.equals(other.currentPlayersIndex)) return false;
		if (gameId == null) {
			if (other.gameId != null) return false;
		} else if (!gameId.equals(other.gameId)) return false;
		if (isStarted != other.isStarted) return false;
		if (playerIds == null) {
			if (other.playerIds != null) return false;
		} else if (!playerIds.equals(other.playerIds)) return false;
		if (previousTurnsPlayerId == null) {
			if (other.previousTurnsPlayerId != null) return false;
		} else if (!previousTurnsPlayerId.equals(other.previousTurnsPlayerId)) return false;
		if (turn == null) {
			if (other.turn != null) return false;
		} else if (!turn.equals(other.turn)) return false;
		return true;
	}

}
