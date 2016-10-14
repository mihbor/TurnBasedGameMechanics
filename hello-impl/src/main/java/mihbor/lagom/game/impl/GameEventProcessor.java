package mihbor.lagom.game.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.*;

import akka.Done;
import mihbor.lagom.game.api.GameEvent;
import mihbor.lagom.game.api.GameProposedEvent;

public class GameEventProcessor extends ReadSideProcessor<GameEvent> {
	
	private final CassandraSession session;
	private final CassandraReadSide readSide;
	
	private PreparedStatement writeGameProposed;
	
	@Inject
	public GameEventProcessor(CassandraSession session, CassandraReadSide readSide) {
		this.session = session;
		this.readSide = readSide;
	}

	@Override
	public PSequence<AggregateEventTag<GameEvent>> aggregateTags() {
		return ConsPStack.singleton(GameEvent.TAG);
	}

	private CompletionStage<List<BoundStatement>> processGameProposed(GameProposedEvent evt){
		BoundStatement bs = writeGameProposed.bind();
		bs.setString("id", evt.getGameId());
		return completedStatements(Arrays.asList(bs));
	}
	
	@Override
	public ReadSideHandler<GameEvent> buildHandler() {
		return readSide.<GameEvent>builder("gameeventoffsets")
			.setGlobalPrepare(this::createTable)
			.setPrepare(tag -> prepare())
			.setEventHandler(GameProposedEvent.class, this::processGameProposed)
			.build();
	}

	private CompletionStage<Done> createTable() {
		return session
			.executeCreateTable("CREATE TABLE IF NOT EXISTS game ("
				+ "id text, PRIMARY KEY (id))");
	}
	
	private CompletionStage<Done> prepare() {
	    return session.prepare("INSERT INTO game (id) VALUES (?)")
	        .thenApply(ps -> {
	            writeGameProposed = ps;
	            return Done.getInstance();
	        });
	}
	
}
