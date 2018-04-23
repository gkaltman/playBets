package communication;


import application.AppStarter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.StringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class APITest {

    private AppStarter appStarter;

    @Before
    public void setUp() throws IOException {

        appStarter = new AppStarter();
        appStarter.start();
    }

    @After
    public void tearDown() throws IOException
    {
        appStarter.stop();
    }


    @Test
    public void testSessionCreation() throws IOException {

        Response response = sendCreateSessionRequest(1234);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getHttpResponseCode());
        Assert.assertNotNull(response.getBody());
    }

    /**
     * Scenario: Try to create 2 sessions for the same customer.
     * Expect: HTPP Conflict.
     * @throws IOException
     */
    @Test
    public void test2Sessions() throws IOException {

        Response response1 = sendCreateSessionRequest(1234);
        Response response2 = sendCreateSessionRequest(1234);


        Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, response2.getHttpResponseCode());
        Assert.assertEquals("Customer 1234 already has a session", response2.getBody());
    }

    @Test
    public void testPostStakeWithoutValidSession() throws IOException {
        int betOfferId = 888;
        Response response = sendPostStake("fakeSession", betOfferId, "450");
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getHttpResponseCode());
    }

    @Test
    public void testPostStakes() throws IOException {

        int customerId1 = 123;
        int betOfferId = 888;
        Response reponse1 = sendCreateSessionRequest(customerId1);
        String sessionKey1 = reponse1.getBody();
        sendPostStake(sessionKey1, betOfferId, "450");

        int customerId2 = 124;
        Response reponse2 = sendCreateSessionRequest(customerId2);
        String sessionKey2 = reponse2.getBody();
        sendPostStake(sessionKey2, betOfferId, "430");

        sendPostStake(sessionKey1, betOfferId, "470");

        Response highestStakesResponse = sendGetHighestRequest(betOfferId);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, highestStakesResponse.getHttpResponseCode());

        List<String> highestStakeResponseAsList = decomposeGetHighestRequestResponse(highestStakesResponse.getBody());
        Assert.assertEquals(String.valueOf(customerId1), highestStakeResponseAsList.get(0));
        Assert.assertEquals("470", highestStakeResponseAsList.get(1));
        Assert.assertEquals(String.valueOf(customerId2), highestStakeResponseAsList.get(2));
        Assert.assertEquals("430", highestStakeResponseAsList.get(3));
    }

    private Response sendCreateSessionRequest(int customerId) throws IOException {

        HttpUriRequest request = new HttpGet( "http://localhost:8001/" +customerId + "/session" );
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        String sessionKey = StringUtil.fromInputStreamToString(httpResponse.getEntity().getContent(), "UTF-8");
        int httpStatusCode = httpResponse.getStatusLine().getStatusCode();

        return new Response(sessionKey, httpStatusCode);
    }

    private Response sendGetHighestRequest(int betOfferId) throws IOException {

        HttpUriRequest request = new HttpGet( "http://localhost:8001/"+ betOfferId+"/highstakes" );
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        String sessionKey = StringUtil.fromInputStreamToString(httpResponse.getEntity().getContent(), "UTF-8");
        int httpStatusCode = httpResponse.getStatusLine().getStatusCode();

        return new Response(sessionKey, httpStatusCode);
    }

    private Response sendPostStake(String sessionKey, int betOfferId, String stake) throws IOException {

        HttpPost postStakeRequest = new HttpPost("http://localhost:8001/" + betOfferId + "/stake?sessionkey=" + sessionKey);
        HttpEntity entity = new ByteArrayEntity(stake.getBytes("UTF-8"));
        postStakeRequest.setEntity(entity);
        HttpResponse postStakeResponse = HttpClientBuilder.create().build().execute(postStakeRequest);
        String body = StringUtil.fromInputStreamToString(postStakeResponse.getEntity().getContent(), "UTF-8");
        return new Response(body, postStakeResponse.getStatusLine().getStatusCode());
    }

    /**
     * Turn string "1=2,3=4,5=6" into the list (1,2,3,4,5,6)
     *      */
    private List<String> decomposeGetHighestRequestResponse(String body) {

        return Arrays.asList(body.split(",")).stream()//
                .flatMap(s -> Arrays.asList(s.split("=")).stream()) //
                .collect(Collectors.toList());
    }

}
