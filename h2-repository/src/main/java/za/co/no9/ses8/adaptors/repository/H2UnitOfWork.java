package za.co.no9.ses8.adaptors.repository;

import org.jdbi.v3.core.Jdbi;
import za.co.no9.ses8.domain.Event;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

public class H2UnitOfWork implements za.co.no9.ses8.domain.ports.UnitOfWork {
    private final Jdbi jdbi;


    public H2UnitOfWork(Jdbi context) {
        this.jdbi = context;
    }

    @Override
    public Event saveEvent(String eventName, String content) {
        return jdbi.withHandle(handle -> {
            handle.execute("insert into event (when, name, content) values (?, ?, ?)", new Timestamp(Date.from(Instant.now()).getTime()), eventName, content);

            return handle
                    .createQuery("select id, when, name, content from event where id = SCOPE_IDENTITY()")
                    .map((rs, ctx) -> new Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")))
                    .findOnly();
        });
    }


    @Override
    public Optional<Event> event(int id) {
        return jdbi.withHandle(handle -> handle
                .select("select id, when, name, content from event where id = ?", id)
                .map((rs, ctx) -> new Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")))
                .findFirst());
    }


    @Override
    public Stream<Event> events(Optional<Integer> from, int pageSize) {
        return from
                .map(id ->
                        jdbi.withHandle(handle -> handle
                                .select("select id, when, name, content from event where id > ? order by id limit ?", id, pageSize)
                                .map((rs, ctx) -> new Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")))
                                .list()))
                .orElseGet(() ->
                        jdbi.withHandle(handle -> handle
                                .select("select id, when, name, content from event order by id limit ?", pageSize)
                                .map((rs, ctx) -> new Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")))
                                .list())).stream();
    }


    int lastEventID() {
        return jdbi.withHandle(handler ->
                handler
                        .createQuery("select max(id) from event")
                        .map((rs, ctx) -> rs.getInt(1))
                        .findFirst()
        ).orElse(Integer.MIN_VALUE);
    }
}
