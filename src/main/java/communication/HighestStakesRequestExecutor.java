package communication;

import model.Stake;
import service.AllServices;
import service.BetOffersService;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

public class HighestStakesRequestExecutor implements RequestExecutor {

    private final int betOfferId;

    public HighestStakesRequestExecutor(int betOfferId) {
        this.betOfferId = betOfferId;
    }

    @Override
    public Response execute(AllServices allServices) {

        List<Stake> highestStakes = allServices.getBetOffersService().getHighestStakes(betOfferId);

        String body = highestStakes.stream()//
                .map(stake -> stake.getCustomerId() + "=" + stake.getValue())//
                .collect(Collectors.joining(","));
        return new Response(body, HttpURLConnection.HTTP_OK);
    }
}
