package run.halo.app.localization;

import java.util.Set;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

/**
 * Aggregates public route ownership declared by Core router components and enabled {@link LocalizationRouteProvider}s.
 *
 * <p>The service provides a stable query surface for localization consumers. It does not authorize a request and does
 * not define language slugs; it only reports routes currently owned by Core or plugins.
 *
 * @since 2.28.0
 */
public interface LocalizationRouteService {
    /**
     * Gets every first path segment currently claimed by an enabled provider.
     *
     * @return an immutable set of claimed segments without a leading slash
     */
    Mono<Set<String>> claimedPrefixes();

    /**
     * Determines whether an enabled provider owns the application-relative request path.
     *
     * @param path request path starting with {@code /}
     * @param method HTTP request method
     * @return true when a provider owns the request
     */
    Mono<Boolean> matches(String path, HttpMethod method);
}
