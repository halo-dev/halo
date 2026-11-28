package run.halo.app.theme.router.factories;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.theme.utils.PatternUtils.normalizePattern;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.router.ModelConst;
import run.halo.app.theme.utils.PublicRoutePatterns;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RouteFactory {
    RouterFunction<ServerResponse> create(String pattern);

    /**
     * Gets the public path patterns registered by this factory.
     *
     * <p>A factory that serves dynamic resources may return no syntactic pattern and override
     * {@link #claimedPrefixes(String)} and {@link #matches(String, String, HttpMethod)} instead.
     *
     * @param pattern normalized theme route pattern
     * @return public path patterns
     */
    default List<String> publicPatterns(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return List.of();
        }
        if ("/".equals(pattern.trim())) {
            return List.of("/");
        }
        return List.of(normalizePattern(pattern));
    }

    /**
     * Gets first path segments statically claimed by this factory.
     *
     * @param pattern normalized theme route pattern
     * @return claimed first segments
     */
    default Mono<Set<String>> claimedPrefixes(String pattern) {
        return Mono.just(PublicRoutePatterns.claimedPrefixes(publicPatterns(pattern)));
    }

    /**
     * Determines whether this factory owns a public request.
     *
     * @param pattern normalized theme route pattern
     * @param path application-relative request path
     * @param method request method
     * @return true when this factory owns the request
     */
    default Mono<Boolean> matches(String pattern, String path, HttpMethod method) {
        return Mono.just(PublicRoutePatterns.matchesAny(path, method, publicPatterns(pattern)));
    }

    default Mono<Integer> configuredPageSize(
            SystemConfigFetcher environmentFetcher, Function<SystemSetting.Post, Integer> mapper) {
        return environmentFetcher.fetchPost().map(p -> defaultIfNull(mapper.apply(p), ModelConst.DEFAULT_PAGE_SIZE));
    }

    default int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }
}
