package service;

import model.CustomerSession;

import java.util.*;
import java.util.concurrent.*;

public class CustomerSessionService {

    private Map<String, CustomerSession> sessionKeyToSession = new HashMap<>();
    private Set<Integer> customersWithSession = new HashSet<>();
    private SessionExpirationService sessionExpirationService = new SessionExpirationService();

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
    public void createSession(int customerId) {

        if (customersWithSession.contains(customerId)) {
            throw new IllegalArgumentException("Customer " + customerId + " already has an associated session");
        }

        CustomerSession session = new CustomerSession(customerId);

        synchronized (this) {
            sessionKeyToSession.put(session.getSessionKey(), session);
            customersWithSession.add(customerId);
        }

        sessionExpirationService.addNewSession(session);
    }

    /**
     * @return Id of the customer associated with the session key.
     * Returns <code>Optional.empty</code> if session key does not exist.
     */
    public Optional<Integer> getCustomerId(String sessionKey) {

        synchronized (this) {
            if (sessionKeyToSession.containsKey(sessionKey)) {
                return Optional.of(sessionKeyToSession.get(sessionKey).getCustomerId());
            } else {
                return Optional.empty();
            }
        }
    }

    private void handleExpiredSession(CustomerSession expiredSession) {

        synchronized (this) {
            CustomerSession customerSession = sessionKeyToSession.remove(expiredSession.getSessionKey());
            customersWithSession.remove(customerSession.getCustomerId());
        }
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
