package mihbor.lagom.game.impl;

public class GameState {
	public final static GameState EMPTY = new GameState(null);
	
	final String name;

	public GameState (String name) {
		this.name = name;
	}
}
