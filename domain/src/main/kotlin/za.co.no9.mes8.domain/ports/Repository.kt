package za.co.no9.mes8.domain.ports

import za.co.no9.mes8.domain.Observer


interface Repository {
    fun newUnitOfWork(): UnitOfWork

    fun register(observer: Observer)
}
