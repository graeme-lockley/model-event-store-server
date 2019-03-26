package za.co.no9.ses8.adaptors.api.javalin;

import com.google.gson.Gson;
import io.javalin.Javalin;
import org.apache.commons.io.IOUtils;
import za.co.no9.ses8.domain.Event;
import za.co.no9.ses8.domain.Services;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class API {
    private static final Gson gson =
            new Gson();

    private final Services services;

    public API(Services services) {
        this.services = services;
    }


    EventBean saveEvent(NewEventBean newEvent) {
        Event event =
                services.saveEvent(newEvent.name, newEvent.content);

        return EventBean.from(event);
    }


    List<EventBean> getEvents(Optional<Integer> start, int pageSize) {
        return services
                .events(start, pageSize)
                .map(EventBean::from)
                .collect(Collectors.toList());
    }


    Optional<EventBean> getEvent(int id) {
        return services
                .event(id)
                .map(EventBean::from);
    }


    private static Date calculateExpires() {
        Calendar instance =
                Calendar.getInstance();

        instance.add(Calendar.YEAR, 1);

        return instance.getTime();
    }


    public static Javalin registerEndpoints(final Javalin server, final Services services) {
        final API api =
                new API(services);

        server.get("/api/events/:id", ctx -> {
            Optional<EventBean> event = api.getEvent(Integer.parseInt(ctx.pathParam("id")));

            if (event.isPresent()) {
                ctx.header("Content-Type", "application/json");
                ctx.header("Expires", calculateExpires().toGMTString());
                ctx.result(gson.toJson(event.get()));
            } else {
                ctx.status(412);
            }
        });

        server.get("/api/events", ctx -> {
            Optional<Integer> start =
                    Optional.ofNullable(ctx.queryParam("start")).map(Integer::parseInt);

            int pageSize =
                    Integer.parseInt(ctx.queryParam("pagesize", "100"));

            List<EventBean> events =
                    api.getEvents(start, pageSize);

            ctx.header("Content-Type", "application/json");
            if (events.size() == pageSize) {
                ctx.header("Expires", calculateExpires().toGMTString());
            }
            ctx.result(gson.toJson(events));
        });

        server.post("/api/events", ctx -> {
            NewEventBean newEventBean =
                    gson.fromJson(ctx.body(), NewEventBean.class);

            EventBean eventBean =
                    api.saveEvent(newEventBean);

            ctx.header("Content-Type", "application/json");
            ctx.result(gson.toJson(eventBean));
        });

        server.get("/api", ctx -> {
            try (InputStream resourceAsStream =
                         API.class.getResourceAsStream("/swagger.yaml")) {

                ctx.result(IOUtils.toString(resourceAsStream));
            }
        });

        return server;
    }
}