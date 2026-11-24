package run.halo.app.theme.utils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Utilities for public route patterns that are registered by Halo's theme router.
 *
 * <p>The utilities intentionally work on route patterns instead of resource types. A static pattern can be matched
 * directly, while a route with dynamic public resources remains responsible for deciding whether a concrete request
 * belongs to it.
 */
public enum PublicRoutePatterns {
    ;

    /**
     * Whether a request method can be served by a public theme route.
     *
     * @param method request method
     * @return true for GET and HEAD
     */
    public static boolean supportsRead(HttpMethod method) {
        return HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method);
    }

    /**
     * Determines whether a path matches one of the supplied route patterns.
     *
     * @param path request path
     * @param method request method
     * @param patterns route patterns
     * @return true when the request is owned by one of the patterns
     */
    public static boolean matchesAny(String path, HttpMethod method, Collection<String> patterns) {
        if (!supportsRead(method) || patterns.isEmpty()) {
            return false;
        }
        var container = pathContainer(path);
        if (container == null) {
            return false;
        }
        return patterns.stream().filter(PublicRoutePatterns::hasText).anyMatch(pattern -> matches(pattern, container));
    }

    /**
     * Matches a path and extracts variables from the first matching route pattern.
     *
     * @param path request path
     * @param patterns route patterns
     * @return URI variables when a pattern matches, otherwise empty
     */
    public static Optional<Map<String, String>> matchAndExtract(String path, Collection<String> patterns) {
        if (!hasText(path) || patterns == null || patterns.isEmpty()) {
            return Optional.empty();
        }
        var container = pathContainer(path);
        if (container == null) {
            return Optional.empty();
        }
        for (var pattern : patterns) {
            if (!hasText(pattern)) {
                continue;
            }
            try {
                var match = PathPatternParser.defaultInstance.parse(pattern).matchAndExtract(container);
                if (match != null) {
                    return Optional.of(Map.copyOf(match.getUriVariables()));
                }
            } catch (IllegalArgumentException error) {
                // An invalid pattern cannot own a request.
            }
        }
        return Optional.empty();
    }

    /**
     * Determines whether a request path and a route path identify the same public resource.
     *
     * <p>Both decoded and percent-encoded forms are accepted because permalinks and request paths can originate from
     * different URL producers.
     *
     * @param path request path
     * @param routePath public route path
     * @return true when both paths refer to the same path
     */
    public static boolean samePath(String path, String routePath) {
        if (!hasText(routePath)) {
            return false;
        }
        var normalizedPath = normalizePath(path);
        var normalizedRoutePath = normalizePath(routePath);
        if (Objects.equals(normalizedPath, normalizedRoutePath)) {
            return true;
        }
        try {
            return Objects.equals(
                    UriUtils.decode(normalizedPath, StandardCharsets.UTF_8),
                    UriUtils.decode(normalizedRoutePath, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    /**
     * Gets first path segments statically claimed by the supplied patterns.
     *
     * @param patterns route patterns
     * @return immutable claimed first segments
     */
    public static Set<String> claimedPrefixes(Collection<String> patterns) {
        var prefixes = new LinkedHashSet<String>();
        patterns.stream()
                .map(PublicRoutePatterns::staticPrefix)
                .filter(PublicRoutePatterns::hasText)
                .forEach(prefixes::add);
        return Set.copyOf(prefixes);
    }

    /**
     * Extracts a first segment that can be used as a language slug.
     *
     * @param path route path or pattern
     * @return first segment when it is a static localizable segment, otherwise an empty string
     */
    public static String staticPrefix(String path) {
        var first = firstSegment(path);
        return isLocalizablePrefix(first) ? first : "";
    }

    /**
     * Gets the first path segment, ignoring slashes inside pattern variables.
     *
     * @param path route path or pattern
     * @return first segment without a leading slash
     */
    public static String firstSegment(String path) {
        if (path == null) {
            return "";
        }
        var start = path.startsWith("/") ? 1 : 0;
        var depth = 0;
        var index = start;
        while (index < path.length()) {
            var character = path.charAt(index);
            if (character == '\\') {
                index += 2;
            } else {
                if (character == '{') {
                    depth++;
                } else if (character == '}') {
                    depth--;
                } else if (character == '/' && depth == 0) {
                    return path.substring(start, index);
                }
                index++;
            }
        }
        return path.substring(start);
    }

    private static boolean isLocalizablePrefix(String value) {
        if (value.isEmpty()
                || value.charAt(0) == '-'
                || value.charAt(value.length() - 1) == '-'
                || value.contains("--")) {
            return false;
        }
        return value.chars().allMatch(character -> {
            var valueCharacter = (char) character;
            return isLowercaseLetterOrDigit(valueCharacter) || valueCharacter == '-';
        });
    }

    private static boolean isLowercaseLetterOrDigit(char character) {
        return (character >= 'a' && character <= 'z') || (character >= '0' && character <= '9');
    }

    private static boolean matches(String pattern, PathContainer path) {
        try {
            return PathPatternParser.defaultInstance.parse(pattern).matches(path);
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private static PathContainer pathContainer(String path) {
        try {
            return PathContainer.parsePath(normalizePath(path));
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    private static String normalizePath(String path) {
        if (!hasText(path)) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
