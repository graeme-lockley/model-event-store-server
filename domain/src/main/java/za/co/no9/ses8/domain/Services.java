package za.co.no9.ses8.domain;

import za.co.no9.ses8.domain.ports.Repository;

import java.util.Optional;
import java.util.stream.Stream;

public class Services {
    private Repository repository;


    public Services(Repository repository) {
        this.repository = repository;
    }


    public Event saveEvent(String eventName, String content) {
        return repository
                .newUnitOfWork()
                .saveEvent(eventName, content);
    }


    public Stream<Event> events(Optional<Integer> from, int pageSize) {
        return repository.newUnitOfWork().events(from, pageSize);
    }


    public Optional<Event> event(int id) {
        return repository
                .newUnitOfWork()
                .event(id);
    }


    public void registerObserver(Observer observer) {
        repository.register(observer);
    }
}
