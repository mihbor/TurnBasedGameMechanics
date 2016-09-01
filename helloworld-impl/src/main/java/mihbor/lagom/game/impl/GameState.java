package mihbor.lagom.game.impl;

public class GameState {
	public final static GameState EMPTY = new GameState(null);
	
	final String id;

	public GameState (String id) {
		this.id = id;
	}
}
