package service;

import model.Stake;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BetOffersService {

    //For every betting offer, keep the highest stakes. In order to preserve memory, we keep only the highest stakes (e.g. 20).
    private Map<Integer, TreeSet<Stake>> betOfferIdToHighestStakes = new HashMap<>();

    //For every tuple (customer, betOffer) keep the max stake.
    private Map<CustomerBetOfferTuple, Stake> customerBetOfferToMaxStake = new HashMap<>();

    private int maxNoOfStakesPerBetOffer = 20;

    private ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
    private Comparator highestStakeFirstComparator = new HighestStakeFirstComparator();
    private CircuitBreaker circuitBreaker = () -> true; //do nothing circuit breaker

    /**
     * Add a stake posted by the specified customer for the specified betting offer.
     * @throws IllegalStateException if from performance reason, new stakes can't be accepted anymore.
     */
    public void addStake(int customerId, int betOfferId, int stake) {

        _lock.writeLock().lock();
        try {

            if(!circuitBreaker.allowCall()) { //inside the lock because it might use some fields of this very class
                throw new IllegalStateException("Can't accept new stakes");
            }

            boolean isFirstStakeForBetOffer = !betOfferIdToHighestStakes.containsKey(betOfferId);
            if (isFirstStakeForBetOffer) {
                addFistStakeForBettingOffer(customerId, betOfferId, stake);
            } else {
                TreeSet<Stake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);
                if(highestStakes.size() == maxNoOfStakesPerBetOffer && highestStakes.last().getValue() >= stake) {
                    return; //lowest stake from the highest stakes is greater than the received stake, so, we can ignore the new stake.
                }

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

    private void addFistStakeForBettingOffer(int customerId, int betOfferId, int stakeValue) {

        TreeSet<Stake> highestStakes = new TreeSet(highestStakeFirstComparator);
        Stake stake = new Stake(betOfferId, customerId, stakeValue);
        highestStakes.add(stake);
        betOfferIdToHighestStakes.put(betOfferId, highestStakes);
        customerBetOfferToMaxStake.put(new CustomerBetOfferTuple(customerId, betOfferId), stake);
    }

    private void addFirstStakeForCustomer(int customerId, int betOfferId, int stakeValue) {
        TreeSet<Stake> highestStakes = betOfferIdToHighestStakes.get(betOfferId);
        Stake stake = new Stake(betOfferId, customerId, stakeValue);
        highestStakes.add(stake);
        customerBetOfferToMaxStake.put(new CustomerBetOfferTuple(customerId, betOfferId), stake);

        if (highestStakes.size() > maxNoOfStakesPerBetOffer) {
            Stake removedStake = highestStakes.pollLast(); //keep only maxNoOfStakesPerBetOffer values.
            customerBetOfferToMaxStake.remove(new CustomerBetOfferTuple(removedStake.getCustomerId(), betOfferId));
        }
    }

    private void keepHighestStakeForCustomer(CustomerBetOfferTuple customerBetOfferTuple, int stake) {

        Stake maxStake = customerBetOfferToMaxStake.get(customerBetOfferTuple);

        if(maxStake.getValue() < stake) {

            int betOfferId = customerBetOfferTuple.getBetOfferId();
            int customerId = customerBetOfferTuple.getCustomerId();
            Stake newMaxStake = new Stake(betOfferId, customerId, stake);

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

            return highestStakes == null ? Collections.emptyList() : new ArrayList(highestStakes);

        } finally {
            _lock.readLock().unlock();
        }
    }

    public void setMaxNoOfStakesPerBetOffer(int maxNoOfStakesPerBetOffer) {

        this.maxNoOfStakesPerBetOffer = maxNoOfStakesPerBetOffer;
    }

    /*
     * @return The max stake for the specified betting offer and customer. Returns <code>null</code> if no max stake recorded.
     */
    public Integer getMaxStakeValue(int bettingOfferId, int customerId) {

        _lock.readLock().lock();
        try {
            Stake stake = customerBetOfferToMaxStake.get(new CustomerBetOfferTuple(customerId, bettingOfferId));
            return stake != null ? stake.getValue() : null;
        } finally {
            _lock.readLock().unlock();
        }
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {

        this.circuitBreaker = circuitBreaker;
    }

    public void start() {
        //do nothing
    }

    public void stop() {
        //do nothing
    }

    int getStakesNumer() {
        _lock.readLock().lock();
        try {

             return betOfferIdToHighestStakes.values().stream()//
                     .mapToInt(tree -> tree.size()).sum();

        } finally {
            _lock.readLock().unlock();
        }
    }
}
