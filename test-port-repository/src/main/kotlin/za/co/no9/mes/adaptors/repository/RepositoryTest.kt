package za.co.no9.mes.adaptors.repository

import io.kotlintest.TestCase
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.mes.domain.ports.Repository
import za.co.no9.mes.domain.ports.UnitOfWork


abstract class RepositoryTest : StringSpec() {
    init {
        "events over a non-empty steam with a page size" {
            val events =
                    unitOfWork.events(pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe startEventsID()
            events[0].content shouldBe "CustomerAdded(name=Luke Skywalker)"

            events[1].id shouldBe startEventsID() + 1
            events[1].content shouldBe "CustomerAdded(name=Ben Kenobi)"
        }


        "events over a non-empty steam with a from and page size" {
            val events =
                    unitOfWork.events(from = startEventsID() + 1, pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe startEventsID() + 2
            events[0].content shouldBe "CustomerAdded(name=Han Solo)"

            events[1].id shouldBe startEventsID() + 3
            events[1].content shouldBe "CustomerAdded(name=Ben Solo)"
        }


        "known event detail" {
            val event =
                    unitOfWork.event(startEventsID() + 1)

            event!!.id shouldBe startEventsID() + 1
            event.content shouldBe "CustomerAdded(name=Ben Kenobi)"

        }


        "unknown event detail" {
            val event =
                    unitOfWork.event(10)

            event shouldBe null
        }


        "publish two events" {
            val event1 =
                    unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))

            val event2 =
                    unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))

            event1.id shouldBeLessThan  event2.id

            event1.content shouldBe "CustomerAdded(name=Luke Skywalker)"
            event1 shouldBe unitOfWork.event(event1.id)
            event2.content shouldBe "CustomerAdded(name=Ben Kenobi)"
            event2 shouldBe unitOfWork.event(event2.id)
        }


        "add a new topic" {
            val topic1 =
                    unitOfWork.saveTopic("*default*")

            topic1.name shouldBe "*default*"
            unitOfWork.topic(topic1.id) shouldBe topic1
        }


        "topics without from" {
            val topics =
                    unitOfWork.topics(pageSize = 2).toList()

            topics.size shouldBe 2

            topics[0].name shouldBe "Topic 1"
            topics[1].name shouldBe "Topic 2"
        }


        "topics with from" {
            val topics =
                    unitOfWork.topics(from = startTopicsID (), pageSize = 2).toList()

            topics.size shouldBe 2

            topics[0].name shouldBe "Topic 2"
            topics[1].name shouldBe "Topic 3"
        }
    }


    val unitOfWork: UnitOfWork
        get() = repository().newUnitOfWork()


    abstract fun repository(): Repository


    abstract fun startEventsID(): Int

    abstract fun startTopicsID(): Int


    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Han Solo"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Solo"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Leia Organa"))

        unitOfWork.saveTopic("Topic 1")
        unitOfWork.saveTopic("Topic 2")
        unitOfWork.saveTopic("Topic 3")
    }
}


data class CustomerAdded(val name: String)


fun customerAddedEvent(name: String): String =
        CustomerAdded(name).toString()
