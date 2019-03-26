package za.co.no9.ses8.adaptors.api.javalin;

import com.google.gson.Gson;
import io.javalin.websocket.WsSession;
import za.co.no9.ses8.domain.Event;
import za.co.no9.ses8.domain.Services;

import java.util.Optional;
import java.util.stream.Stream;


public class Session {
    private static final Gson gson =
            new Gson();

    private final Mailbox mailbox;
    private final WsSession wsSession;
    private final Services services;

    private Optional<Integer> fromID =
            Optional.empty();


    Session(Mailbox mailbox, WsSession wsSession, Services services) {
        this.mailbox = mailbox;
        this.wsSession = wsSession;
        this.services = services;
    }


    public void close() {
        try {
            wsSession.close();
        } catch (Throwable t) {
            System.err.println("Error throw whilst closing websocket: " + t.getMessage());
        }
    }


    void ping() {
        if (fromID.isPresent()) {
            postCatchup();
        }
    }


    void refresh() {
        if (fromID.isPresent()) {
            int pageSize =
                    1000;

            while (true) {
                Stream<Event> events =
                        services.events(fromID, pageSize);

                Optional<Integer> last =
                        fromID;

                events.forEach(event -> {
                    String message =
                            gson.toJson(event);

                    System.out.println(message);

                    wsSession.send(message);

                    fromID = Optional.of(event.id);
                });

                if (last.equals(fromID)) {
                    break;
                }
            }
        }
    }


    public void reset(String message) {
        fromID = Optional.of(Integer.parseInt(message));
        ping();
    }


    private void postCatchup() {
        mailbox.postTask(new CatchupTask(this));
    }
}
