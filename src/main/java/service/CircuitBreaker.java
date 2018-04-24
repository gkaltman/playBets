package service;

/**
 * Allows the application to cut various processes in order to keep the application alive.
 */
public interface CircuitBreaker {

    /**
     * @return True if the circuit should stay close. False otherwise.
     */
    boolean allowCall();
}
