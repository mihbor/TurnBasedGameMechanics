package mihbor.lagom.game.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import akka.NotUsed;
import mihbor.lagom.game.api.GameEvent.*;

public interface GameService extends Service {
	
	abstract ServiceCall<NotUsed, GameProposed> proposeGame(String gameId);
	
	abstract ServiceCall<NotUsed, PlayerJoinedGame> joinGame(String gameId, String playerId);
	
	abstract ServiceCall<NotUsed, GameStarted> startGame(String gameId);
	
	abstract ServiceCall<NotUsed, PlayersTurnEnded> endTurn(String gameId, String playerId, long turn);

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
