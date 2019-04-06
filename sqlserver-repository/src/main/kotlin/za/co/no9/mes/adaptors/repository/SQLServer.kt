package za.co.no9.mes.adaptors.repository

import org.jdbi.v3.core.Jdbi
import za.co.no9.mes.domain.Observer
import za.co.no9.mes.domain.ports.Repository
import za.co.no9.mes.domain.ports.UnitOfWork


class SQLServer(private val jdbi: Jdbi) : Repository {
    private val poll: SQLServerPoll = SQLServerPoll(this, 1000)


    init {
        Thread(poll).start()
    }


    override fun newUnitOfWork(): UnitOfWork {
        return SQLServerUnitOfWork(jdbi)
    }


    override fun register(observer: Observer) {
        poll.registerObserver(observer)
    }
}
