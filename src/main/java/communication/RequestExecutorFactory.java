package communication;

import com.sun.net.httpserver.HttpExchange;
import service.BetOffersService;
import service.CustomerSessionService;
import util.StringUtil;

import java.io.*;
import java.net.URI;
import java.nio.Buffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse an URI and create an request executor which will execute the client request.
 */
public class RequestExecutorFactory {

    private Pattern createSessionPattern = Pattern.compile("^/(\\d+)/session$");
    private Pattern postStakePattern = Pattern.compile("^/(\\d+)/stake\\?sessionkey=(\\w+)$");
    private Pattern highestStakesPattern = Pattern.compile("^/(\\d+)/highstakes$");

    private static final Logger LOGGER = Logger.getLogger( RequestExecutorFactory.class.getName() );

    /**
     * @return RequestExecutor or <br>
     * <code>null</code> if the URI can't be parsed or the http request body can't be read.
     */
    public RequestExecutor getRequestExecutor(HttpExchange httpExchange) {

        URI uri = httpExchange.getRequestURI();

        try {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {

                Matcher createSessionMatcher = createSessionPattern.matcher(uri.toString());
                if (createSessionMatcher.matches()) {
                    int customerId = Integer.valueOf(createSessionMatcher.group(1));
                    check31BitUnsignedInt("customerId", customerId);
                    return new CreateSessionRequestExecutor(customerId);
                }

                Matcher highestStakesMatcher = highestStakesPattern.matcher(uri.toString());
                if (highestStakesMatcher.matches()) {
                    int betOfferId = Integer.valueOf(highestStakesMatcher.group(1));
                    check31BitUnsignedInt("betofferId", betOfferId);
                    return new HighestStakesRequestExecutor(betOfferId);
                }
            } else if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {

                Matcher postStakeMatcher = postStakePattern.matcher(uri.toString());
                if (postStakeMatcher.matches()) {

                    int betOfferId = Integer.valueOf(postStakeMatcher.group(1));
                    check31BitUnsignedInt("betofferId", betOfferId);
                    String sessionKey = postStakeMatcher.group(2);
                    int stake = Integer.valueOf(StringUtil.fromInputStreamToString(httpExchange.getRequestBody(), "UTF-8"));
                    check31BitUnsignedInt("stake", stake);
                    return new PostStakeRequestExecutor(betOfferId, sessionKey, stake);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }

        return null;
    }

    private void check31BitUnsignedInt(String parameter, int value) {

        if(value < 0) {
            throw new IllegalArgumentException("Parameter " + parameter + " must be a 31 bit unsigned int");
        }
    }
}


