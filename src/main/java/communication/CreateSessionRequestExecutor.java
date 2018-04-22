package communication;

import service.CustomerSessionService;

import java.net.HttpURLConnection;

public class CreateSessionRequestExecutor implements RequestExecutor {

    private final CustomerSessionService customerSessionService;
    private final int customerId;

    public CreateSessionRequestExecutor(CustomerSessionService customerSessionService, int customerId) {

        this.customerSessionService = customerSessionService;
        this.customerId = customerId;
    }

    @Override
    public Response execute() {

        try {
            String sessionKey = customerSessionService.createSession(customerId);
            return new Response(sessionKey, HttpURLConnection.HTTP_OK);
        } catch(IllegalArgumentException e) {
            return new Response("Customer " + customerId + " already has a session", HttpURLConnection.HTTP_CONFLICT);
        }
    }
}
