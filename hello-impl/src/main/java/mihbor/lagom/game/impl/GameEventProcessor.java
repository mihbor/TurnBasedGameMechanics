package mihbor.lagom.game.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import mihbor.lagom.game.api.GameEvent;

public class GameEventProcessor extends CassandraReadSideProcessor<GameEvent> {

	@Override
	public AggregateEventTag<GameEvent> aggregateTag() {
		return GameEvent.Tag.INSTANCE;
	}

	@Override
	public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
		// TODO define event handlers
	    return builder.build();
	}

	@Override
	public CompletionStage<Optional<UUID>> prepare(CassandraSession arg0) {
		// TODO prepare statements, fetch offset
	    return noOffset();
	}

}
