package application;

import com.sun.net.httpserver.HttpContext;
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

        AppStarter appStarted = new AppStarter();
        appStarted.start();

    }

    private BetOffersService betOffersService;
    private CustomerSessionService customerSessionService;
    private SimpleHttpServer simpleHttpServer;
    private HttpContext rootContext;

    public  void start() throws IOException {

        Properties properies = new Properties();
        properies.load(AppStarter.class.getClassLoader().getResourceAsStream("application.properties"));

        //init and start services
        betOffersService = new BetOffersService();
        betOffersService.start();
        customerSessionService = new CustomerSessionService();
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
        simpleHttpServer = new SimpleHttpServer(serverHost, serverPort, executor);

        //configure and start http server
         rootContext = simpleHttpServer.setContext("/", httpHandler);

        simpleHttpServer.start();


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
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
