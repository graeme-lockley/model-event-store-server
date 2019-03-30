package za.co.no9.mes.adaptors.api.javalin


import io.javalin.Javalin
import io.javalin.websocket.WsHandler
import io.javalin.websocket.WsSession
import za.co.no9.mes.domain.Observer
import za.co.no9.mes.domain.Services
import java.util.*
import java.util.function.Consumer


class WebsocketAPI constructor(private val services: Services) : Observer {
    private val sessions = HashMap<String, Session>()

    private val postOffice = PostOffice(10)


    init {
        services.registerObserver(this)
    }


    operator fun invoke(): Consumer<WsHandler> =
            Consumer { ws ->
                ws.onConnect { wsSession: WsSession -> this.connect(wsSession) }
                ws.onMessage { wsSession, message -> this.message(wsSession, message) }
                ws.onClose { wsSession, statusCode, reason -> this.close(wsSession, statusCode, reason) }
                ws.onError { wsSession, throwable -> this.error(wsSession, throwable) }
            }


    @Synchronized
    private fun connect(wsSession: WsSession) {
        println("WsConnect: " + wsSession.id)

        val id = wsSession.id

        sessions[id] = Session(postOffice.mailbox(id), wsSession, services)
    }


    private fun message(wsSession: WsSession, message: String) {
        println("WsMessage: " + wsSession.id + ": " + message)

        val session = sessions[wsSession.id]

        if (session == null) {
            System.err.println("WsMessage: " + wsSession.id + ": " + message + ": Unknown session ID")
        } else {
            session.reset(message)
        }
    }


    private fun close(wsSession: WsSession, statusCode: Int, reason: String?) {
        println("WeClose: " + wsSession.id + ": " + statusCode + ": " + reason)

        removeSession(wsSession.id)
    }


    private fun error(wsSession: WsSession, throwable: Throwable?) {
        println("WsError: " + wsSession.id + ": " + throwable?.message)

        removeSession(wsSession.id)
    }


    @Synchronized
    private fun removeSession(sessionId: String) {
        val session = sessions[sessionId]

        if (session != null) {
            session.close()
            sessions.remove(sessionId)
        }
    }


    @Synchronized
    override fun ping() {
        sessions.forEach { _, session -> session.ping() }
    }
}


fun Javalin.registerWebsocketEndpoints(services: Services): Javalin =
        this.ws("/websocket/events", WebsocketAPI(services).invoke())
