package za.co.no9.mes.adaptors.repository

import za.co.no9.mes.domain.Observer


class SQLServerPoll internal constructor(private val sqlServer: SQLServer, private val sleepDuration: Long) : Runnable {
    private val observers =
            ArrayList<Observer>()

    private var lastEventID =
            0


    override fun run() {
        lastEventID = latestEventID()

        println("SQLServer Poll: $lastEventID")

        while (true) {
            try {
                Thread.sleep(sleepDuration)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            try {
                val eventID =
                        latestEventID()

                println("sqlServer Poll: $lastEventID: $eventID")

                if (lastEventID != eventID) {
                    lastEventID = eventID
                    notifyObservers()
                }
            } catch (t: Throwable) {
                System.err.println("sqlServer Pool: Error: " + t.message)
            }

        }
    }


    @Synchronized
    private fun notifyObservers() {
        observers.forEach { it.ping() }
    }


    @Synchronized
    internal fun registerObserver(observer: Observer) {
        observers.add(observer)
    }


    private fun latestEventID(): Int {
        return (sqlServer.newUnitOfWork() as SQLServerUnitOfWork).lastEventID()
    }
}
