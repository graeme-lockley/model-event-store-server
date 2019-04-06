package za.co.no9.mes.domain

import za.co.no9.mes.domain.ports.Repository


class Services(val repository: Repository) {
    fun events(from: Int? = null, pageSize: Int = 100): Sequence<Event> =
            repository.newUnitOfWork().events(from, pageSize)


    fun event(id: Int): Event? =
            repository.newUnitOfWork().event(id)


    fun saveEvent(eventName: String, content: String): Event =
            repository.newUnitOfWork().saveEvent(eventName, content)


    fun registerObserver(observer: Observer) {
        repository.register(observer)
    }
}
