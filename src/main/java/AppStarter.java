import communication.RootHttpHandler;
import communication.SimpleHttpServer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppStarter {


    public static void main(String[] args) throws IOException {


        Properties properies = new Properties();
        properies.load(AppStarter.class.getClassLoader().getResourceAsStream("application.properties"));

        String serverHost = properies.getProperty("httpserver.host", "localhost");
        int serverPort = Integer.valueOf(properies.getProperty("httpserver.port", "8080"));
        int serverTreadsNumber = Integer.valueOf(properies.getProperty("httpserver.threadsNumber", "50"));


        Executor executor = Executors.newFixedThreadPool(serverTreadsNumber);
        final SimpleHttpServer simpleHttpServer = new SimpleHttpServer(serverHost, serverPort, executor);
        RootHttpHandler httpHandler = new RootHttpHandler();
        simpleHttpServer.setContext("/", httpHandler);
        simpleHttpServer.start();


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                simpleHttpServer.stop();
            }
        });
    }

}
