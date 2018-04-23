package service;

import communication.RootHttpHandler;
import model.CustomerSession;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerSessionService {

    private static final Logger LOGGER = Logger.getLogger(CustomerSessionService.class.getName());
    private Map<String, CustomerSession> sessionKeyToSession = new HashMap<>();
    private Set<Integer> customersWithSession = new HashSet<>();

    private SessionExpirationService sessionExpirationService = new SessionExpirationService();
    private long sessionTimeToLiveInSec = TimeUnit.MINUTES.toSeconds(10);

    private List<ExpiredSessionListener> expiredSessionListener = new CopyOnWriteArrayList<>();


    public void start() {
        sessionExpirationService.start();
    }

    public void stop() {
        sessionExpirationService.stop();
    }

    /**
     * Create a session for the customer with the specified id.
     *
     * @return The session key.
     * @throws IllegalArgumentException if the specified customer has already a session.
     */
    public String createSession(int customerId) {

        synchronized (this) {

            if (customersWithSession.contains(customerId)) {
                throw new IllegalArgumentException("Customer " + customerId + " already has an associated session");
            }

            CustomerSession session = new CustomerSession(customerId, sessionTimeToLiveInSec);

            sessionKeyToSession.put(session.getSessionKey(), session);
            customersWithSession.add(customerId);
            sessionExpirationService.addNewSession(session);

            return session.getSessionKey();
        }
    }

    /**
     * @return Id of the customer associated with the session key.
     * Returns <code>null</code> if sessionKey does not exist.
     */
    public Integer getCustomerId(String sessionKey) {

        synchronized (this) {
            return sessionKeyToSession.containsKey(sessionKey) ?//
                    sessionKeyToSession.get(sessionKey).getCustomerId() : null;
        }
    }

    private void handleExpiredSession(CustomerSession expiredSession) {

        synchronized (this) {
            CustomerSession customerSession = sessionKeyToSession.remove(expiredSession.getSessionKey());
            customersWithSession.remove(customerSession.getCustomerId());
        }

        expiredSessionListener.stream()//
                .forEach(listener -> listener.notifyExpiredSession(expiredSession.getCustomerId(), expiredSession.getSessionKey()));
    }

    public void addListener(ExpiredSessionListener listener) {

        expiredSessionListener.add(listener);
    }

    public void setSessionTimeToLiveInSec(long timeToLiveInSec) {

        sessionTimeToLiveInSec = timeToLiveInSec;
    }


    private class SessionExpirationService {

        private ExecutorService executorService = Executors.newSingleThreadExecutor();
        private DelayQueue<CustomerSession> expiringSessionQueue = new DelayQueue<>();

        void start() {

            executorService.submit(() -> {
                while (true) {
                    try {
                        CustomerSession expiredSession = expiringSessionQueue.take();
                        handleExpiredSession(expiredSession);
                    } catch (InterruptedException ignore) {
                        break;
                    } catch (Throwable t) {
                        //we choose just to log the exception and keep running.
                        LOGGER.log(Level.SEVERE, t.getMessage(), t);
                    }
                }
            });
        }

        void stop() {
            executorService.shutdownNow();
        }

        void addNewSession(CustomerSession session) {
            expiringSessionQueue.add(session);
        }
    }
}
