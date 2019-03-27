package za.co.no9.mes8.domain.ports

import za.co.no9.mes8.domain.Event


interface UnitOfWork {
    fun saveEvent(eventName: String, content: String): Event

    fun event(id: Int): Event?

    fun events(from: Int?, pageSize: Int): Sequence<Event>
}
