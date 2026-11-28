package run.halo.app.localization;

import java.util.Set;
import org.pf4j.ExtensionPoint;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

/**
 * Declares public routes owned by Core or a plugin.
 *
 * <p>This is a {@code MULTI_INSTANCE} extension point. Core's theme router and single-page route implement it for
 * built-in routes; plugins implement it for their own routes. Localization consumers use the declarations to avoid
 * matching a language prefix to a native route and to identify language slugs that could shadow an existing public
 * route. A provider returns only routes that it can authoritatively decide; it must not infer ownership from a title,
 * slug or other field name.
 *
 * <p>Implementations must perform non-blocking reads, remain free of side effects and return route paths relative to
 * the application context path. A path starts with {@code /} and contains no query string or fragment.
 *
 * @since 2.28.0
 */
public interface LocalizationRouteProvider extends ExtensionPoint {
    /**
     * Gets first path segments currently claimed by this provider.
     *
     * <p>The result identifies public routes that a language slug could shadow. Providers with dynamic resource routes
     * should include the concrete prefixes of resources that currently have a public representation. An empty result is
     * valid.
     *
     * @return claimed first path segments without a leading slash
     */
    Mono<Set<String>> claimedPrefixes();

    /**
     * Determines whether this provider owns a public request.
     *
     * <p>Return true only when the provider can route the request independently of language handling. A static route
     * can be matched directly. A dynamic route must verify that the referenced public resource exists instead of
     * claiming every path that its pattern can syntactically match. The default owns no request.
     *
     * @param path application-relative request path starting with {@code /}
     * @param method HTTP request method
     * @return true when the provider owns the request
     */
    default Mono<Boolean> matches(String path, HttpMethod method) {
        return Mono.just(false);
    }
}
