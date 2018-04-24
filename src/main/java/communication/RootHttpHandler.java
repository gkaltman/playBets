package communication;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AllServices;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the requests coming on the root URI path "/".
 */
public class RootHttpHandler implements HttpHandler {


    private RequestExecutorFactory requestExecutorFactory;
    private AllServices allServices;
    private static final Logger LOGGER = Logger.getLogger( RootHttpHandler.class.getName() );

    public RootHttpHandler(RequestExecutorFactory requestExecutorFactory, AllServices allServices) {

        this.requestExecutorFactory = requestExecutorFactory;
        this.allServices = allServices;
    }

    public void handle(HttpExchange httpExchange) {

        RequestExecutor requestExecutor;
        try {
            requestExecutor = requestExecutorFactory.getRequestExecutor(httpExchange);
        } catch (Throwable t) {
            LOGGER.log( Level.SEVERE, t.getMessage(), t);
            sendResponse(httpExchange, new Response("", HttpURLConnection.HTTP_INTERNAL_ERROR));
            return;
        }

        if (requestExecutor != null) {
            Response response = requestExecutor.execute(allServices);
            sendResponse(httpExchange, response);
        } else {
            //unknow URI
            sendResponse(httpExchange, new Response("", HttpURLConnection.HTTP_BAD_REQUEST));
        }
    }

    private void sendResponse(HttpExchange httpExchange, Response response) {

        try {
            httpExchange.sendResponseHeaders(response.getHttpResponseCode(), response.getBody().getBytes().length);
        } catch (IOException e) {
            //not much to do besides logging
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBody().getBytes("UTF-8"));
        } catch (IOException e) {
            //not much to do besides logging
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }
}


