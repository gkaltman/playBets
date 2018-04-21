package service;

import model.BetOfferStake;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BetOffersService {

    //For every betting offer, keep the highest stakes
    private Map<Integer, TreeSet<BetOfferStake>> betOfferIdToHighestStakes = new HashMap<>();

    //For every tuple (customer, bettoffer) keep the max stake.
    private Map<CustomerBetOfferKey, BetOfferStake> customerBetOfferToMaxStake = new HashMap<>();

    private int maxNoOfStakesPerBetOffer = 20;

    private ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
    private Comparator highestStakeFirstComparator = new HighestStakeFirstComparator();

    /**
     * Take into account a stake posted by the specified customer for the specified betting offer.
     */
    public void addStake(int customerId, int betOfferId, int stake) {

        _lock.writeLock().lock();
        try {
            boolean isFirstStakeEverForBetOffer = !betOfferIdToHighestStakes.containsKey(betOfferId);
            if (isFirstStakeEverForBetOffer) {
                addFistStakeEverForBettingOffer(customerId, betOfferId, stake);
            } else {


                CustomerBetOfferKey customerBetOfferKey = new CustomerBetOfferKey(customerId, betOfferId);

                boolean hasCustomerAlreadyPostedStake = customerBetOfferToMaxStake.containsKey(customerBetOfferKey);
                if(hasCustomerAlreadyPostedStake) {
                    keepHighestStakeForCustomer(customerBetOfferKey, stake);
                } else {
                    addStakeForBettingOffer(customerId, betOfferId, stake);
                }
            }
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private void addFistStakeEverForBettingOffer(int customerId, int betOfferId, int stake) {

        TreeSet<BetOfferStake> highestStakes = new TreeSet(highestStakeFirstComparator);
        highestStakes.add(new BetOfferStake(betOfferId, customerId, stake));
        betOfferIdToHighestStakes.put(betOfferId, highestStakes);
    }

    private void addStakeForBettingOffer(int customerId, int betOfferId, int stake) {
        TreeSet<BetOfferStake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);
        highestStakes.add(new BetOfferStake(betOfferId, customerId, stake));
        if (highestStakes.size() > maxNoOfStakesPerBetOffer) {
            highestStakes.pollLast(); //keep only maxNoOfStakesPerBetOffer values.
        }
    }

    private void keepHighestStakeForCustomer(CustomerBetOfferKey customerBetOfferKey, int stake) {

        BetOfferStake maxStake = customerBetOfferToMaxStake.get(customerBetOfferKey);

        if(maxStake.getStake() < stake) {

            int betOfferId = customerBetOfferKey.getBetOfferId();
            int customerId = customerBetOfferKey.getCustomerId();
            BetOfferStake newMaxStake = new BetOfferStake(customerId, betOfferId, stake);

            TreeSet<BetOfferStake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);
            highestStakes.remove(maxStake);
            highestStakes.add(newMaxStake);

            customerBetOfferToMaxStake.put(customerBetOfferKey, newMaxStake);
        }
    }

    /**
     * For the specified bettingOffer, get the highest stakes.
     *
     * @return List with the highest stakes. The highest stake is the first in the list.<br>
     * If there are no stakes, return an empty list.
     */
    public List<BetOfferStake> getHighestBetOffers(int betOfferId) {

        _lock.readLock().lock();
        try {
            TreeSet<BetOfferStake> highestValues = betOfferIdToHighestStakes.get(betOfferId);

            return highestValues.isEmpty() ? Collections.emptyList() : new ArrayList(highestValues);

        } finally {
            _lock.readLock().unlock();
        }
    }

    private static class HighestStakeFirstComparator implements Comparator<BetOfferStake> {

        @Override
        public int compare(BetOfferStake betOfferStake1, BetOfferStake betoffer2) {

            if (betOfferStake1.getStake() < betoffer2.getStake()) {
                return 1;
            }
            if (betOfferStake1.getStake() > betoffer2.getStake()) {
                return -1;
            } else {
                //if stakes are equal, order by consumer id.
                return Integer.compare(betOfferStake1.getCustomerId(), betoffer2.getCustomerId());
            }

        }
    }

    private static class CustomerBetOfferKey {

        private final int customerId;
        private final int betOfferId;

        public CustomerBetOfferKey(int customerId, int betOfferId) {
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
            CustomerBetOfferKey that = (CustomerBetOfferKey) o;
            return customerId == that.customerId &&
                    betOfferId == that.betOfferId;
        }

        @Override
        public int hashCode() {

            return Objects.hash(customerId, betOfferId);
        }
    }
}
