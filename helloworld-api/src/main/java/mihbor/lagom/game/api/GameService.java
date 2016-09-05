package mihbor.lagom.game.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import akka.Done;
import akka.NotUsed;

public interface GameService extends Service {
	
	abstract ServiceCall<NotUsed, Done> proposeGame(String id);
	
	abstract ServiceCall<NotUsed, Done> startGame(String id);

	@Override
	default Descriptor descriptor() {
		return named("helloservice").withCalls(
			pathCall("/api/proposeGame/:id", this::proposeGame),
			pathCall("/api/startGame/:id", this::startGame)
		).withAutoAcl(true);
	}
}
