package mihbor.game.kafka.streams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.examples.pageview.JsonPOJODeserializer;
import org.apache.kafka.streams.examples.pageview.JsonPOJOSerializer;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pcollections.TreePVector;

import io.confluent.examples.streams.IntegrationTestUtils;
import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;
import mihbor.lagom.game.api.*;

public class GameEventStreamIT {

	@ClassRule
	public static final EmbeddedSingleNodeKafkaCluster CLUSTER = new EmbeddedSingleNodeKafkaCluster();

	private static final String inputTopic = "game-events";
	private static final String outputTopic = "games";

	@BeforeClass
	public static void startKafkaCluster() throws Exception {
		CLUSTER.createTopic(inputTopic);
		CLUSTER.createTopic(outputTopic);
	}

	@Test
	public void shouldCountWords() throws Exception {
		List<GameEvent> inputValues = Arrays.asList(
			GameProposedEvent.of("Abra"),
			GameProposedEvent.of("cadabra"),
			PlayerJoinedGameEvent.of("Abra","Bob"),
			PlayerJoinedGameEvent.of("Abra","Alice"),
			PlayerJoinedGameEvent.of("cadabra","Alice"),
			PlayerJoinedGameEvent.of("cadabra","Bob"),
			PlayerJoinedGameEvent.of("cadabra","Charlie"),
			GameStartedEvent.of("Abra"),
			PlayersTurnBegunEvent.of("Abra","Bob",0),
			PlayersTurnEndedEvent.of("Abra","Bob",0),
			PlayersTurnBegunEvent.of("Abra","Alice",1)
		);
	    List<KeyValue<String, GameState>> expectedWordCounts = Arrays.asList(
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.empty(), false)),
	        new KeyValue<>("cadabra", GameState.of("cadabra", TreePVector.empty(), false)),
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.singleton("Bob"), false)),
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.singleton("Bob").plus("Alice"), false)),
	        new KeyValue<>("cadabra", GameState.of("cadabra", TreePVector.singleton("Alice"), false)),
	        new KeyValue<>("cadabra", GameState.of("cadabra", TreePVector.singleton("Alice").plus("Bob"), false)),
	        new KeyValue<>("cadabra", GameState.of("cadabra", TreePVector.singleton("Alice").plus("Bob").plus("Charlie"), false)),
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.singleton("Bob").plus("Alice"), true)),
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.singleton("Bob").plus("Alice"), true)),
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.singleton("Bob").plus("Alice"), true)),
	        new KeyValue<>("Abra", GameState.of("Abra", TreePVector.singleton("Bob").plus("Alice"), true))
		);


	    Properties streamsConfiguration = new Properties();
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "game-event-integration-test");
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
	    streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, CLUSTER.zookeeperConnect());
	    streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//	    streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
	    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
	    
	    IntegrationTestUtils.purgeLocalStreamsState(streamsConfiguration);
	    
//	    KStreamBuilder builder = new KStreamBuilder();
	    
//	    KStream<Void, GameEvent> textLines = builder.stream((Serde<Void>)null, jsonSerde(GameEvent.class), inputTopic);
//	    KStream<String, Long> out = textLines
//	    	.map((key, event) -> new KeyValue<>(event.getGameId(), event))
//	      // This will change the stream type from `KStream<String, String>` to
//	      // `KTable<String, Long>` (word -> count).  We must provide a name for
//	      // the resulting KTable, which will be used to name e.g. its associated
//	      // state store and changelog topic.
//	      .countByKey("Counts")
//	      // Convert the `KTable<String, Long>` into a `KStream<String, Long>`.
//	      .toStream();
//	    out.to(Serdes.String(), Serdes.Long(), outputTopic);
//	    
//	    KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
	    StateStoreSupplier gameStore = Stores.create("game-states")
	    	.withKeys(Serdes.String())
	    	.withValues(jsonSerde(GameState.class))
	    	.persistent()
	    	.build();
	    TopologyBuilder builder = new TopologyBuilder();

        Map<String, Object> deserProps = new HashMap<>();
        deserProps.put("JsonPOJOClass", GameEvent.class);
	    final Deserializer<GameEvent> deserializer = new JsonPOJODeserializer<>();
	    deserializer.configure(deserProps, false);
	    Map<String, Object> serProps = new HashMap<>();
        serProps.put("JsonPOJOClass", GameState.class);
	    final Serializer<GameState> serializer = new JsonPOJOSerializer<>();
	    serializer.configure(serProps, false);
	    
	    builder
	    	.addSource("Source", null, deserializer, inputTopic)
	    	.addProcessor("Process", () -> new GameEventStreamProcessor(), "Source")
	    	.addStateStore(gameStore, "Process")
	    	.addSink("Sink", outputTopic, new StringSerializer(), serializer, "Process");
	    KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
	    streams.start();
	    

	    //
	    // Step 2: Produce some input data to the input topic.
	    //
	    Properties producerConfig = new Properties();
	    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
	    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
	    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
	    producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
	    producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerializer.class);
	    IntegrationTestUtils.produceValuesSynchronously(inputTopic, inputValues, producerConfig);

	    //
	    // Step 3: Verify the application's output data.
	    //
	    Properties consumerConfig = new Properties();
	    consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
	    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "wordcount-lambda-integration-test-standard-consumer");
	    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	    consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonGameStateDeserializer.class);
	    List<KeyValue<String, GameState>> actualWordCounts = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(consumerConfig,
	        outputTopic, expectedWordCounts.size(), 300 * 1000L);
	    streams.close();
	    assertThat(actualWordCounts).containsOnlyElementsOf(expectedWordCounts);
	    actualWordCounts.stream().forEach(System.out::println);
	}

	private <T> Serde<T> jsonSerde(Class<T> clazz) {

        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("JsonPOJOClass", clazz);
	    final Deserializer<T> deserializer = new JsonPOJODeserializer<>();
	    deserializer.configure(serdeProps, false);
	    final Serializer<T> serializer = new JsonPOJOSerializer<>();
	    serializer.configure(serdeProps, false);
	    return Serdes.serdeFrom(serializer, deserializer);
	}
}
