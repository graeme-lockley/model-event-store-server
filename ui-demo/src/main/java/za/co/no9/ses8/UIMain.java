package za.co.no9.ses8;

import io.javalin.Javalin;

import java.io.IOException;


public class UIMain {
    public static void main(String[] args) throws IOException {
        final Javalin server =
                Javalin
                        .create()
                        .port(8081)
                        .enableStaticFiles("/")
                        .disableStartupBanner()
                        .start();

        System.out.println("Server running  - hit enter to stop it...");

        System.in.read();
        server.stop();
    }
}
