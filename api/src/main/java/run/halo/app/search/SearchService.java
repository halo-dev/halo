package run.halo.app.search;

import reactor.core.publisher.Mono;

/**
 * Search service is used to search content.
 *
 * @author johnniang
 * @since 2.17.0
 */
public interface SearchService {

    /**
     * Perform search.
     *
     * @param option search option must not be null
     * @return search result
     */
    Mono<SearchResult> search(SearchOption option);

}
