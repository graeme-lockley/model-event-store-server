package za.co.no9.mes.adaptors.api.javalin

import com.google.gson.Gson
import io.javalin.websocket.WsSession
import za.co.no9.mes.domain.Services


class Session internal constructor(private val mailbox: Mailbox, private val wsSession: WsSession, private val services: Services) {

    private var fromID: Int? =
            null


    fun close() {
        try {
            wsSession.close()
        } catch (t: Throwable) {
            System.err.println("Error throw whilst closing websocket: " + t.message)
        }

    }


    internal fun ping() {
        if (fromID != null) {
            postCatchup()
        }
    }


    internal fun refresh() {
        if (fromID != null) {
            val pageSize = 1000

            while (true) {
                val events = services.events(fromID, pageSize)

                val last = fromID

                events.forEach { event ->
                    val message = gson.toJson(event)

                    println(message)

                    wsSession.send(message)

                    fromID = event.id
                }

                if (last == fromID) {
                    break
                }
            }
        }
    }


    fun reset(message: String) {
        fromID = Integer.parseInt(message)
        ping()
    }


    private fun postCatchup() {
        mailbox.postTask(CatchupTask(this))
    }

    companion object {
        private val gson = Gson()
    }
}
