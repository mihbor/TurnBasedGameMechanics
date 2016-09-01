package mihbor.lagom.game.impl;

import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import mihbor.lagom.game.impl.GameCommand.ProposeGame;
import mihbor.lagom.game.impl.GameEvent.GameProposed;

public class Game extends PersistentEntity<GameCommand, GameEvent, GameState> {

	@Override
	public PersistentEntity<GameCommand, GameEvent, GameState>.Behavior initialBehavior(Optional<GameState> snapshot) {
		BehaviorBuilder b = newBehaviorBuilder(snapshot.orElse(GameState.EMPTY));

		b.setCommandHandler(
			ProposeGame.class, 
			(cmd, ctx) -> ctx.thenPersist(new GameProposed(cmd.name), evt -> ctx.reply(evt))
		);
		
		return b.build();
	}

}
