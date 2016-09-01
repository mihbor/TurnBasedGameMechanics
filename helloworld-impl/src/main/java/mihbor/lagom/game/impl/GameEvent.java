package mihbor.lagom.game.impl;

public interface GameEvent {
	public final class GameProposed implements GameEvent {}
	public final class PlayerJoinedGame implements GameEvent {}
	public final class GameStarted implements GameEvent {}
	public final class PlayerOrderSet implements GameEvent {}
	public final class PlayersTurnBegun implements GameEvent {}
	public final class PlayersTurnEnded implements GameEvent {}
	public final class ActionTaken implements GameEvent {}
	public final class GameFinished implements GameEvent {}

}
