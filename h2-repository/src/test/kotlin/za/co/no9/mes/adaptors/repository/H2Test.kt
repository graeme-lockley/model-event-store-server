package za.co.no9.mes.adaptors.repository;

import io.kotlintest.TestCase
import org.jdbi.v3.core.Jdbi
import za.co.no9.jfixture.Fixtures
import za.co.no9.jfixture.FixturesInput
import za.co.no9.jfixture.JDBCHandler
import za.co.no9.mes.domain.ports.Repository


class H2Test : RepositoryTest() {
    private var h2: H2? =
            null


    override fun repository(): Repository =
            h2!!


    override fun startEventsID(): Int =
            1

    override fun startTopicsID(): Int =
            1


    override fun beforeTest(testCase: TestCase) {
        val fixtures =
                Fixtures.process(FixturesInput.fromLocation("resource:initial.yaml"))

        val jdbc =
                Jdbi.create(fixtures.findHandler(JDBCHandler::class.java).get().connection())

        h2 = H2(jdbc)

        super.beforeTest(testCase)
    }
}
