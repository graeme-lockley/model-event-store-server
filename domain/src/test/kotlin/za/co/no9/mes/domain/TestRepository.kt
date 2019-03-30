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

    private val savedTopics =
            mutableListOf<TopicRecord>()


    private var idCounter =
            0


    override fun newUnitOfWork(): UnitOfWork =
            TestUnitOfWork(this)


    class TestUnitOfWork(private val repository: TestRepository) : UnitOfWork {
        override fun saveEvent(eventName: String, content: String): Event =
                repository.saveEvent(eventName, content)

        override fun event(id: Int): Event? =
                repository.event(id)

        override fun events(from: Int?, pageSize: Int): Sequence<Event> =
                repository.events(from, pageSize)

        override fun topic(id: Int): Topic? =
                repository.topic(id)

        override fun saveTopic(topicName: String): Topic =
                repository.saveTopic(topicName)
    }

    override fun register(observer: Observer) {
        observers.add(observer)
    }


    private fun notifyObservers() {
        observers.forEach { it.ping() }
    }


    private fun saveEvent(eventName: String, content: String): Event {
        val detail =
                Event(idCounter, Date.from(Instant.now()), eventName, content)

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


    private fun saveTopic(topicName: String): Topic {
        val record =
                TopicRecord(idCounter, topicName)

        savedTopics.add(record)
        idCounter += 1

        return record.asTopic()
    }


    private fun topic(id: Int): Topic? =
            savedTopics.firstOrNull { event -> event.id == id }?.asTopic()


    fun reset() {
        observers.clear()
        savedEvents.clear()
        savedTopics.clear()

        idCounter = 0
    }
}


data class TopicRecord(val id: Int, val name: String) {
    fun asTopic(): Topic =
            Topic(id, name)
}