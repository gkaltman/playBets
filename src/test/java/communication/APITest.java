package communication;


import application.AppStarter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.StringUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

public class APITest {

    private AppStarter appStarter;

    @Before
    public void setUp() throws IOException {

        System.out.println("Starting ");
        appStarter = new AppStarter();
        appStarter.start();
        System.out.println("Started ");
    }

    @After
    public void tearDown() throws IOException
    {

        System.out.println("Tear down ");
        appStarter.stop();
        System.out.println("Down ");
    }


    @Test
    public void testSessionCreation() throws IOException {

        HttpUriRequest request = new HttpGet( "http://localhost:8001/1234/session" );

        BasicResponseHandler handler = new BasicResponseHandler();
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        String sessionKey = handler.handleResponse(httpResponse);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResponse.getStatusLine().getStatusCode());
        Assert.assertNotNull(sessionKey);
    }

    /**
     * Scenario: Try to create 2 sessions for the same customer.
     * Expect: HTPP Conflict.
     * @throws IOException
     */
    @Test
    public void test2Sessions() throws IOException {

        HttpUriRequest request = new HttpGet( "http://localhost:8001/1234/session" );

        HttpResponse httpResponse1 = HttpClientBuilder.create().build().execute(request);
        HttpResponse httpResponse2 = HttpClientBuilder.create().build().execute(request);

        String body = StringUtil.fromInputStreamToString(httpResponse2.getEntity().getContent(), "UTF-8");
        Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, httpResponse2.getStatusLine().getStatusCode());
        Assert.assertEquals("Customer 1234 already has a session", body);
    }
}
