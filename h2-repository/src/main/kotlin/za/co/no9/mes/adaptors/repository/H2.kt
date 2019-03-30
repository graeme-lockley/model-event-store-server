package za.co.no9.mes.adaptors.repository

import org.jdbi.v3.core.Jdbi
import za.co.no9.mes.domain.Observer
import za.co.no9.mes.domain.ports.Repository
import za.co.no9.mes.domain.ports.UnitOfWork


class H2(private val jdbi: Jdbi) : Repository {
    private val poll: H2Poll = H2Poll(this, 1000)


    init {
        Thread(poll).start()
    }


    override fun newUnitOfWork(): UnitOfWork {
        return H2UnitOfWork(jdbi)
    }


    override fun register(observer: Observer) {
        poll.registerObserver(observer)
    }
}
