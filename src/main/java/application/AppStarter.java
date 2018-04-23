package application;

import communication.RequestExecutorFactory;
import communication.RootHttpHandler;
import communication.SimpleHttpServer;
import service.BetOffersService;
import service.CustomerSessionService;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppStarter {


    public static void main(String[] args) throws IOException {
        start();
    }

    public static void start() throws IOException {

        Properties properies = new Properties();
        properies.load(AppStarter.class.getClassLoader().getResourceAsStream("application.properties"));

        //init and start services
        BetOffersService betOffersService = new BetOffersService();
        betOffersService.start();
        CustomerSessionService customerSessionService = new CustomerSessionService();
        customerSessionService.start();

        //init http handler + associated
        RequestExecutorFactory requestExecutorFactory = new RequestExecutorFactory();
        requestExecutorFactory.setBetOffersService(betOffersService);
        requestExecutorFactory.setCustomerSessionService(customerSessionService);

        RootHttpHandler httpHandler = new RootHttpHandler(requestExecutorFactory);

        //create http server
        String serverHost = properies.getProperty("httpserver.host", "localhost");
        int serverPort = Integer.valueOf(properies.getProperty("httpserver.port", "8080"));

        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(serverHost, serverPort, executor);

        //configure and start http server
        simpleHttpServer.setContext("/", httpHandler);
        simpleHttpServer.start();


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                betOffersService.stop();
                customerSessionService.stop();
                simpleHttpServer.stop();
            }
        });
    }
}
