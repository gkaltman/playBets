package communication;

import service.AllServices;

import java.net.HttpURLConnection;

public class CreateSessionRequestExecutor implements RequestExecutor {

    private final int customerId;

    CreateSessionRequestExecutor(int customerId) {

        this.customerId = customerId;
    }

    @Override
    public Response execute(AllServices allServices) {

        try {
            String sessionKey = allServices.getCustomerSessionService().createSession(customerId);
            return new Response(sessionKey, HttpURLConnection.HTTP_OK);
        } catch(IllegalArgumentException e) {
            return new Response("Customer " + customerId + " already has a session", HttpURLConnection.HTTP_CONFLICT);
        }
    }
}
