package za.co.no9.mes.application

import io.javalin.Javalin
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.jdbi.v3.core.Jdbi
import za.co.no9.mes.adaptors.api.javalin.registerAPIEndpoints
import za.co.no9.mes.adaptors.repository.H2
import za.co.no9.mes.domain.Services


class Main(private val port: Int, private val services: Services) {
    private var server: Javalin? = null


    init {
        startup()
    }


    constructor(port: Int, jdbcURL: String, username: String, password: String) : this(port, Services(H2(Jdbi.create(jdbcURL, username, password)))) {}


    private fun startup() {
        server = startServer(services, port)
    }


    fun shutdown() {
        server!!.stop()
    }
}


const val DEFAULT_PORT =
        8080

const val DEFAULT_JDBC_URL =
        "jdbc:h2:./h2-javalin-application/target/stream"

const val DEFAULT_TEST_JDBC_URL =
        "jdbc:h2:./target/stream"

const val DEFAULT_JDBC_USER =
        "sa"

const val DEFAULT_JDBC_PASS =
        ""


fun startServer(services: Services, port: Int): Javalin {
    val javalin = Javalin
            .create()
            .port(port)
            .disableStartupBanner()
            .enableCorsForAllOrigins()
            .registerAPIEndpoints(services)
            .start()

//            WebsocketAPI.registerEndpoints(javalin, services)

    return javalin
}


fun main(args: Array<String>) {
    val options =
            Options()

    options.addOption(Option("port", true, "API port"))
    options.addOption(Option("dburl", true, "JDBC URL containing events table"))
    options.addOption(Option("dbuser", true, "JDBC user"))
    options.addOption(Option("dbpass", true, "JDBC user password"))
    options.addOption(Option("help", "Print this message"))

    val parser =
            DefaultParser()

    val cmd =
            parser.parse(options, args)

    if (cmd.hasOption("help")) {
        HelpFormatter().printHelp("Main", options)
    } else {
        val main = Main(
                Integer.parseInt(cmd.getOptionValue("port", Integer.toString(DEFAULT_PORT))),
                cmd.getOptionValue("db", DEFAULT_JDBC_URL),
                cmd.getOptionValue("dbuser", DEFAULT_JDBC_USER),
                cmd.getOptionValue("dbpassword", DEFAULT_JDBC_PASS))

        println("Javalin app started - hit enter to stop it...")

        System.`in`.read()

        main.shutdown()
    }
}
