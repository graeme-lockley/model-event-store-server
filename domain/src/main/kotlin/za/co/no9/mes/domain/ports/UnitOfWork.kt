package za.co.no9.mes.domain.ports

import za.co.no9.mes.domain.Event


interface UnitOfWork {
    fun saveEvent(eventName: String, content: String): Event

    fun event(id: Int): Event?

    fun events(from: Int? = null, pageSize: Int): Sequence<Event>
}
