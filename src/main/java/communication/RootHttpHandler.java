package communication;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the requests coming on the root URI path "/".
 */
public class RootHttpHandler implements HttpHandler {


    private RequestExecutorFactory requestExecutorFactory;
    private static final Logger LOGGER = Logger.getLogger( RootHttpHandler.class.getName() );

    public RootHttpHandler(RequestExecutorFactory requestExecutorFactory) {

        this.requestExecutorFactory = requestExecutorFactory;
    }

    public void handle(HttpExchange httpExchange) {

        RequestExecutor requestExecutor;
        try {
            requestExecutor = requestExecutorFactory.getRequestHandler(httpExchange);
        } catch (Throwable t) {
            LOGGER.log( Level.SEVERE, t.getMessage(), t);
            sendResponse(httpExchange, new Response("", HttpURLConnection.HTTP_INTERNAL_ERROR));
            return;
        }

        if (requestExecutor != null) {
            Response response = requestExecutor.execute();
            sendResponse(httpExchange, response);
        } else {
            //unknow URI
            sendResponse(httpExchange, new Response("", HttpURLConnection.HTTP_BAD_REQUEST));
        }
    }

    private void sendResponse(HttpExchange httpExchange, Response response) {

        try {
            httpExchange.sendResponseHeaders(response.getHttpResponseCode(), response.getBody().getBytes().length);
        } catch (IOException ignore) {
            //not much to do besides logging
        }

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBody().getBytes("UTF-8"));
        } catch (IOException ignore) {
            //not much to do besides logging
        }
    }
}


