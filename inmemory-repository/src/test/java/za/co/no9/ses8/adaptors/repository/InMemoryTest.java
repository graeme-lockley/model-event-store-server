package za.co.no9.ses8.adaptors.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.no9.ses8.domain.Event;
import za.co.no9.ses8.domain.ports.UnitOfWork;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class InMemoryTest {
    private UnitOfWork unitOfWork;

    @BeforeEach
    void before() {
        InMemory h2 =
                new InMemory();

        unitOfWork = h2.newUnitOfWork();
    }


    @Test
    void saveEvent() {
        Event event1 =
                saveEvent("Luke Skywalker");

        Event event2 =
                saveEvent("Han Solo");

        assertEquals(1, event1.id);
        assertEquals("CustomerAdded{name='Luke Skywalker'}", event1.content);

        assertEquals(2, event2.id);
        assertEquals("CustomerAdded{name='Han Solo'}", event2.content);
    }


    @Test
    void knownEvent() {
        saveEvent("Luke Skywalker");
        saveEvent("Han Solo");
        saveEvent("R2D2");

        Optional<Event> event =
                unitOfWork.event(2);

        assertTrue(event.isPresent());

        assertEventEquals(event.get(), 2, "CustomerAdded{name='Han Solo'}");
    }


    @Test
    void unKnownEvent() {
        saveEvent("Luke Skywalker");
        saveEvent("Han Solo");
        saveEvent("R2D2");

        Optional<Event> event =
                unitOfWork.event(10);

        assertFalse(event.isPresent());
    }


    @Test
    void events() {
        saveEvent("Luke Skywalker");
        saveEvent("Han Solo");
        saveEvent("R2D2");

        Stream<Event> events =
                unitOfWork.events(Optional.empty(), 100);

        Event[] eventsArray =
                events.toArray(Event[]::new);

        assertEquals(3, eventsArray.length);

        assertEventEquals(eventsArray[0], 1, "CustomerAdded{name='Luke Skywalker'}");
        assertEventEquals(eventsArray[1], 2, "CustomerAdded{name='Han Solo'}");
        assertEventEquals(eventsArray[2], 3, "CustomerAdded{name='R2D2'}");
    }


    @Test
    void eventsWithPageSize() {
        saveEvent("Luke Skywalker");
        saveEvent("Han Solo");
        saveEvent("R2D2");

        Stream<Event> events =
                unitOfWork.events(Optional.empty(), 2);

        Event[] eventsArray =
                events.toArray(Event[]::new);

        assertEquals(2, eventsArray.length);

        assertEventEquals(eventsArray[0], 1, "CustomerAdded{name='Luke Skywalker'}");
        assertEventEquals(eventsArray[1], 2, "CustomerAdded{name='Han Solo'}");
    }


    @Test
    void eventsFrom() {
        saveEvent("Luke Skywalker");
        saveEvent("Han Solo");
        saveEvent("R2D2");

        Stream<Event> events =
                unitOfWork.events(Optional.of(2), 100);

        Event[] eventsArray =
                events.toArray(Event[]::new);


        assertEquals(1, eventsArray.length);

        assertEventEquals(eventsArray[0], 3, "CustomerAdded{name='R2D2'}");
    }


    @Test
    void eventsFromWithPageSize() {
        saveEvent("Luke Skywalker");
        saveEvent("Ben Kenobi");
        saveEvent("Han Solo");
        saveEvent("Ben Solo");
        saveEvent("R2D2");
        saveEvent("Leia Organa");

        Stream<Event> events =
                unitOfWork.events(Optional.of(2), 2);

        Event[] eventsArray =
                events.toArray(Event[]::new);

        assertEquals(2, eventsArray.length);

        assertEventEquals(eventsArray[0], 3, "CustomerAdded{name='Han Solo'}");
        assertEventEquals(eventsArray[1], 4, "CustomerAdded{name='Ben Solo'}");
    }


    private void assertEventEquals(Event event, int id, String content) {
        Assertions.assertEquals(id, event.id);
        Assertions.assertEquals(content, event.content);
    }


    private Event saveEvent(String name) {
        return unitOfWork.saveEvent("CustomerAdded", new CustomerAdded(name).toString());
    }
}