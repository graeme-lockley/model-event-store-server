package za.co.no9.mes.domain

import za.co.no9.mes.domain.ports.Repository


class Services(val repository: Repository) {
    fun events(from: Int? = null, pageSize: Int = 100): Sequence<Event> =
            repository.newUnitOfWork().events(from, pageSize)


    fun event(id: Int): Event? =
            repository.newUnitOfWork().event(id)


    fun saveEvent(eventName: String, content: String): Event =
            repository.newUnitOfWork().saveEvent(eventName, content)


    fun topic(id: Int): Topic? =
            repository.newUnitOfWork().topic(id)


    fun topics(from: Int? = null, pageSize: Int = 100): Sequence<Topic> =
            repository.newUnitOfWork().topics(from, pageSize)


    fun saveTopic(topicName: String): Topic =
            repository.newUnitOfWork().saveTopic(topicName)


    fun createEventType(eventName: String, topicID: Int): Either<Error, EventType> {
        val topic =
                topic(topicID)

        return if (topic == null) {
            error(UnknownTopicID(topicID))
        } else {
            val eventTypeDTO =
                    repository.newUnitOfWork().saveEventType(eventName, topicID)

            value(EventType(eventTypeDTO.id, eventName, topic))
        }
    }

    fun registerObserver(observer: Observer) {
        repository.register(observer)
    }
}


data class EventType(val id: Int, val name: String, val topic: Topic)