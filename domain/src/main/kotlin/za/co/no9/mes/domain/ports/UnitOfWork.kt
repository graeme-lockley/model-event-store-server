package za.co.no9.mes.domain.ports

import za.co.no9.mes.domain.Event
import za.co.no9.mes.domain.Topic


interface UnitOfWork {
    fun saveEvent(eventName: String, content: String): Event

    fun event(id: Int): Event?

    fun events(from: Int? = null, pageSize: Int): Sequence<Event>


    fun saveTopic(topicName: String): Topic

    fun topic(id: Int): Topic?

    fun topics(from: Int? = null, pageSize: Int): Sequence<Topic>


    fun saveEventType(name: String, topicID: Int): EventTypeDTO
}


data class EventTypeDTO(val id: Int, val name: String, val topicID: Int)
