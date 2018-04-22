package model;

/**
 * Represents a stake give by a customer for a given bet offer.
 */
public class Stake {

    private final int betOfferId;
    private final int customerId;
    private int stake;

    public Stake(int betOfferId, int customerId, int stake) {
        this.betOfferId = betOfferId;
        this.customerId = customerId;
        this.stake = stake;
    }

    public int getBetOfferId() {
        return betOfferId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getStake() {
        return stake;
    }

    @Override
    public String toString() {
        return "Stake{" +
                "betOfferId=" + betOfferId +
                ", customerId=" + customerId +
                ", stake=" + stake +
                '}';
    }
}
