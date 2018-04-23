package service;

public interface ExpiredSessionListener {

    void notifyExpiredSession(int customerId, String sessionKey);
}
