package run.halo.app.security;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Utilities for building redirect responses to the saved request. */
public final class RedirectUtils {

    private RedirectUtils() {}

    /**
     * Redirects to the URI saved in the request cache, or the fallback URI when none is saved.
     *
     * @param requestCache the request cache to read the redirect URI from
     * @param exchange the current exchange
     * @param fallbackUri the URI to redirect to when the request cache is empty
     * @return a 302 response to the resolved target
     */
    public static Mono<ServerResponse> redirectToSavedRequest(
            ServerRequestCache requestCache, ServerWebExchange exchange, URI fallbackUri) {
        return requestCache
                .getRedirectUri(exchange)
                .defaultIfEmpty(fallbackUri)
                .flatMap(uri ->
                        ServerResponse.status(HttpStatus.FOUND).location(uri).build());
    }
}
