package mihbor.game.kafka.streams;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.pcollections.TreePVector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import mihbor.lagom.game.api.GameEvent;
import mihbor.lagom.game.api.GameEvent.GameProposed;
import mihbor.lagom.game.api.GameEvent.GameStarted;
import mihbor.lagom.game.api.GameEvent.PlayerJoinedGame;

public class GameEventStreamProcessor implements Processor<Void, GameEvent> {
	
	@Immutable
	@Style(typeImmutable="*State", allParameters=true)
	@JsonDeserialize
	public interface Game{
		String getGameId();
		TreePVector<String> getPlayerIds();
		boolean getStarted();
	}

	private ProcessorContext ctx;
	private KeyValueStore<String, GameState> store;
	

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
		this.store = (KeyValueStore<String, GameState>) ctx.getStateStore("game-states");
	}

	@Override
	public void process(Void key, GameEvent evt) {
		String gameId = evt.getGameId();
		if(evt instanceof GameProposed) store.put(gameId, GameState.of(gameId, TreePVector.empty(), false));
		else{
			GameState current = store.get(gameId);
			if(evt instanceof PlayerJoinedGame) {
				current = current.withPlayerIds(
					current.getPlayerIds().plus(
						((PlayerJoinedGame)evt).getPlayerId()
					)
				);
			} else if(evt instanceof GameStarted) {
				current = current.withStarted(true);
			}
			store.put(gameId, current);
		}
		ctx.forward(gameId, store.get(gameId));
	}

	@Override
	public void punctuate(long timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		store.close();
	}
}
