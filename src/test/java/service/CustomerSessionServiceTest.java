package service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CustomerSessionServiceTest {

    private CustomerSessionService customerSessionService;

    @Before
    public void setUp() {
        customerSessionService = new CustomerSessionService();
    }

    @Test
    public void smokeTest() {

        int customerId = 15;
        String sessionKey = customerSessionService.createSession(customerId);

        Assert.assertNotNull(sessionKey);
    }

    @Test (expected = IllegalArgumentException.class)
    public void tryToCreate2Sessions() {

        int customerId = 15;
        customerSessionService.createSession(customerId);
        customerSessionService.createSession(customerId);
    }

    /**
     * Scenario: set session TTL to 2 seconds and create a session.
     * Expect: the session is expired.
     *
     */
    @Test
    public void testSessionExpiration() throws InterruptedException {

        //set up
        customerSessionService.setSessionTimeToLiveInSec(2);
        customerSessionService.start();

        int customerId = 123;

        //command
        customerSessionService.createSession(customerId);

        //assert
        CountDownLatch countDownLatch = new CountDownLatch(1);
        customerSessionService.addListener((expiredCustomerId, sessionKey) -> {
            Assert.assertEquals(customerId, expiredCustomerId);
            Assert.assertNull(customerSessionService.getCustomerId(sessionKey));
            countDownLatch.countDown();
        });

        boolean awaitSuccesful = countDownLatch.await(4, TimeUnit.SECONDS);
        Assert.assertTrue(awaitSuccesful);

        customerSessionService.stop();
    }

    /**
     * Scenario: set session TTL to 2 seconds and create 2 sessions.
     * Expect: the sessions expired in the creation order.
     *
     */
    @Test
    public void testSessionsExpiredInCorrectOrder() throws InterruptedException {

        //set up
        customerSessionService.setSessionTimeToLiveInSec(2);
        customerSessionService.start();

        int customerId1 = 123;
        int customerId2 = 124;

        //command
        customerSessionService.createSession(customerId1);
        Thread.sleep(1000);
        customerSessionService.createSession(customerId2);

        List customersWithExpiredSession = new ArrayList();
        //assert
        CountDownLatch countDownLatch = new CountDownLatch(2);
        customerSessionService.addListener((expiredCustomerId, sessionKey) -> {

            customersWithExpiredSession.add(expiredCustomerId);
            countDownLatch.countDown();
        });

        boolean awaitSuccesful = countDownLatch.await(4, TimeUnit.SECONDS);
        Assert.assertTrue(awaitSuccesful);
        Assert.assertEquals(2, customersWithExpiredSession.size());
        Assert.assertEquals(customerId1, customersWithExpiredSession.get(0));
        Assert.assertEquals(customerId2, customersWithExpiredSession.get(1));

        customerSessionService.stop();
    }
}
