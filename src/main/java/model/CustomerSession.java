package model;

import util.StringUtil;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * An expiring customer session.
 */
public class CustomerSession implements Delayed {

    private final String sessionKey;
    private final int customerId;
    private final long expirationTimeInMs;

    public CustomerSession(int customerId, long timeToLiveInSec) {

        this.sessionKey = StringUtil.generateRandomAlphaNumericString();
        this.customerId = customerId;
        this.expirationTimeInMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeToLiveInSec);
    }

    /**
     * @return Id of the customer to which this session belongs to.
     */
    public int getCustomerId() {
        return customerId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    @Override
    public long getDelay(TimeUnit unit) {

        return unit.convert(expirationTimeInMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {

        CustomerSession other = (CustomerSession) o;

        if (this.expirationTimeInMs < other.expirationTimeInMs) {
            return -1;
        }
        if (this.expirationTimeInMs > other.expirationTimeInMs) {
            return +1;
        }

        //if two sessions expire in the same time, order them by sessionKey to have a correct implementation for comparareTo.
        return this.sessionKey.compareTo(other.sessionKey);
    }
}
