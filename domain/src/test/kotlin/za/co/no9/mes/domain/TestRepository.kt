package za.co.no9.mes.domain

import za.co.no9.mes.domain.ports.Repository
import za.co.no9.mes.domain.ports.UnitOfWork
import java.time.Instant
import java.util.*


class TestRepository : Repository {
    private val observers =
            mutableListOf<Observer>()

    private val savedEvents =
            mutableListOf<Event>()

    private var idCounter =
            0


    override fun newUnitOfWork(): UnitOfWork =
            TestUnitOfWork(this)


    class TestUnitOfWork(val repository: TestRepository) : UnitOfWork {
        override fun saveEvent(eventName: String, content: String): Event =
                repository.saveEvent(eventName, content)

        override fun event(id: Int): Event? =
                repository.event(id)

        override fun events(from: Int?, pageSize: Int): Sequence<Event> =
                repository.events(from, pageSize)

    }

    override fun register(observer: Observer) {
        observers.add(observer)
    }


    private fun notifyObservers() {
        observers.forEach { it.ping() }
    }


    private fun saveEvent(eventName: String, content: String): Event {
        val detail = Event(idCounter, Date.from(Instant.now()), eventName, content)

        savedEvents.add(detail)
        idCounter += 1

        notifyObservers()

        return detail
    }


    private fun event(id: Int): Event? =
            savedEvents.firstOrNull { event -> event.id == id }


    private fun events(from: Int?, pageSize: Int): Sequence<Event> =
            if (from == null)
                savedEvents.take(pageSize).asSequence()
            else
                eventsFrom(from, pageSize)


    private fun eventsFrom(id: Int, pageSize: Int): Sequence<Event> =
            savedEvents.dropWhile { it.id <= id }.take(pageSize).asSequence()


    fun reset() {
        observers.clear()
        savedEvents.clear()

        idCounter = 0
    }
}
