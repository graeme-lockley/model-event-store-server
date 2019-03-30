package za.co.no9.mes.domain.ports

import za.co.no9.mes.domain.Event
import za.co.no9.mes.domain.Topic


interface UnitOfWork {
    fun saveEvent(eventName: String, content: String): Event

    fun event(id: Int): Event?

    fun events(from: Int?, pageSize: Int): Sequence<Event>

    fun topic(id: Int): Topic?

    fun saveTopic(topicName: String): Topic
}
