package mihbor.lagom.game.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;
import akka.NotUsed;
import mihbor.lagom.game.api.GameEvent;
import mihbor.lagom.game.api.GameProposedEvent;

public class GameEventProcessor extends CassandraReadSideProcessor<GameEvent> {
	
	private PreparedStatement writeGameProposed;
	private PreparedStatement writeOffset;
	

	@Override
	public AggregateEventTag<GameEvent> aggregateTag() {
		return GameEvent.Tag.INSTANCE;
	}

	private CompletionStage<List<BoundStatement>> processGameProposed(GameProposedEvent evt, UUID offset){
		BoundStatement bs = writeGameProposed.bind();
		bs.setString("id", evt.getGameId());
		return completedStatements(Arrays.asList(bs, writeOffset.bind(offset)));
	}
	
	@Override
	public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
		builder.setEventHandler(GameProposedEvent.class, this::processGameProposed);
	    return builder.build();
	}

	private CompletionStage<Done> createTables(CassandraSession session) {
		return session
			.executeCreateTable("CREATE TABLE IF NOT EXISTS game ("
				+ "id text, PRIMARY KEY (id))")
			.thenCompose(x -> 
				session.executeCreateTable("CREATE TABLE IF NOT EXISTS gameevent_offset ("
				+ "partition int, offset timeuuid, PRIMARY KEY (partition))"));
	}
	
	@Override
	public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
	    return 
	    	createTables(session)
	    	.thenCompose(x -> 
		    	session.prepare("INSERT INTO game (id) VALUES (?)")
		        .thenApply(ps -> {
		            writeGameProposed = ps;
		            return NotUsed.getInstance();
		        })
	    	)
	        .thenCompose(x -> 
	        	session.prepare("INSERT INTO gameevent_offset (partition, offset) VALUES (1, ?)")
				.thenApply(ps -> {
					writeOffset = ps;
					return NotUsed.getInstance();
				})
			)
	        .thenCompose(x -> noOffset());
	}

	private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
		return session.selectOne("SELECT offset FROM gameevent_offset")
			.thenApply(optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
	}
	
}
