package za.co.no9.mes.adaptors.api.javalin

import com.google.gson.Gson
import io.javalin.Javalin
import org.apache.commons.io.IOUtils
import za.co.no9.mes.adaptors.api.javalin.beans.NewEvent
import za.co.no9.mes.adaptors.api.javalin.beans.NewTopic
import za.co.no9.mes.domain.Event
import za.co.no9.mes.domain.Services
import za.co.no9.mes.domain.Topic
import java.util.*


class API(private val services: Services) {
    internal fun saveEvent(newEvent: NewEvent): za.co.no9.mes.adaptors.api.javalin.beans.Event =
            services
                    .saveEvent(newEvent.name, newEvent.content)
                    .from()


    internal fun getEvents(start: Int?, pageSize: Int): List<za.co.no9.mes.adaptors.api.javalin.beans.Event> =
            services
                    .events(start, pageSize)
                    .map { it.from() }
                    .toList()


    internal fun getEvent(id: Int): za.co.no9.mes.adaptors.api.javalin.beans.Event? =
            services.event(id)?.from()


    fun saveTopic(newTopic: NewTopic): za.co.no9.mes.adaptors.api.javalin.beans.Topic =
            services
                    .saveTopic(newTopic.name)
                    .from()


    internal fun getTopic(id: Int): za.co.no9.mes.adaptors.api.javalin.beans.Topic? =
            services.topic(id)?.from()


    internal fun getTopics(start: Int?, pageSize: Int): List<za.co.no9.mes.adaptors.api.javalin.beans.Topic> =
            services
                    .topics(start, pageSize)
                    .map { it.from() }
                    .toList()
}


fun Javalin.registerAPIEndpoints(services: Services): Javalin {
    val api =
            API(services)

    val gson =
            Gson()


    this.get("/api/events/:id") { ctx ->
        val event =
                api.getEvent(Integer.parseInt(ctx.pathParam("id")))

        if (event == null) {
            ctx.status(412)
        } else {
            ctx.header("Content-Type", "application/json")
            ctx.header("Expires", calculateExpires().toGMTString())
            ctx.result(gson.toJson(event))
        }
    }


    this.get("/api/events") { ctx ->
        val start =
                ctx.queryParam("start")?.toInt()

        val pageSize =
                ctx.queryParam("pagesize", "100")?.toInt() ?: 100

        val events =
                api.getEvents(start, pageSize)

        ctx.header("Content-Type", "application/json")
        if (events.size == pageSize) {
            ctx.header("Expires", calculateExpires().toGMTString())
        }
        ctx.result(gson.toJson(events))
    }


    this.post("/api/events") { ctx ->
        val newEventBean =
                gson.fromJson(ctx.body(), NewEvent::class.java)

        val event: za.co.no9.mes.adaptors.api.javalin.beans.Event =
                api.saveEvent(newEventBean)

        ctx.header("Content-Type", "application/json")
        ctx.result(gson.toJson(event))
    }


    this.get("/api/topics/:id") { ctx ->
        val topic =
                api.getTopic(Integer.parseInt(ctx.pathParam("id")))

        if (topic == null) {
            ctx.status(412)
        } else {
            ctx.header("Content-Type", "application/json")
            ctx.header("Expires", calculateExpires().toGMTString())
            ctx.result(gson.toJson(topic))
        }
    }


    this.get("/api/topics") { ctx ->
        val start =
                ctx.queryParam("start")?.toInt()

        val pageSize =
                ctx.queryParam("pagesize", "100")?.toInt() ?: 100

        val topics =
                api.getTopics(start, pageSize)

        ctx.header("Content-Type", "application/json")
        if (topics.size == pageSize) {
            ctx.header("Expires", calculateExpires().toGMTString())
        }
        ctx.result(gson.toJson(topics))
    }


    this.post("/api/topics") { ctx ->
        val newTopicBean =
                gson.fromJson(ctx.body(), NewTopic::class.java)

        val topic =
                api.saveTopic(newTopicBean)

        ctx.header("Content-Type", "application/json")
        ctx.result(gson.toJson(topic))
    }


    this.get("/api") { ctx ->
        API::class.java.getResourceAsStream("/swagger.yaml").use { resourceAsStream ->
            ctx.result(IOUtils.toString(resourceAsStream))
        }
    }

    return this
}


private fun calculateExpires(): Date {
    val instance =
            Calendar.getInstance()

    instance.add(Calendar.YEAR, 1)

    return instance.time
}


private fun Event.from(): za.co.no9.mes.adaptors.api.javalin.beans.Event =
        za.co.no9.mes.adaptors.api.javalin.beans.Event(id, `when`, name, content)


private fun Topic.from(): za.co.no9.mes.adaptors.api.javalin.beans.Topic =
        za.co.no9.mes.adaptors.api.javalin.beans.Topic(id, name)

