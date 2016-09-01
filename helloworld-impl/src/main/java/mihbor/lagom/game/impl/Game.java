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
			(cmd, ctx) -> {
				if(state().id == null) {
					return ctx.thenPersist(new GameProposed(cmd.id), evt -> ctx.reply(evt));
				} else { // already proposed, we're idempotent, so reply GameProposed
					assert state().id == cmd.id; // this must hold as this is the identifier for this entity!
					ctx.reply(new GameProposed(state().id));
					return ctx.done();
				}
			}
		);
		
		b.setEventHandler(
			GameProposed.class, evt -> new GameState(evt.id)
		);
		
		return b.build();
	}

}
