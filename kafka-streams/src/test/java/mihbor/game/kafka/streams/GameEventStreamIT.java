package mihbor.game.kafka.streams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.examples.pageview.JsonPOJODeserializer;
import org.apache.kafka.streams.examples.pageview.JsonPOJOSerializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.junit.*;

import com.fasterxml.jackson.databind.JsonNode;

import io.confluent.examples.streams.IntegrationTestUtils;
import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;
import mihbor.lagom.game.api.GameEvent;
import mihbor.lagom.game.api.GameProposedEvent;
import mihbor.lagom.game.api.GameStartedEvent;
import mihbor.lagom.game.api.PlayerJoinedGameEvent;
import mihbor.lagom.game.api.PlayersTurnBegunEvent;
import mihbor.lagom.game.api.PlayersTurnEndedEvent;

import static org.assertj.core.api.Assertions.assertThat;

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
	    List<KeyValue<String, Long>> expectedWordCounts = Arrays.asList(
	        new KeyValue<>("Abra", 1L),
	        new KeyValue<>("cadabra", 1L),
	        new KeyValue<>("Abra", 2L),
	        new KeyValue<>("Abra", 3L),
	        new KeyValue<>("cadabra", 2L),
	        new KeyValue<>("cadabra", 3L),
	        new KeyValue<>("cadabra", 4L),
	        new KeyValue<>("Abra", 4L),
	        new KeyValue<>("Abra", 5L),
	        new KeyValue<>("Abra", 6L),
	        new KeyValue<>("Abra", 7L)
		);

	    final Deserializer<JsonNode> deserializer = new JsonDeserializer();
	    final Serializer<JsonNode> serializer = new JsonSerializer();
	    final Serde<JsonNode> serde = Serdes.serdeFrom(serializer, deserializer);

	    Properties streamsConfiguration = new Properties();
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "game-event-integration-test");
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
	    streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, CLUSTER.zookeeperConnect());
	    streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
	    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
	    
	    IntegrationTestUtils.purgeLocalStreamsState(streamsConfiguration);
	    
	    KStreamBuilder builder = new KStreamBuilder();
	    
	    KStream<Void, JsonNode> textLines = builder.stream((Serde<Void>)null, serde, inputTopic);
	    KStream<String, Long> out = textLines
	    	.map((key, event) -> new KeyValue<>(event.get("gameId").textValue(), event.get("gameId").textValue()))
	      // This will change the stream type from `KStream<String, String>` to
	      // `KTable<String, Long>` (word -> count).  We must provide a name for
	      // the resulting KTable, which will be used to name e.g. its associated
	      // state store and changelog topic.
	      .countByKey("Counts")
	      // Convert the `KTable<String, Long>` into a `KStream<String, Long>`.
	      .toStream();
	    out.to(Serdes.String(), Serdes.Long(), outputTopic);
	    
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
	    consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
	    List<KeyValue<String, Long>> actualWordCounts = IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(consumerConfig,
	        outputTopic, expectedWordCounts.size(), 300 * 1000L);
	    streams.close();
	    assertThat(actualWordCounts).containsOnlyElementsOf(expectedWordCounts);
	    actualWordCounts.stream().forEach(System.out::println);
	}
}
