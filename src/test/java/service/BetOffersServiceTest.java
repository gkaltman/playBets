package service;

import model.Stake;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        //assert
        Assert.assertEquals(3, highestStakes.size());
        Assert.assertEquals(customerId1, highestStakes.get(0).getCustomerId());
        Assert.assertEquals(475, highestStakes.get(0).getValue());

        Assert.assertEquals(customerId3, highestStakes.get(1).getCustomerId());
        Assert.assertEquals(430, highestStakes.get(1).getValue());

        Assert.assertEquals(customerId4, highestStakes.get(2).getCustomerId());
        Assert.assertEquals(417, highestStakes.get(2).getValue());

        Assert.assertEquals(475, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId1));
        Assert.assertNull(betOffersService.getMaxStakeValue(bettingOfferId, customerId2));
        Assert.assertEquals(430, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId3));
        Assert.assertEquals(417, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId4));
    }

    @Test
    public void getHighestStakeForRandomBetOffer() {

        List<Stake> highestStakes = betOffersService.getHighestStakes(1231);
        Assert.assertTrue(highestStakes.isEmpty());
    }

    @Test
    public void getStakesNumber() {

        int customerId1 = 1;
        int customerId2 = 2;
        int bettingOfferId1 = 888;
        int bettingOfferId2 = 889;

        betOffersService.addStake(customerId1, bettingOfferId1, 450);
        betOffersService.addStake(customerId2, bettingOfferId1, 415);
        betOffersService.addStake(customerId1, bettingOfferId2, 475);

        Assert.assertEquals(3, betOffersService.getStakesNumer());
    }
    /**
     * Scenario: for a given betOffer, we have the max number of stakes.
     * A new stake is posted. The new stake is lower than the lowest highest.
     * Expect: the stake is not added.
     */
    @Test
    public void testLowStake() {

        //set up
        int customerId1 = 1;
        int customerId2 = 2;
        int customerId3 = 3;
        int customerId4 = 4;
        int bettingOfferId = 888;

        //command
        betOffersService.addStake(customerId1, bettingOfferId, 450);
        betOffersService.addStake(customerId2, bettingOfferId, 415);
        betOffersService.addStake(customerId3, bettingOfferId, 430);
        betOffersService.addStake(customerId4, bettingOfferId, 410);

        //command
        List<Stake> highestStakes = betOffersService.getHighestStakes(bettingOfferId);

        //assert
        Assert.assertEquals(3, highestStakes.size());
        Assert.assertEquals(customerId1, highestStakes.get(0).getCustomerId());
        Assert.assertEquals(450, highestStakes.get(0).getValue());

        Assert.assertEquals(customerId3, highestStakes.get(1).getCustomerId());
        Assert.assertEquals(430, highestStakes.get(1).getValue());

        Assert.assertEquals(customerId2, highestStakes.get(2).getCustomerId());
        Assert.assertEquals(415, highestStakes.get(2).getValue());

        Assert.assertEquals(450, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId1));
        Assert.assertEquals(430, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId3));
        Assert.assertEquals(415, (int) betOffersService.getMaxStakeValue(bettingOfferId, customerId2));
        Assert.assertNull(betOffersService.getMaxStakeValue(bettingOfferId, customerId4));

    }

    @Test(expected = IllegalStateException.class)
    public void testAlwaysOpenCircuitBreaker() {

        betOffersService.setCircuitBreaker(() -> false);
        betOffersService.addStake(1,1,1);
    }

}
