package communication;

public class Response {

    private final String body;
    private final int httpResponseCode;

    public Response(String body, int httpResponseCode) {

        this.body = body;
        this.httpResponseCode = httpResponseCode;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public String getBody() {

        return body;
    }
}
