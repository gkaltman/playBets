package communication;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.xml.ws.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the requests coming on the root URI path "/".
 */
public class RootHttpHandler implements HttpHandler {


    public void handle(HttpExchange httpExchange) throws IOException {

        handle0(httpExchange.getRequestMethod(), httpExchange.getRequestURI(), httpExchange.getResponseBody());
        String response = "All OK";

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        try {
            os.write(response.getBytes());
        } finally {
            os.close();
        }
    }

    private void handle0(String requestMethod, URI requestURI, OutputStream responseBody) {

        if("GET".equalsIgnoreCase(requestMethod)) {

        }
    }

    private void handle0(URI requestURI, String requestMethod) {

    }

    public static void main(String[] args) {

        Pattern sessionPattern = Pattern.compile("^/(\\d+)/session$");
        Pattern stakePattern = Pattern.compile("^/(\\d+)/stake\\?sessionkey=(\\w+)$");
        Pattern highStakePattern = Pattern.compile("^/(\\d+)/highstakes$");


        Matcher m = sessionPattern.matcher("/1234/session");
        if (m.matches()) {
            System.out.println("The quantity is " + m.group(1));
        }

        Matcher m1 = stakePattern.matcher("/888/stake?sessionkey=QWER12A");
        if (m1.matches()) {
            System.out.println("offer if " + m1.group(1));
            System.out.println("sessionkey " + m1.group(2));
        }

        Matcher m3 = highStakePattern.matcher("/888/highstakes");
        if (m3.matches()) {
            System.out.println("stack " + m3.group(1));
        }

        //http://localhost:8001/888/highstakes
    }



}


///http://localhost:8001/1234/session