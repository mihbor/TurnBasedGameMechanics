package mihbor.lagom.game.api;

import static com.lightbend.lagom.javadsl.api.Service.*;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;

import java.util.List;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;

import akka.NotUsed;

public interface GameService extends Service {
	
	/**
	 * curl -X POST http://localhost:9000/api/proposeGame/abcd
	 */
	ServiceCall<NotUsed, GameProposedEvent> proposeGame(String gameId);
	
	/**
	 * curl -H "Content-Type: application/json" -X POST -d '{"playerId": "Alice"}' http://localhost:9000/api/joinGame/abcd
	 */
	ServiceCall<JoinGameCmd, PlayerJoinedGameEvent> joinGame(String gameId);
	
	/**
	 * curl -X POST http://localhost:9000/api/startGame/abcd
	 */
	ServiceCall<NotUsed, GameStartedEvent> startGame(String gameId);
	
	/**
	 * curl -H "Content-Type: application/json" -X POST -d '{"playerId": "Alice", "turn" : "0"}' http://localhost:9000/api/endTurn/abcd
	 */
	ServiceCall<EndTurnCmd, PlayersTurnEndedEvent> endTurn(String gameId);
	
	ServiceCall<NotUsed, List<String>> getAllGames();
	
	Topic<GameEvent> eventsTopic();

	@Override
	default Descriptor descriptor() {
		return named("helloservice")
			.withCalls(
				restCall(POST, "/api/proposeGame/:gameId", this::proposeGame),
				restCall(POST, "/api/joinGame/:gameId"   , this::joinGame),
				restCall(POST, "/api/startGame/:gameId"  , this::startGame),
				restCall(POST, "/api/endTurn/:gameId"    , this::endTurn),
				restCall(GET , "/api/games"              , this::getAllGames)
			)
			.publishing(topic("game-events", this::eventsTopic))
			.withAutoAcl(true);
	}
}
