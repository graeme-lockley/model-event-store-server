package za.co.no9.mes.adaptors.repository

import org.jdbi.v3.core.Jdbi
import za.co.no9.mes.domain.Event
import java.sql.Timestamp
import java.time.Instant
import java.util.*


class H2UnitOfWork(private val jdbi: Jdbi) : za.co.no9.mes.domain.ports.UnitOfWork {
    override fun saveEvent(eventName: String, content: String): Event =
            jdbi.withHandle<Event, RuntimeException> { handle ->
                handle.execute("insert into event (when, name, content) values (?, ?, ?)", Timestamp(Date.from(Instant.now()).time), eventName, content)

                handle
                        .createQuery("select id, when, name, content from event where id = SCOPE_IDENTITY()")
                        .map { rs, _ -> Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")) }
                        .findOnly()
            }


    override fun event(id: Int): Event? =
            jdbi.withHandle<Event?, RuntimeException> { handle ->
                handle.select("select id, when, name, content from event where id = ?", id)
                        .map { rs, _ -> Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")) }
                        .findFirst()
                        .orElse(null)
            }


    override fun events(from: Int?, pageSize: Int): Sequence<Event> =
            if (from == null) {
                jdbi.withHandle<List<Event>, RuntimeException> { handle ->
                    handle.select("select id, when, name, content from event order by id limit ?", pageSize)
                            .map { rs, _ -> Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")) }
                            .list()
                }

            } else {
                jdbi.withHandle<List<Event>, RuntimeException> { handle ->
                    handle.select("select id, when, name, content from event where id > ? order by id limit ?", from, pageSize)
                            .map { rs, _ -> Event(rs.getInt("id"), rs.getTimestamp("when"), rs.getString("name"), rs.getString("content")) }
                            .list()
                }
            }.asSequence()


    internal fun lastEventID(): Int =
            jdbi.withHandle<Int, RuntimeException> { handler ->
                handler.createQuery("select max(id) from event")
                        .map { rs, _ -> rs.getInt(1) }
                        .findFirst()
                        .orElse(Integer.MIN_VALUE)
            }
}
