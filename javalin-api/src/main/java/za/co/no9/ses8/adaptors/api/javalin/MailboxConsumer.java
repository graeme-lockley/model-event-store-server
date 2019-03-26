package za.co.no9.ses8.adaptors.api.javalin;

import java.util.concurrent.BlockingDeque;

class MailboxConsumer implements Runnable {
    private BlockingDeque<MailboxTask> queue;

    MailboxConsumer(BlockingDeque<MailboxTask> queue) {
        this.queue = queue;
    }


    @Override
    public void run() {
        while (true) {
            try {
                queue.take().process();
            } catch (Throwable t) {
                System.err.println("Exception thrown in mailbox consumer: " + t.getMessage() + ": ");
            }
        }
    }
}