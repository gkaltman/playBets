package communication;

import model.Stake;
import service.BetOffersService;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

public class HighestStakesRequestExecutor implements RequestExecutor {

    private final BetOffersService betOffersService;
    private final int betOfferId;

    public HighestStakesRequestExecutor(BetOffersService betOffersService, int betOfferId) {
        this.betOffersService = betOffersService;
        this.betOfferId = betOfferId;
    }

    @Override
    public Response execute() {

        List<Stake> highestStakes = betOffersService.getHighestStakes(betOfferId);

        String body = highestStakes.stream()//
                .map(stake -> stake.getCustomerId() + "=" + stake.getStake())//
                .collect(Collectors.joining(","));
        return new Response(body, HttpURLConnection.HTTP_OK);
    }
}
