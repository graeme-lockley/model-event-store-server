package za.co.no9.mes.domain

import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class ServicesTest : StringSpec() {
    private val repository =
            TestRepository()

    private val services =
            Services(repository)


    init {
        "all events over an empty repository" {
            repository.reset()

            val events =
                    services.events()

            events.count() shouldBe 0
        }


        "events over a non-empty stream with a page size" {
            val events =
                    services.events(pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe 0
            events[0].content shouldBe "CustomerAdded(name=Luke Skywalker)"

            events[1].id shouldBe 1
            events[1].content shouldBe "CustomerAdded(name=Ben Kenobi)"
        }


        "events over a non-empty stream with a from and page size" {
            val events =
                    services.events(from = 1, pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe 2
            events[0].content shouldBe "CustomerAdded(name=Han Solo)"

            events[1].id shouldBe 3
            events[1].content shouldBe "CustomerAdded(name=Ben Solo)"
        }


        "known event detail" {
            val event =
                    services.event(1)

            event!!.id shouldBe 1
            event.content shouldBe "CustomerAdded(name=Ben Kenobi)"

        }


        "unknown event detail" {
            val event =
                    services.event(10)

            event shouldBe null
        }


        "publish two events" {
            repository.reset()

            val event1 =
                    services.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))

            val event2 =
                    services.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))

            event1.id shouldBe 0
            event1.content shouldBe "CustomerAdded(name=Luke Skywalker)"

            event2.id shouldBe 1
            event2.content shouldBe "CustomerAdded(name=Ben Kenobi)"
        }


        "create a new topic" {
            val topic1 =
                    services.saveTopic("*default*")

            topic1.name shouldBe "*default*"

            services.topic(topic1.id) shouldBe topic1
        }

        "topics without from" {
            val topics =
                    services.topics(pageSize = 2).toList()

            topics.size shouldBe 2

            topics[0].name shouldBe "Topic 1"
            topics[1].name shouldBe "Topic 2"
        }

        "topics with from" {
            val topics =
                    services.topics(from = 5, pageSize = 2).toList()

            topics.size shouldBe 2

            topics[0].name shouldBe "Topic 2"
            topics[1].name shouldBe "Topic 3"
        }


        "create event type with a known topic ID" {
            val eventType =
                    services.createEventType("CustomerAdded", 5).right()!!

            eventType.name shouldBe "CustomerAdded"
            eventType.topic.name shouldBe "Topic 1"
        }

        "attempt to create event type with an unknown topic ID" {
            val eventType =
                    services.createEventType("CustomerAdded", 100).left()!! as UnknownTopicID

            eventType.topicID shouldBe 100
        }
    }


    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        this.repository.reset()

        services.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Han Solo"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Ben Solo"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Leia Organa"))

        services.saveTopic("Topic 1")
        services.saveTopic("Topic 2")
        services.saveTopic("Topic 3")
    }
}


data class CustomerAdded(val name: String)


fun customerAddedEvent(name: String): String =
        CustomerAdded(name).toString()