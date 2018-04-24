package service;

/**
 * Dummy circuit breaker. Breakes circuit when the total number of stakes is greater than a hardcoded value.
 */
public class MaxNumberOfStakesCircuitBreaker implements CircuitBreaker {

    private final BetOffersService betOffersService;

    public MaxNumberOfStakesCircuitBreaker(BetOffersService betOffersService) {
        this.betOffersService = betOffersService;
    }

    @Override
    public boolean allowCall() {

        return betOffersService.getStakesNumer() < 1000000 ? true : false;
    }
}
