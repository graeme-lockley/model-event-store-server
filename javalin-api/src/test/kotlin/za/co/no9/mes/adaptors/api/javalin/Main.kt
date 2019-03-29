package za.co.no9.mes.adaptors.api.javalin

import io.javalin.Javalin
import za.co.no9.mes.adaptors.repository.InMemory
import za.co.no9.mes.domain.Services
import za.co.no9.mes.domain.ports.Repository
import java.util.*


val BASE_URI =
        "http://localhost:8080/api/"


fun startServer(services: Services): Javalin =
        Javalin.create()
                .port(8080)
                .disableStartupBanner()
                .enableCorsForAllOrigins()
                .registerAPIEndpoints(services)
                .start()


fun main() {
    val repository =
            InMemory()

    val services =
            Services(repository)

    val server =
            startServer(services)

    insertRows(repository)

    println("Server running - hit enter to stop it...")

    System.`in`.read()
    server.stop()
}


private fun insertRows(repository: Repository) {
    val unitOfWork =
            repository.newUnitOfWork()

    var lp = 0
    while (lp < 1000000) {
        when (lp % 3) {
            0 -> unitOfWork.saveEvent("CustomerAdded",
                    "{name: \"" + randomString(1, 'A', 'Z') + randomString(8, 'a', 'z') + "\"," +
                            "customerID: " + lp + "}")

            1 -> unitOfWork.saveEvent("AccountAdded",
                    "{productID: \"CUR023\"," +
                            "number: \"" + randomString(10, '0', '9') + "\"," +
                            "customerID: " + (lp - 1) + "}")

            2 -> unitOfWork.saveEvent("AccountAdded",
                    "{productID: \"CUR027\"," +
                            "number: \"" + randomString(10, '0', '9') + "\"," +
                            "customerID: " + (lp - 2) + "}")
        }

        lp += 1
    }
}


private fun randomString(targetStringLength: Int, leftLimit: Char, rightLimit: Char): String {
    val random =
            Random()

    val buffer =
            StringBuilder(targetStringLength)

    for (i in 0 until targetStringLength) {
        val randomLimitedInt =
                (leftLimit.toInt() + (random.nextFloat() * (rightLimit - leftLimit + 1)).toInt())

        buffer.append(randomLimitedInt.toChar())
    }

    return buffer.toString()
}

