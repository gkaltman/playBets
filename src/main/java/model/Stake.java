package model;

/**
 * Represents the stake posted by a customer for a given bet offer.
 */
public class Stake {

    private final int betOfferId;
    private final int customerId;
    private int value;

    public Stake(int betOfferId, int customerId, int value) {
        this.betOfferId = betOfferId;
        this.customerId = customerId;
        this.value = value;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Stake{" +
                "betOfferId=" + betOfferId +
                ", customerId=" + customerId +
                ", value=" + value +
                '}';
    }
}
