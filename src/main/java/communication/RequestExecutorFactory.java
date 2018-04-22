package communication;

import com.sun.net.httpserver.HttpExchange;
import service.BetOffersService;
import service.CustomerSessionService;
import util.StringUtil;

import java.io.*;
import java.net.URI;
import java.nio.Buffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse an URI and decide which executor should execute the client request.
 */
public class RequestExecutorFactory {

    private Pattern createSessionPattern = Pattern.compile("^/(\\d+)/session$");
    private Pattern postStakePattern = Pattern.compile("^/(\\d+)/stake\\?sessionkey=(\\w+)$");
    private Pattern highestStakesPattern = Pattern.compile("^/(\\d+)/highstakes$");



    private CustomerSessionService customerSessionService;
    private BetOffersService betOffersService;

    /**
     * @return RequestExecutor or <code>null</code> if no executor was found for the specified URI.
     */
    public RequestExecutor getRequestHandler(HttpExchange httpExchange) throws IOException {

        URI uri = httpExchange.getRequestURI();

        Matcher createSessionMatcher = createSessionPattern.matcher(uri.toString());
        if (createSessionMatcher.matches()) {
            return new CreateSessionRequestExecutor(customerSessionService, Integer.valueOf(createSessionMatcher.group(1)));
        }

        Matcher postStakeMatcher = postStakePattern.matcher(uri.toString());
        if (postStakeMatcher.matches()) {
            int betOfferId = Integer.valueOf(postStakeMatcher.group(1));
            String sessionKey = postStakeMatcher.group(2);
            int stake = Integer.valueOf(StringUtil.fromInputStreamToString(httpExchange.getRequestBody(), "UTF-8"));
            return new PostStakeRequestExecutor(betOffersService, customerSessionService, betOfferId, sessionKey, stake);
        }

        Matcher highestStakesMatcher = highestStakesPattern.matcher(uri.toString());
        if(highestStakesMatcher.matches()) {
            int betOfferId = Integer.valueOf(highestStakesMatcher.group(1));
            return new HighestStakesRequestExecutor(betOffersService, betOfferId);
        }

        return null;
    }

    public static void main(String[] args) {

        System.out.println(Pattern.compile("^/(\\d+)/highstakes$").matcher("/888/highstakes").matches());
    }

    public void setCustomerSessionService(CustomerSessionService customerSessionService) {

        this.customerSessionService = customerSessionService;
    }

    public void setBetOffersService(BetOffersService betOffersService) {

        this.betOffersService = betOffersService;
    }
}

