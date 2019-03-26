package za.co.no9.ses8.adaptors.repository;

import za.co.no9.ses8.domain.Event;
import za.co.no9.ses8.domain.Observer;
import za.co.no9.ses8.domain.ports.Repository;
import za.co.no9.ses8.domain.ports.UnitOfWork;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class InMemory implements Repository {
    private List<Observer> observers =
            new ArrayList<>();

    private List<Event> savedEvents =
            new ArrayList<>();

    private int idCounter =
            1;


    @Override
    public UnitOfWork newUnitOfWork() {
        return new UnitOfWork() {
            @Override
            public Event saveEvent(String eventName, String content) {
                return InMemory.this.saveEvent(eventName, content);
            }

            @Override
            public Optional<Event> event(int id) {
                return InMemory.this.event(id);
            }

            @Override
            public Stream<Event> events(Optional<Integer> from, int pageSize) {
                return InMemory.this.events(from, pageSize);
            }
        };
    }


    @Override
    public synchronized void register(za.co.no9.ses8.domain.Observer observer) {
        observers.add(observer);
    }


    private synchronized void notifyObservers() {
        observers.forEach(Observer::ping);
    }


    private Event saveEvent(String eventName, String content) {
        Event detail =
                new Event(idCounter, Date.from(Instant.now()), eventName, content);

        savedEvents.add(detail);
        idCounter += 1;

        notifyObservers();

        return detail;
    }


    private Optional<Event> event(int id) {
        return savedEvents.stream().filter(event -> event.id == id).findFirst();
    }


    private Stream<Event> events(Optional<Integer> from, int pageSize) {
        return from
                .map(id -> eventsFrom(id, pageSize))
                .orElseGet(() -> savedEvents.stream().limit(pageSize));
    }


    private Stream<Event> eventsFrom(int id, int pageSize) {
        int index =
                0;

        int lengthOfSavedEvents =
                savedEvents.size();

        while (true) {
            if (index < lengthOfSavedEvents) {
                if (savedEvents.get(index).id <= id) {
                    index += 1;
                } else {
                    Iterator<Event> sourceIterator =
                            savedEvents.listIterator(index);

                    Iterable<Event> iterable =
                            () -> sourceIterator;

                    return StreamSupport.stream(iterable.spliterator(), false).limit(pageSize);
                }
            } else {
                return Stream.empty();
            }
        }
    }


    public void reset() {
        savedEvents.clear();
    }
}