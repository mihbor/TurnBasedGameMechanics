package mihbor.game.kafka.streams;

import org.apache.kafka.streams.processor.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class GameEventStreamProcessor implements Processor<Void, String>{

	private ProcessorContext ctx;
	private KeyValueStore<String, String> store;
	

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
		this.store = (KeyValueStore<String, String>) ctx.getStateStore("games");
	}

	@Override
	public void process(Void key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void punctuate(long arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		store.close();
	}
}
