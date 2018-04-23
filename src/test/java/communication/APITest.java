package communication;


import application.AppStarter;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

public class APITest {

    @Test
    public void testSessionKey() throws IOException {

        AppStarter.start();

        HttpUriRequest request = new HttpGet( "http://localhost:8001/1234/session" );

        BasicResponseHandler handler = new BasicResponseHandler();
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        String sessionKey = handler.handleResponse(httpResponse);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, httpResponse.getStatusLine().getStatusCode());
        Assert.assertNotNull(sessionKey);
    }
}
