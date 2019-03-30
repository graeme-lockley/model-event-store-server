package za.co.no9.mes.adaptors.repository;

import io.kotlintest.Description
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.core.Jdbi
import za.co.no9.jfixture.Fixtures
import za.co.no9.jfixture.FixturesInput
import za.co.no9.jfixture.JDBCHandler
import za.co.no9.mes.domain.ports.UnitOfWork


class H2Test : StringSpec() {
    var h2: H2? =
            null

    init {
        "events over a non-empty steam with a page size" {
            val events =
                    unitOfWork.events(from = null, pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe 1
            events[0].content shouldBe "CustomerAdded(name=Luke Skywalker)"

            events[1].id shouldBe 2
            events[1].content shouldBe "CustomerAdded(name=Ben Kenobi)"
        }


        "events over a non-empty steam with a from and page size" {
            val events =
                    unitOfWork.events(from = 1, pageSize = 2).toList()

            events.size shouldBe 2

            events[0].id shouldBe 2
            events[0].content shouldBe "CustomerAdded(name=Ben Kenobi)"

            events[1].id shouldBe 3
            events[1].content shouldBe "CustomerAdded(name=Han Solo)"
        }


        "known event detail" {
            val event =
                    unitOfWork.event(2)

            event!!.id shouldBe 2
            event.content shouldBe "CustomerAdded(name=Ben Kenobi)"

        }


        "unknown event detail" {
            val event =
                    unitOfWork.event(10)

            event shouldBe null
        }


        "publish two events" {
            val event6 =
                    unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("R2D2"))

            val event7 =
                    unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("C3PIO"))

            event6.id shouldBe 6
            event6.content shouldBe "CustomerAdded(name=R2D2)"
            unitOfWork.event(6) shouldBe event6

            event7.id shouldBe 7
            event7.content shouldBe "CustomerAdded(name=C3PIO)"
            unitOfWork.event(7) shouldBe event7
        }
    }


    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        val fixtures =
                Fixtures.process(FixturesInput.fromLocation("resource:initial.yaml"))

        val jdbc =
                Jdbi.create(fixtures.findHandler(JDBCHandler::class.java).get().connection())

        h2 = H2(jdbc)

        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Han Solo"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Ben Solo"))
        unitOfWork.saveEvent("CustomerAdded", customerAddedEvent("Leia Organa"))
    }


    private val unitOfWork: UnitOfWork
        get() = h2!!.newUnitOfWork()
}

data class CustomerAdded(val name: String)

fun customerAddedEvent(name: String): String =
        CustomerAdded(name).toString()