package service;

/**
 * Allows the application to drop/block various calls in order to keep the application alive.
 */
public interface CircuitBreaker {

    /**
     * @return True if the circuit should stay close. False otherwise.
     */
    boolean allowCall();
}
