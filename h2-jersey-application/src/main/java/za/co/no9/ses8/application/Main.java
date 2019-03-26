package za.co.no9.ses8.application;

import io.swagger.jaxrs.config.BeanConfig;
import org.apache.commons.cli.*;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jdbi.v3.core.Jdbi;
import za.co.no9.ses8.adaptors.repository.H2;
import za.co.no9.ses8.domain.Services;

import java.io.IOException;
import java.net.URI;


public class Main {
    public static final String DEFAULT_BASE_URI =
            "http://localhost:8080/api/";

    private static final String DEFAULT_JDBC_URL =
            "jdbc:h2:./h2-jersey-application/target/stream";

    private static final String DEFAULT_JDBC_USER =
            "sa";

    private static final String DEFAULT_JDBC_PASS =
            "";

    private String baseURI;
    private HttpServer server;
    private Services services;

    public Main(String baseURI, Services services) {
        this.baseURI = baseURI;
        this.services = services;

        startup();
    }


    public Main(Services services) {
        this(DEFAULT_BASE_URI, services);
    }


    public Main(String baseURI, String jdbcURL, String username, String password) {
        this(baseURI, new Services(new H2(Jdbi.create(jdbcURL, username, password))));
    }


    public String getBaseURI() {
        return baseURI;
    }


    private HttpServer startServer() {
        String resources = "za.co.no9.ses8.adaptors";
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setBasePath("/api/");
        beanConfig.setResourcePackage(resources);
        beanConfig.setScan(true);

        final ResourceConfig rc =
                new ResourceConfig()
                        .register(new AbstractBinder() {
                            @Override
                            protected void configure() {
                                bind(services).to(Services.class);
                            }
                        })
                        .register(io.swagger.jaxrs.listing.ApiListingResource.class)
                        .register(io.swagger.jaxrs.listing.SwaggerSerializers.class)
                        .packages("za.co.no9.ses8.adaptors");

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseURI), rc);
    }


    private void startup() {
        server = startServer();

        ClassLoader loader = Main.class.getClassLoader();
        CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "swagger-ui/");
        docsHandler.setFileCacheEnabled(false);

        ServerConfiguration cfg = server.getServerConfiguration();
        cfg.addHttpHandler(docsHandler, "/");
    }


    public void shutdown() {
        server.shutdownNow();
    }


    public static void main(String[] args) throws IOException, ParseException {
        Options options =
                new Options();

        options.addOption(new Option("uri", true, "The REST base URI"));
        options.addOption(new Option("dburl", true, "JDBC URL containing events table"));
        options.addOption(new Option("dbuser", true, "JDBC user"));
        options.addOption(new Option("dbpass", true, "JDBC user password"));
        options.addOption(new Option("help", "Print this message"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            (new HelpFormatter()).printHelp("Main", options);
        } else {
            Main main = new Main(
                    cmd.getOptionValue("uri", DEFAULT_BASE_URI),
                    cmd.getOptionValue("db", DEFAULT_JDBC_URL),
                    cmd.getOptionValue("dbuser", DEFAULT_JDBC_USER),
                    cmd.getOptionValue("dbpassword", DEFAULT_JDBC_PASS));

            System.out.println(String.format("Jersey app started with WADL available at %sapplication.wadl\nHit enter to stop it...", main.getBaseURI()));

            System.in.read();

            main.shutdown();
        }
    }
}
