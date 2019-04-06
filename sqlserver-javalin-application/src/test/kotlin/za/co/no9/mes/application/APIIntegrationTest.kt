package za.co.no9.mes.application

import com.google.gson.Gson
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.apache.http.client.fluent.Request
import za.co.no9.mes.adaptors.api.javalin.beans.Event


class APIIntegrationTest : StringSpec({
    val baseURI =
            "http://localhost:$DEFAULT_PORT/api/"


    "known event" {
        val response =
                Request.Get(baseURI + "events/2").execute().returnContent().asString()

        val eventBean =
                Gson().fromJson(response, Event::class.java)

        eventBean.name shouldBe "AccountAdded"

    }


    "unknown event" {
        val response =
                Request.Get(baseURI + "events/-2").execute()

        response.returnResponse().statusLine.statusCode shouldBe 412
    }
}) {
    private var server: Main? =
            null

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        server = Main(DEFAULT_PORT, DEFAULT_JDBC_URL, DEFAULT_JDBC_USER, DEFAULT_JDBC_PASS)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)

        server!!.shutdown()
    }
}