package za.co.no9.mes.adaptors.repository

import io.kotlintest.Description
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class InMemoryTest : StringSpec() {
    private val repository =
            InMemory()

    private val unitOfWork =
            repository.newUnitOfWork();

    init {
        "all events over an empty repository" {
            repository.reset()

            val events =
                    unitOfWork.events(from = null, pageSize = 100)

            events.count() shouldBe 0
        }


        "events over a non-empty steam with a page size" {
            val events =
                    unitOfWork.events(from = null, pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe 0
            events[0].content shouldBe "CustomerAdded(name=Luke Skywalker)"

            events[1].id shouldBe 1
            events[1].content shouldBe "CustomerAdded(name=Ben Kenobi)"
        }


        "events over a non-empty steam with a from and page size" {
            val events =
                    unitOfWork.events(from = 1, pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe 2
            events[0].content shouldBe "CustomerAdded(name=Han Solo)"

            events[1].id shouldBe 3
            events[1].content shouldBe "CustomerAdded(name=Ben Solo)"
        }


        "known event detail" {
            val event =
                    unitOfWork.event(1)

            event!!.id shouldBe 1
            event.content shouldBe "CustomerAdded(name=Ben Kenobi)"

        }


        "unknown event detail" {
            val event =
                    unitOfWork.event(10)

            event shouldBe null
        }


        "publish two events" {
            repository.reset()

            val event1 =
                    unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))

            val event2 =
                    unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))

            event1.id shouldBe 0
            event1.content shouldBe "CustomerAdded(name=Luke Skywalker)"

            event2.id shouldBe 1
            event2.content shouldBe "CustomerAdded(name=Ben Kenobi)"
        }
    }


    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        this.repository.reset()

        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Han Solo"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Solo"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Leia Organa"))
    }
}


data class CustomerAdded(val name: String)


fun customerAddedEvent(name: String): String =
        CustomerAdded(name).toString()