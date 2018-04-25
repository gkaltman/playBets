package communication;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class SimpleHttpServer {

    private HttpServer server;
    private int maxDelayUntilShutdownInSec = 3;

    public SimpleHttpServer(String host, int port) throws IOException {

        this(host, port, null);
    }

    public SimpleHttpServer(String host, int port, Executor executor) throws IOException {
        server = HttpServer.create();
        server.setExecutor(executor); //null means default executor.
        server.bind(new InetSocketAddress(port), 0); //0 means: use the OS default on TCP connection backlog.
    }

    public HttpContext setContext(String path, HttpHandler httpHandler) {

        return server.createContext(path, httpHandler);
    }


    public void start() throws IOException {

        server.start();
    }

    public void stop() {

        server.stop(maxDelayUntilShutdownInSec);
    }

    /**
     * When stopping the server, if there are still {@link com.sun.net.httpserver.HttpExchange}s open,
     * the server will be stopped after maxDelayUntilShutdownInSec.</p>
     * If not set, the default (10 seconds) is used.
     */
    public void setMaxDelayUntilServerShutdownInSec(int maxDelayUntilShutdownInSec) {

        this.maxDelayUntilShutdownInSec = maxDelayUntilShutdownInSec;
    }
}
