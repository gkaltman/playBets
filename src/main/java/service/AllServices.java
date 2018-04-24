package service;

/**
 * Container for all the services of the application.
 */
public class AllServices {

    private BetOffersService betOffersService;
    private CustomerSessionService customerSessionService;

    public BetOffersService getBetOffersService() {
        return betOffersService;
    }

    public void setBetOffersService(BetOffersService betOffersService) {
        this.betOffersService = betOffersService;
    }

    public CustomerSessionService getCustomerSessionService() {
        return customerSessionService;
    }

    public void setCustomerSessionService(CustomerSessionService customerSessionService) {
        this.customerSessionService = customerSessionService;
    }
}
