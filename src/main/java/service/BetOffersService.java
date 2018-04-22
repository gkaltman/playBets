package service;

import model.Stake;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BetOffersService {

    //For every betting offer, keep the highest stakes
    private Map<Integer, TreeSet<Stake>> betOfferIdToHighestStakes = new HashMap<>();

    //For every tuple (customer, betoffer) keep the max stake.
    private Map<CustomerBetOfferTuple, Stake> customerBetOfferToMaxStake = new HashMap<>();

    private int maxNoOfStakesPerBetOffer = 20;

    private ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
    private Comparator highestStakeFirstComparator = new HighestStakeFirstComparator();

    /**
     * Add a stake posted by the specified customer for the specified betting offer.
     */
    public void addStake(int customerId, int betOfferId, int stake) {

        _lock.writeLock().lock();
        try {
            boolean isFirstStakeEverForBetOffer = !betOfferIdToHighestStakes.containsKey(betOfferId);
            if (isFirstStakeEverForBetOffer) {
                addFistStakeEverForBettingOffer(customerId, betOfferId, stake);
            } else {

                CustomerBetOfferTuple customerBetOfferTuple = new CustomerBetOfferTuple(customerId, betOfferId);
                boolean hasCustomerAlreadyPostedAStake = customerBetOfferToMaxStake.containsKey(customerBetOfferTuple);
                if(!hasCustomerAlreadyPostedAStake) {
                    addFirstStakeForCustomer(customerId, betOfferId, stake);
                } else {
                    keepHighestStakeForCustomer(customerBetOfferTuple, stake);
                }
            }
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private void addFistStakeEverForBettingOffer(int customerId, int betOfferId, int stake) {

        TreeSet<Stake> highestStakes = new TreeSet(highestStakeFirstComparator);
        highestStakes.add(new Stake(betOfferId, customerId, stake));
        betOfferIdToHighestStakes.put(betOfferId, highestStakes);
    }

    private void addFirstStakeForCustomer(int customerId, int betOfferId, int stake) {
        TreeSet<Stake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);
        highestStakes.add(new Stake(betOfferId, customerId, stake));
        if (highestStakes.size() > maxNoOfStakesPerBetOffer) {
            highestStakes.pollLast(); //keep only maxNoOfStakesPerBetOffer values.
        }
    }

    private void keepHighestStakeForCustomer(CustomerBetOfferTuple customerBetOfferTuple, int stake) {

        Stake maxStake = customerBetOfferToMaxStake.get(customerBetOfferTuple);

        if(maxStake.getStake() < stake) {

            int betOfferId = customerBetOfferTuple.getBetOfferId();
            int customerId = customerBetOfferTuple.getCustomerId();
            Stake newMaxStake = new Stake(customerId, betOfferId, stake);

            TreeSet<Stake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);
            highestStakes.remove(maxStake);
            highestStakes.add(newMaxStake);

            customerBetOfferToMaxStake.put(customerBetOfferTuple, newMaxStake);
        }
    }

    /**
     * For the specified bettingOffer, get the highest stakes.
     *
     * @return List with the highest stakes. The highest stake is the first in the list.<br>
     * If there are no stakes, return an empty list.
     */
    public List<Stake> getHighestStakes(int betOfferId) {

        _lock.readLock().lock();
        try {
            TreeSet<Stake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);

            return highestStakes.isEmpty() ? Collections.emptyList() : new ArrayList(highestStakes);

        } finally {
            _lock.readLock().unlock();
        }
    }

    public void start() {
        //do nothing
    }

    public void stop() {
        //do nothing
    }

    private static class HighestStakeFirstComparator implements Comparator<Stake> {

        @Override
        public int compare(Stake stake1, Stake betoffer2) {

            if (stake1.getStake() < betoffer2.getStake()) {
                return 1;
            }
            if (stake1.getStake() > betoffer2.getStake()) {
                return -1;
            } else {
                //if stakes are equal, order by consumer id.
                return Integer.compare(stake1.getCustomerId(), betoffer2.getCustomerId());
            }

        }
    }

    private static class CustomerBetOfferTuple {

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
}
