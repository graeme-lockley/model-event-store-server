package za.co.no9.mes.adaptors.repository

import io.kotlintest.TestCase
import za.co.no9.mes.domain.ports.Repository


class InMemoryTest : RepositoryTest() {
    val inMemory =
            InMemory()


    override fun repository(): Repository =
            inMemory


    override fun startEventsID(): Int =
            0

    override fun startTopicsID(): Int =
            5


    override fun beforeTest(testCase: TestCase) {
        inMemory.reset()

        super.beforeTest(testCase)
    }

}
