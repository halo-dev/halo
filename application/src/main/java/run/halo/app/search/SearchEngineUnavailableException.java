package run.halo.app.search;

import org.springframework.web.server.ServerWebInputException;

/**
 * Search engine unavailable exception.
 *
 * @author johnniang
 */
public class SearchEngineUnavailableException extends ServerWebInputException {

    public SearchEngineUnavailableException() {
        super("Search Engine is unavailable.");
    }

}
