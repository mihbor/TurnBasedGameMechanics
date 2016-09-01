package mihbor.lagom.game.impl;

import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

public class Game extends PersistentEntity<GameCommand, GameEvent, GameState> {

	@Override
	public PersistentEntity<GameCommand, GameEvent, GameState>.Behavior initialBehavior(Optional<GameState> snapshot) {
		BehaviorBuilder b = newBehaviorBuilder(snapshot.orElse(GameState.EMPTY));
		
		return b.build();
	}

}
