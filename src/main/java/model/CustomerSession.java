package model;

import service.CustomerSessionService;

import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public class CustomerSession implements Delayed{

    private final String sessionKey;
    private final int customerId;
    private final long expirationTimeInMs;

    private int timeToLiveInMin = 10;

    public CustomerSession(int customerId) {

        this.sessionKey = generateSessionKey();
        this.customerId = customerId;
        this.expirationTimeInMs = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(timeToLiveInMin);
    }

    private String generateSessionKey() {

        //the session key must have only digits and letters. (see spec)
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     *
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

        //if the sessions expire in the same time, order them by session key to have a correct implementation for comparareTo.
        return this.sessionKey.compareTo(other.sessionKey);
    }
}
