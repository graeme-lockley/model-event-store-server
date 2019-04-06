package za.co.no9.mes.application

import io.javalin.Javalin
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.jdbi.v3.core.Jdbi
import za.co.no9.mes.adaptors.api.javalin.registerAPIEndpoints
import za.co.no9.mes.adaptors.api.javalin.registerWebsocketEndpoints
import za.co.no9.mes.adaptors.repository.SQLServer
import za.co.no9.mes.domain.Services
import java.sql.DriverManager


class Main(private val port: Int, private val services: Services) {
    private var server: Javalin? = null


    init {
        startup()
    }


    constructor(port: Int, jdbcURL: String, username: String, password: String) : this(port, Services(SQLServer(initialiseDatabase(jdbcURL, username, password))))


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
        "jdbc:sqlserver://localhost:1433"

const val DEFAULT_JDBC_USER =
        "sa"

const val DEFAULT_JDBC_PASS =
        "Sa345678"


fun startServer(services: Services, port: Int): Javalin =
        Javalin
                .create()
                .port(port)
                .disableStartupBanner()
                .enableCorsForAllOrigins()
                .registerAPIEndpoints(services)
                .registerWebsocketEndpoints(services)
                .start()


private fun initialiseDatabase(url: String, username: String, password: String): Jdbi {
    val connection =
            DriverManager.getConnection(url, username, password)

    val database =
            liquibase.database.DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))

    val liquibase =
            Liquibase("db.changelog.xml", ClassLoaderResourceAccessor(), database)

    liquibase.update("")

    return Jdbi.create(url, username, password)
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
