package za.co.no9.mes.adaptors.api.javalin

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import za.co.no9.mes.adaptors.api.javalin.beans.Event
import za.co.no9.mes.adaptors.api.javalin.beans.NewEvent
import za.co.no9.mes.adaptors.api.javalin.beans.NewTopic
import za.co.no9.mes.adaptors.api.javalin.beans.Topic
import za.co.no9.mes.adaptors.repository.InMemory
import za.co.no9.mes.domain.Services


private val gson =
        Gson()


class APITest : StringSpec() {
    private val inMemory =
            InMemory()

    private val services =
            Services(inMemory)

    private val javalin =
            startServer(services)


    init {
        "save event" {
            val content =
                    CustomerAdded("Luke Skywalker").toString()

            val input =
                    NewEvent("CharacterAdded", content)

            val response =
                    post("events", input)

            val eventBean =
                    gson.fromJson(response, Event::class.java)

            eventBean.name shouldBe "CharacterAdded"
            eventBean.content shouldBe content
        }


        "known event" {
            val response =
                    Request.Get(BASE_URI + "events/2").execute().returnContent().asString()

            val eventBean =
                    gson.fromJson(response, Event::class.java)

            eventBean.name shouldBe "CustomerAdded"
            eventBean.content shouldBe customerAddedEvent("Han Solo")
        }


        "unknown event" {
            val response =
                    Request.Get(BASE_URI + "events/10").execute()

            response.returnResponse().statusLine.statusCode shouldBe 412
        }


        "events" {
            val response =
                    Request.Get(BASE_URI + "events").execute().returnContent().asString()

            val eventBeans = toEventBeanList(response)

            eventBeans!!.size shouldBe 5

            eventBeans.filter { it.name == "CustomerAdded " }.size shouldBe 0

            eventBeans.map { it.content } shouldBe listOf(
                    "Luke Skywalker", "Ben Kenobi", "Han Solo", "Ben Solo", "Leia Organa"
            ).map { CustomerAdded(it).toString() }
        }


        "events from" {
            val response =
                    Request.Get(BASE_URI + "events?start=2").execute().returnContent().asString()

            val eventBeans = toEventBeanList(response)

            eventBeans!!.size shouldBe 2

            eventBeans.filter { it.name == "CustomerAdded " }.size shouldBe 0

            eventBeans[0].content shouldBe CustomerAdded("Ben Solo").toString()
            eventBeans[1].content shouldBe CustomerAdded("Leia Organa").toString()
        }


        "events with pagesize" {
            val response =
                    Request.Get(BASE_URI + "events?pagesize=2").execute().returnContent().asString()

            val eventBeans = toEventBeanList(response)

            eventBeans!!.size shouldBe 2

            eventBeans.filter { it.name == "CustomerAdded " }.size shouldBe 0

            eventBeans[0].content shouldBe CustomerAdded("Luke Skywalker").toString()
            eventBeans[1].content shouldBe CustomerAdded("Ben Kenobi").toString()
        }


        "events from with pagesize" {
            val response =
                    Request.Get(BASE_URI + "events?start=1&pagesize=2").execute().returnContent().asString()

            val eventBeans = toEventBeanList(response)

            eventBeans!!.size shouldBe 2

            eventBeans.filter { it.name == "CustomerAdded " }.size shouldBe 0

            eventBeans[0].content shouldBe CustomerAdded("Han Solo").toString()
            eventBeans[1].content shouldBe CustomerAdded("Ben Solo").toString()
        }


        "save topic" {
            val input =
                    NewTopic("*default*")

            val response =
                    post("topics", input)

            val topicBean =
                    gson.fromJson(response, Topic::class.java)

            topicBean.name shouldBe "*default*"
        }


        "known topic" {
            val topicID =
                    services.saveTopic("*default*").id

            val response =
                    Request.Get("${BASE_URI}topics/$topicID").execute().returnContent().asString()

            val topic =
                    gson.fromJson(response, Topic::class.java)

            topic.name shouldBe "*default*"
        }


        "unknown topic" {
            val response =
                    Request.Get("${BASE_URI}topics/10").execute()

            response.returnResponse().statusLine.statusCode shouldBe 412
        }


        "topics without from" {
            val response =
                    Request.Get(BASE_URI + "topics").execute().returnContent().asString()

            val events =
                    toTopicsList(response)

            events.size shouldBe 3
        }


        "topics with from" {
            val response =
                    Request.Get(BASE_URI + "topics?start=5").execute().returnContent().asString()

            val topics =
                    toTopicsList(response)

            topics.size shouldBe 2
            topics[0].name shouldBe "Topic 2"
            topics[1].name shouldBe "Topic 3"
        }


        "topics with pagesize" {
            val response =
                    Request.Get(BASE_URI + "topics?start=5&pagesize=1").execute().returnContent().asString()

            val topics =
                    toTopicsList(response)

            topics.size shouldBe 1
            topics[0].name shouldBe "Topic 2"
        }
    }


    private fun post(resourceName: String, input: Any): String {
        // This is a piece of dummy Get code which causes the flow to wait until the server is ready.  For one or other
        // reason Post does not wait but then fails immediately.
        Request.Get(BASE_URI + "events/1").execute()

        return Request.Post(BASE_URI + resourceName).bodyString(gson.toJson(input), ContentType.APPLICATION_JSON).execute().returnContent().asString()
    }


    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        inMemory.reset()

        services.saveEvent("CustomerAdded", customerAddedEvent("Luke Skywalker"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Ben Kenobi"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Han Solo"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Ben Solo"))
        services.saveEvent("CustomerAdded", customerAddedEvent("Leia Organa"))


        services.saveTopic("Topic 1")
        services.saveTopic("Topic 2")
        services.saveTopic("Topic 3")
    }


    override fun afterProject() {
        super.afterProject()

        javalin.stop()
    }
}


fun toEventBeanList(response: String): List<Event>? {
    val listType = object : TypeToken<ArrayList<Event>>() {
    }.type

    return gson.fromJson<List<Event>>(response, listType)
}


fun toTopicsList(response: String): List<Topic> {
    val listType = object : TypeToken<ArrayList<Topic>>() {
    }.type

    return gson.fromJson<List<Topic>>(response, listType)!!
}


data class CustomerAdded(val name: String)


fun customerAddedEvent(name: String): String =
        CustomerAdded(name).toString()