package za.co.no9.ses8.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.javalin.Javalin;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.no9.ses8.adaptors.api.javalin.EventBean;
import za.co.no9.ses8.adaptors.api.javalin.NewEventBean;
import za.co.no9.ses8.adaptors.repository.InMemory;
import za.co.no9.ses8.domain.Services;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class APITest {
    private Gson gson =
            new Gson();

    private Services services;

    private Javalin server;
    
    private String baseURI;


    @BeforeEach
    void before() {
        services =
                new Services(new InMemory());

        server =
                Main.startServer(services, Main.DEFAULT_PORT);
        
        baseURI = 
                "http://localhost:" + Main.DEFAULT_PORT + "/api/";
    }


    @AfterEach
    void after() {
        server.stop();
    }


    @Test
    void knownEvent() throws IOException {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Han Solo\"}");

        String response =
                Request.Get(baseURI + "events/2").execute().returnContent().asString();

        EventBean eventBean =
                gson.fromJson(response, EventBean.class);

        assertEventEquals("CustomerAdded", "Ben Solo", eventBean);
    }


    @Test
    void unknownEvent() throws IOException {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Han Solo\"}");

        Response response =
                Request.Get(baseURI + "events/10").execute();

        Assertions.assertEquals(412, response.returnResponse().getStatusLine().getStatusCode());
    }


    @Test
    void events() throws IOException {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");

        String response =
                Request.Get(baseURI + "events").execute().returnContent().asString();

        List<EventBean> eventBeans =
                toEventBeanList(response);


        assertEquals(2, eventBeans.size());

        assertEventEquals("CustomerAdded", "Luke Skywalker", eventBeans.get(0));
        assertEventEquals("CustomerAdded", "Ben Solo", eventBeans.get(1));
    }


    @Test
    void eventsFrom() throws IOException {
        services.saveEvent("CustomerAdded", "{name: \"Luke Skywalker\"}");
        services.saveEvent("CustomerAdded", "{name: \"Ben Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Han Solo\"}");
        services.saveEvent("CustomerAdded", "{name: \"Leia Organa\"}");

        String response =
                Request.Get(baseURI + "events?start=2").execute().returnContent().asString();

        List<EventBean> eventBeans =
                toEventBeanList(response);

        assertEquals(2, eventBeans.size());

        assertEventEquals("CustomerAdded", "Han Solo", eventBeans.get(0));
        assertEventEquals("CustomerAdded", "Leia Organa", eventBeans.get(1));
    }


    @Test
    void eventsWithDefaultPageSize() throws IOException {
        populateEvents();

        String response =
                Request.Get(baseURI + "events").execute().returnContent().asString();

        List<EventBean> eventBeans =
                toEventBeanList(response);

        assertEquals(100, eventBeans.size());
    }


    @Test
    void eventsFromWithPageSize() throws IOException {
        populateEvents();

        String response =
                Request.Get(baseURI + "events?start=50&pagesize=10").execute().returnContent().asString();

        List<EventBean> eventBeans =
                toEventBeanList(response);

        assertEquals(10, eventBeans.size());
        assertEquals(51, eventBeans.get(0).id);
    }


    @Test
    void saveEvents() throws IOException {
        NewEventBean input =
                new NewEventBean("CharacterAdded", "{name: \"Luke Skywalker\"}");

        String content =
                gson.toJson(input);

        // This is a piece of dummy Get code which causes the flow to wait until the server is ready.  For one or other
        // reason Post does not wait but then fails immediately.
        Request.Get(baseURI + "events/10").execute();

        String response =
                Request.Post(baseURI + "events").bodyString(content, ContentType.APPLICATION_JSON).execute().returnContent().asString();

        EventBean eventBean =
                gson.fromJson(response, EventBean.class);

        assertEventEquals("CharacterAdded", "Luke Skywalker", eventBean);
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


    private List<EventBean> toEventBeanList(String response) {
        Type listType = new TypeToken<ArrayList<EventBean>>() {
        }.getType();

        return gson.fromJson(response, listType);
    }
}
