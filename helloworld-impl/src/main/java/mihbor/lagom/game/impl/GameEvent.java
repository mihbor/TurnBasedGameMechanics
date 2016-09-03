package mihbor.lagom.game.impl;

public interface GameEvent {
	public final class GameProposed implements GameEvent {
		String gameId;
		
		public GameProposed(String gameId) {
			this.gameId = gameId;
		}
	}
	public final class PlayerJoinedGame implements GameEvent {
		String gameId;
		String playerId;
		
		public PlayerJoinedGame(String gameId, String playerId) {
			this.gameId = gameId;
			this.playerId = playerId;
		}
	}
	public final class GameStarted implements GameEvent {}
	public final class PlayerOrderSet implements GameEvent {}
	public final class PlayersTurnBegun implements GameEvent {}
	public final class PlayersTurnEnded implements GameEvent {}
	public final class ActionTaken implements GameEvent {}
	public final class GameFinished implements GameEvent {}

}
