package service;

import java.util.Objects;

public class CustomerBetOfferTuple {

    private final int customerId;
    private final int betOfferId;

    public CustomerBetOfferTuple(int customerId, int betOfferId) {
        this.customerId = customerId;
        this.betOfferId = betOfferId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getBetOfferId() {
        return betOfferId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerBetOfferTuple that = (CustomerBetOfferTuple) o;
        return customerId == that.customerId &&
                betOfferId == that.betOfferId;
    }

    @Override
    public int hashCode() {

        return Objects.hash(customerId, betOfferId);
    }
}
