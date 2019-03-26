package za.co.no9.ses8.domain.ports;

import za.co.no9.ses8.domain.Observer;

public interface Repository {
    UnitOfWork newUnitOfWork();

    void register(Observer observer);
}
