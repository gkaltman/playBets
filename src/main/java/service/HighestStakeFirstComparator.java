package service;

import model.Stake;

import java.util.Comparator;

/**
 * Comparator for sorting the stake values, highest values first.
 */
class HighestStakeFirstComparator implements Comparator<Stake> {

    @Override
    public int compare(Stake stake1, Stake stake2) {

        if (stake1.getValue() < stake2.getValue()) {
            return 1;
        }
        if (stake1.getValue() > stake2.getValue()) {
            return -1;
        } else {
            //if stakes are equal, order by customer id.
            return Integer.compare(stake1.getCustomerId(), stake2.getCustomerId());
        }

    }
}
