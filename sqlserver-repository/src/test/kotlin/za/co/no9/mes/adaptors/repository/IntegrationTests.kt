package za.co.no9.mes.adaptors.repository

import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.core.Jdbi


class IntegrationTest : StringSpec({
    val jdbcURL =
            "jdbc:sqlserver://localhost:" + (System.getProperty("it-database.port") ?: "1433")

    val unitOfWork =
            SQLServer(Jdbi.create(jdbcURL, "sa", "Sa345678")).newUnitOfWork()


    "save an event" {
        val newEvent =
                unitOfWork.saveEvent("BobSentMe", "hello world")

        newEvent.name shouldBe "BobSentMe"
        newEvent.content shouldBe "hello world"
    }


    "lastEventId increases when an event is saved" {
        val beforeLastEventId =
                (unitOfWork as SQLServerUnitOfWork).lastEventID()

        val newEvent =
                unitOfWork.saveEvent("BobSentMe", "hello world")

        val afterLastEventId =
                unitOfWork.lastEventID()

        beforeLastEventId shouldBeLessThan newEvent.id
        newEvent.id shouldBeLessThanOrEqual afterLastEventId
        beforeLastEventId shouldBeLessThan afterLastEventId
    }


    "select an event" {
        val newEvent =
                unitOfWork.saveEvent("BobSentMe", "hello world")

        val selectedEvent =
                unitOfWork.event(newEvent.id)

        selectedEvent shouldBe newEvent
    }


    "events from" {
        val sourceEvents =
                unitOfWork.events(from = 10, pageSize = 400).drop(1).toList()

        val targetEvents =
                unitOfWork.events(from = 11, pageSize = 399).toList()

        sourceEvents shouldBe targetEvents
    }


    "events without from" {
        val sourceEvents =
                unitOfWork.events(pageSize = 400).toList()

        val targetEvents =
                unitOfWork.events(from = -1, pageSize = 400).toList()

        sourceEvents shouldBe targetEvents
    }
})