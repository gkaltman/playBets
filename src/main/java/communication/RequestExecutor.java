package communication;

import service.AllServices;

public interface RequestExecutor {

    /**
     * Execute a request and return a response.
     * Inject all services, the subclasses will whatever service they want.
     */
    Response execute(AllServices allServices);
}
