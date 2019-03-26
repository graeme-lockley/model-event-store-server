package za.co.no9.ses8.domain.ports;

import za.co.no9.ses8.domain.Event;

import java.util.Optional;
import java.util.stream.Stream;

public interface UnitOfWork {
    Event saveEvent(String eventName, String content);

    Optional<Event> event(int id);

    Stream<Event> events(Optional<Integer> from, int pageSize);
}
