package run.halo.app.infra.utils;

import java.util.List;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Path conventions for public APIs.
 *
 * <p>Keep this definition aligned with the public API grouping used by OpenAPI. A public API is an {@code /apis/api.*}
 * route, except for routes that are explicitly Console or user-center APIs.
 *
 * @since 2.28.0
 */
public final class PublicApiPaths {

    public static final String PATTERN = "/apis/api.*/**";

    public static final List<String> EXCLUDED_PATTERNS = List.of(
            "/apis/api.console.*/**",
            "/apis/api.notification.halo.run/v1alpha1/userspaces/**",
            "/apis/api.notification.halo.run/v1alpha1/notifiers/**");

    private static final PathPatternParser PARSER = new PathPatternParser();

    private static final PathPattern PUBLIC_API = PARSER.parse(PATTERN);

    private static final List<PathPattern> EXCLUDED =
            EXCLUDED_PATTERNS.stream().map(PARSER::parse).toList();

    private PublicApiPaths() {}

    /**
     * Determines whether the path belongs to the public API surface.
     *
     * @param path request path
     * @return true for public API paths
     */
    public static boolean matches(PathContainer path) {
        return PUBLIC_API.matches(path) && EXCLUDED.stream().noneMatch(pattern -> pattern.matches(path));
    }
}
