package za.co.no9.mes.adaptors.api.javalin

import java.util.concurrent.LinkedBlockingDeque


internal class Mailbox {
    private val queue = LinkedBlockingDeque<MailboxTask>()

    init {
        Thread(MailboxConsumer(queue)).start()
    }


    fun postTask(task: MailboxTask) {
        queue.add(task)
    }
}