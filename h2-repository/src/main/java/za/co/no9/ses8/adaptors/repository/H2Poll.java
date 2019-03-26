package za.co.no9.ses8.adaptors.repository;

import za.co.no9.ses8.domain.Observer;

import java.util.ArrayList;
import java.util.List;

public class H2Poll implements Runnable {
    private List<Observer> observers =
            new ArrayList<>();

    private final H2 h2;

    private final long sleepDuration;

    private int lastEventID;


    H2Poll(H2 h2, long sleepDuration) {
        this.h2 = h2;
        this.sleepDuration = sleepDuration;
    }


    @Override
    public void run() {
        lastEventID = latestEventID();

        System.out.println("H2 Poll: " + lastEventID);

        while (true) {
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                int eventID = latestEventID();

                System.out.println("H2 Poll: " + lastEventID + ": " + eventID);

                if (lastEventID != eventID) {
                    lastEventID = eventID;
                    notifyObservers();
                }
            } catch (Throwable t) {
                System.err.println("H2 Pool: Error: " + t.getMessage());
            }
        }
    }


    private synchronized void notifyObservers() {
        observers.forEach(Observer::ping);
    }


    synchronized void registerObserver(Observer observer) {
        observers.add(observer);
    }


    private int latestEventID() {
        return ((H2UnitOfWork) h2.newUnitOfWork()).lastEventID();
    }
}
