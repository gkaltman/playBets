package service;

import model.Stake;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import service.BetOffersService;

import java.util.List;

public class BetOffersServiceTest {

    private BetOffersService betOffersService;

    @Before
    public void setUp() {
        betOffersService = new BetOffersService();
        betOffersService.setMaxNoOfStakesPerBetOffer(3);
    }

    @Test
    public void smokeTest() {

        //set up
        int customerId1 = 1;
        int customerId2 = 2;
        int customerId3 = 3;
        int customerId4 = 4;
        int bettingOfferId = 888;

        betOffersService.addStake(customerId1, bettingOfferId, 450);
        betOffersService.addStake(customerId2, bettingOfferId, 415);
        betOffersService.addStake(customerId1, bettingOfferId, 475);
        betOffersService.addStake(customerId3, bettingOfferId, 430);
        betOffersService.addStake(customerId4, bettingOfferId, 417);

        //command
        List<Stake> highestStakes = betOffersService.getHighestStakes(bettingOfferId);

        System.out.println(highestStakes);
        //assert
        Assert.assertEquals(3, highestStakes.size());
        Assert.assertEquals(customerId1, highestStakes.get(0).getCustomerId());
        Assert.assertEquals(475, highestStakes.get(0).getStake());

        Assert.assertEquals(customerId3, highestStakes.get(1).getCustomerId());
        Assert.assertEquals(430, highestStakes.get(1).getStake());

        Assert.assertEquals(customerId4, highestStakes.get(2).getCustomerId());
        Assert.assertEquals(417, highestStakes.get(2).getStake());

        Assert.assertEquals(475, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId1));
        Assert.assertNull(betOffersService.getMaxStakeValue(bettingOfferId, customerId2));
        Assert.assertEquals(430, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId3));
        Assert.assertEquals(417, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId4));
    }
}
