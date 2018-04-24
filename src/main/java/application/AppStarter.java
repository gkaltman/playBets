package application;

import communication.RequestExecutorFactory;
import communication.RootHttpHandler;
import communication.SimpleHttpServer;
import service.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application starting point. It also add a shutdown hook for clean shut down.
 */
public class AppStarter {


    private static final Logger LOGGER = Logger.getLogger(AppStarter.class.getName());

    public static void main(String[] args) throws IOException {

        Properties properies = new Properties();
        properies.load(AppStarter.class.getClassLoader().getResourceAsStream("application.properties"));

        AppStarter appStarted = new AppStarter();
        appStarted.start(properies);
    }

    private BetOffersService betOffersService;
    private CustomerSessionService customerSessionService;
    private SimpleHttpServer simpleHttpServer;


    public void start(Properties properties) throws IOException {

        LOGGER.log(Level.INFO, "Starting app...");

        //init and start services
        betOffersService = new BetOffersService();
        CircuitBreaker circuitBreaker = new MaxNumberOfStakesCircuitBreaker(betOffersService);
        betOffersService.setCircuitBreaker(circuitBreaker);
        betOffersService.start();
        customerSessionService = new CustomerSessionService();
        customerSessionService.start();

        //group services
        AllServices allServices = new AllServices();
        allServices.setBetOffersService(betOffersService);
        allServices.setCustomerSessionService(customerSessionService);

        //init http handler + associated
        RequestExecutorFactory requestExecutorFactory = new RequestExecutorFactory();
        RootHttpHandler httpHandler = new RootHttpHandler(requestExecutorFactory, allServices);

        //create http server
        String serverHost = properties.getProperty("httpserver.host", "localhost");
        int serverPort = Integer.valueOf(properties.getProperty("httpserver.port", "8080"));

        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        simpleHttpServer = new SimpleHttpServer(serverHost, serverPort, executor);

        //configure and start http server
        simpleHttpServer.setContext("/", httpHandler);
        simpleHttpServer.start();
        LOGGER.log(Level.INFO, "App started.");


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                LOGGER.log(Level.INFO, "Shutting down app..");
                AppStarter.this.stop();


            }
        });
    }

    public void stop() {

        betOffersService.stop();
        customerSessionService.stop();
        simpleHttpServer.stop();

    }
}
