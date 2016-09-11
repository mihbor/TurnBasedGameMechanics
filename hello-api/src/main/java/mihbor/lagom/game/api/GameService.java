package mihbor.lagom.game.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import akka.NotUsed;

public interface GameService extends Service {
	
	abstract ServiceCall<NotUsed, GameProposedEvent> proposeGame(String gameId);
	
	abstract ServiceCall<NotUsed, PlayerJoinedGameEvent> joinGame(String gameId, String playerId);
	
	abstract ServiceCall<NotUsed, GameStartedEvent> startGame(String gameId);
	
	abstract ServiceCall<NotUsed, PlayersTurnEndedEvent> endTurn(String gameId, String playerId, long turn);

	@Override
	default Descriptor descriptor() {
		return named("helloservice").withCalls(
			pathCall("/api/proposeGame/:gameId", this::proposeGame),
			pathCall("/api/joinGame/:gameId/:playerId", this::joinGame),
			pathCall("/api/startGame/:gameId", this::startGame),
			pathCall("/api/endTurn/:gameId/:playerId/:turn", this::endTurn)
		).withAutoAcl(true);
	}
}
