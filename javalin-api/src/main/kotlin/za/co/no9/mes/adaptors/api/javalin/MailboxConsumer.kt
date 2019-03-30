package za.co.no9.mes.adaptors.api.javalin

import java.util.concurrent.BlockingDeque


internal class MailboxConsumer(private val queue: BlockingDeque<MailboxTask>) : Runnable {
    override fun run() {
        while (true) {
            try {
                queue.take().process()
            } catch (t: Throwable) {
                System.err.println("Exception thrown in mailbox consumer: " + t.message + ": ")
            }

        }
    }
}