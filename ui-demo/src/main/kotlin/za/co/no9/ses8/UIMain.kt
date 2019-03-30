package za.co.no9.ses8;

import io.javalin.Javalin


fun main() {
    val server =
            Javalin
                    .create()
                    .port(8081)
                    .enableStaticFiles("/")
                    .disableStartupBanner()
                    .start()

    System.out.println("Server running  - hit enter to stop it...")

    System.`in`.read()
    server.stop()
}
