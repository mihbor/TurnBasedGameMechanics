package mihbor.lagom.game.kafka.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.junit.*;

import io.confluent.examples.streams.IntegrationTestUtils;
import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;

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
		List<String> inputValues = Arrays.asList(
			"\"{\"type\":\"GameProposedEvent\",\"gameId\":\"Abra\"}",
			"{\"type\":\"GameProposedEvent\",\"gameId\":\"cadabra\"}",
			"{\"type\":\"PlayerJoinedGameEvent\",\"gameId\":\"Abra\",\"playerId\":\"Bob\"}",
			"{\"type\":\"PlayerJoinedGameEvent\",\"gameId\":\"Abra\",\"playerId\":\"Alice\"}",
			"{\"type\":\"PlayerJoinedGameEvent\",\"gameId\":\"cadabra\",\"playerId\":\"Alice\"}",
			"{\"type\":\"PlayerJoinedGameEvent\",\"gameId\":\"cadabra\",\"playerId\":\"Bob\"}",
			"{\"type\":\"PlayerJoinedGameEvent\",\"gameId\":\"cadabra\",\"playerId\":\"Charlie\"}",
			"{\"type\":\"GameStartedEvent\",\"gameId\":\"Abra\"}",
			"{\"type\":\"PlayersTurnBegunEvent\",\"gameId\":\"Abra\",\"playerId\":\"Bob\",\"turn\":0}",
			"{\"type\":\"PlayersTurnEndedEvent\",\"gameId\":\"Abra\",\"playerId\":\"Bob\",\"turn\":0}",
			"{\"type\":\"PlayersTurnBegunEvent\",\"gameId\":\"Abra\",\"playerId\":\"Alice\",\"turn\":1}"
		);

	    final Serde<String> stringSerde = Serdes.String();

	    Properties streamsConfiguration = new Properties();
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "game-event-integration-test");
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
	    streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, CLUSTER.zookeeperConnect());
	    streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
	    
	    IntegrationTestUtils.purgeLocalStreamsState(streamsConfiguration);
	    
	    KStreamBuilder builder = new KStreamBuilder();
	    
	    KStream<Void, String> textLines = builder.stream(inputTopic);
	    KStream<Void, String> out = textLines;
	    out.to(outputTopic);
	    
	    KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
	    streams.start();
	}
}
