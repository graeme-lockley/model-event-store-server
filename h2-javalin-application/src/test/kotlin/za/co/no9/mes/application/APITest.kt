package za.co.no9.mes.application

import com.google.gson.Gson
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.apache.http.client.fluent.Request
import za.co.no9.mes.adaptors.api.javalin.EventBean


class APITest : StringSpec({
    val baseURI =
            "http://localhost:$DEFAULT_PORT/api/"


    "known event" {
        val response =
                Request.Get(baseURI + "events/2").execute().returnContent().asString()

        val eventBean =
                Gson().fromJson(response, EventBean::class.java)

        eventBean.name shouldBe "AccountAdded"

    }


    "unknown event" {
        val response =
                Request.Get(baseURI + "events/-2").execute()

        response.returnResponse().statusLine.statusCode shouldBe 412
    }
}) {
    var server: Main? =
            null

    override fun beforeTest(description: Description) {
        super.beforeTest(description)

        server = Main(DEFAULT_PORT, DEFAULT_TEST_JDBC_URL, DEFAULT_JDBC_USER, DEFAULT_JDBC_PASS)
    }

    override fun afterTest(description: Description, result: TestResult) {
        super.afterTest(description, result)

        server!!.shutdown()
    }
}