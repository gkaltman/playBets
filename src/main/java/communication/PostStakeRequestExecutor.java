package communication;

import model.CustomerSession;
import service.BetOffersService;
import service.CustomerSessionService;

import java.net.HttpURLConnection;

public class PostStakeRequestExecutor implements RequestExecutor {

    private final BetOffersService betOffersService;
    private CustomerSessionService customerSessionService;
    private final int betOfferId;
    private final String sessionKey;

    private final int stake;

    public PostStakeRequestExecutor(BetOffersService betOffersService, CustomerSessionService customerSessionService, int betOfferId, String sessionKey, int stake) {
        this.betOffersService = betOffersService;
        this.customerSessionService = customerSessionService;
        this.betOfferId = betOfferId;
        this.sessionKey = sessionKey;
        this.stake = stake;
    }

    @Override
    public Response execute() {

        Integer customerId = customerSessionService.getCustomerId(sessionKey);
        if(customerId == null) {
            return new Response("Session is not associated with any customer", HttpURLConnection.HTTP_NOT_FOUND);
        }
        betOffersService.addStake(customerId, betOfferId, stake);
        return new Response("", HttpURLConnection.HTTP_OK);
    }
}
