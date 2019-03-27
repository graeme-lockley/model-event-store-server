package za.co.no9.mes.domain.ports

import za.co.no9.mes.domain.Observer


interface Repository {
    fun newUnitOfWork(): UnitOfWork

    fun register(observer: Observer)
}
