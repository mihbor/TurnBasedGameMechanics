package mihbor.lagom.game.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

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

	@Override
	default Descriptor descriptor() {
		return named("helloservice").withCalls(
			restCall(Method.POST, "/api/proposeGame/:gameId", this::proposeGame),
			restCall(Method.POST, "/api/joinGame/:gameId"   , this::joinGame),
			restCall(Method.POST, "/api/startGame/:gameId"  , this::startGame),
			restCall(Method.POST, "/api/endTurn/:gameId"    , this::endTurn)
		).withAutoAcl(true);
	}
}
