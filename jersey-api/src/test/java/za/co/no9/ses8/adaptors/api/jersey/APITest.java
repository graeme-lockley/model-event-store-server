package za.co.no9.ses8.adaptors.api.jersey;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.no9.ses8.adaptors.repository.InMemory;
import za.co.no9.ses8.domain.Services;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class APITest {
    private Services services;

    private HttpServer server;
    private WebTarget target;


    @BeforeEach
    void before() {
        services =
                new Services(new InMemory());

        server =
                Main.startServer(services);

        target =
                ClientBuilder.newClient().target(Main.BASE_URI);
    }


    @AfterEach
    void after() {
        server.shutdownNow();
    }


    @Test
    void knownEvent() {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Han Solo\"}");

        EventBean response =
                target.path("events/2").request().get(EventBean.class);

        assertEventEquals("CustomerAdded", "Ben Solo", response);
    }


    @Test
    void unknownEvent() {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Han Solo\"}");

        Assertions.assertThrows(NotFoundException.class, () -> target.path("events/10").request().get(EventBean.class));
    }


    @Test
    void events() {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");

        List<EventBean> response =
                target.path("events").request().get(new GenericType<List<EventBean>>() {
                });

        assertEquals(2, response.size());

        assertEventEquals("CustomerAdded", "Luke Skywalker", response.get(0));
        assertEventEquals("CustomerAdded", "Ben Solo", response.get(1));
    }


    @Test
    void eventsFrom() {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Han Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Leia Organa\"}");

        List<EventBean> response =
                target.path("events")
                        .queryParam("start", 2)
                        .request()
                        .get(new GenericType<List<EventBean>>() {
                        });

        assertEquals(2, response.size());

        assertEventEquals("CustomerAdded", "Han Solo", response.get(0));
        assertEventEquals("CustomerAdded", "Leia Organa", response.get(1));
    }


    @Test
    void eventsWithDefaultPageSize() {
        populateEvents();

        List<EventBean> response =
                target.path("events").request().get(new GenericType<List<EventBean>>() {
                });

        assertEquals(100, response.size());
    }


    @Test
    void eventsFromWithPageSize() {
        populateEvents();

        List<EventBean> response =
                target.path("events")
                        .queryParam("start", 50)
                        .queryParam("pagesize", 10)
                        .request().get(new GenericType<List<EventBean>>() {
                });

        assertEquals(10, response.size());
        assertEquals(51, response.get(0).id);
    }


    @Test
    void saveEvents() {
        NewEventBean input =
                new NewEventBean("CharacterAdded", "{name: \"Luke Skywalker\"}");

        EventBean response =
                target.path("events").request("application/json").post(Entity.entity(input, MediaType.APPLICATION_JSON), EventBean.class);

        assertEventEquals("CharacterAdded", "Luke Skywalker", response);
    }


    private void assertEventEquals(String eventName, String name, EventBean event) {
        assertEquals(eventName, event.name);
        assertEquals("{name: \"" + name + "\"}", event.content);
    }


    private void populateEvents() {
        for (int lp = 0; lp < 200; lp += 1) {
            services.saveEvent("SomeEventHappened", "{count: " + lp + "}");
        }
    }
}
