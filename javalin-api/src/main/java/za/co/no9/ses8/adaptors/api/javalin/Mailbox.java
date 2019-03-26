package za.co.no9.ses8.adaptors.api.javalin;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


class Mailbox {
    private final BlockingDeque<MailboxTask> queue =
            new LinkedBlockingDeque<>();


    Mailbox() {
        new Thread(new MailboxConsumer(queue)).start();
    }


    void postTask(MailboxTask task) {
        queue.add(task);
    }
}