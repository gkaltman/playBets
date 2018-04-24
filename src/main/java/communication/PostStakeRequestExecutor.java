package communication;

import service.AllServices;

import java.net.HttpURLConnection;

public class PostStakeRequestExecutor implements RequestExecutor {

    private final int betOfferId;
    private final String sessionKey;

    private final int stake;

    public PostStakeRequestExecutor(int betOfferId, String sessionKey, int stake) {

        this.betOfferId = betOfferId;
        this.sessionKey = sessionKey;
        this.stake = stake;
    }

    @Override
    public Response execute(AllServices allServices) {

        Integer customerId = allServices.getCustomerSessionService().getCustomerId(sessionKey);
        if(customerId == null) {
            return new Response("Session is not associated with any customer", HttpURLConnection.HTTP_NOT_FOUND);
        }
        try {
            allServices.getBetOffersService().addStake(customerId, betOfferId, stake);
            return new Response("", HttpURLConnection.HTTP_OK);
        }catch (IllegalStateException e) {
            return new Response("Can't take any more stakes", HttpURLConnection.HTTP_UNAVAILABLE);
        }
    }
}
