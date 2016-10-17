package mihbor.game.kafka.streams;

import java.util.Map;

import org.apache.kafka.streams.examples.pageview.JsonPOJODeserializer;

import com.fasterxml.jackson.datatype.pcollections.PCollectionsModule;

public class JsonGameStateDeserializer extends JsonPOJODeserializer<GameState> {
	
	public JsonGameStateDeserializer() {
		super();
	    objectMapper.registerModule(new PCollectionsModule());
    	tClass = GameState.class;
	}

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
    	//no-op
    }

}
