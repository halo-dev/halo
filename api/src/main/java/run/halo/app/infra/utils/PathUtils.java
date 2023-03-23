package run.halo.app.infra.utils;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Http path manipulation tool class.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@UtilityClass
public class PathUtils {

    /**
     * Every HTTP URL conforms to the syntax of a generic URI. The URI generic syntax consists of
     * components organized hierarchically in order of decreasing significance from left to
     * right:
     * <pre>
     * URI = scheme ":" ["//" authority] path ["?" query] ["#" fragment]
     * </pre>
     * The authority component consists of subcomponents:
     * <pre>
     * authority = [userinfo "@"] host [":" port]
     * </pre>
     * Examples of popular schemes include http, https, ftp, mailto, file, data and irc. URI
     * schemes should be registered with the
     * <a href="https://en.wikipedia.org/wiki/Internet_Assigned_Numbers_Authority">Internet Assigned Numbers Authority (IANA)</a>, although
     * non-registered schemes are used in practice.
     *
     * @param uriString url or path
     * @return true if the linkBase is absolute, otherwise false
     * @see <a href="https://en.wikipedia.org/wiki/URL">URL</a>
     */
    public static boolean isAbsoluteUri(final String uriString) {
        if (StringUtils.isBlank(uriString)) {
            return false;
        }
        try {
            URI uri = new URI(uriString);
            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            log.debug("Failed to parse uri: " + uriString, e);
            // ignore this exception
            return false;
        }
    }

    /**
     * Combine paths based on the passed in path segments parameters.
     * <br/><br/>
     * This method doesn't work for Windows system currently.
     *
     * @param pathSegments Path segments to be combined
     * @return the combined path
     */
    public static String combinePath(String... pathSegments) {
        StringBuilder sb = new StringBuilder();
        for (String path : pathSegments) {
            if (path == null) {
                continue;
            }
            String s = path.startsWith("/") ? path : "/" + path;
            String segment = s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
            sb.append(segment);
        }
        return sb.toString();
    }


    /**
     * <p>Append a {@code '/'} if the path does not end with a {@code '/'}.</p>
     * Examples are as follows:
     * <pre>
     *     PathUtils.appendPathSeparatorIfMissing("hello") -> hello/
     *     PathUtils.appendPathSeparatorIfMissing("some-path/") -> some-path/
     *     PathUtils.appendPathSeparatorIfMissing(null) -> null
     * </pre>
     *
     * @param path a path
     * @return A new String if suffix was appended, the same string otherwise.
     */
    public static String appendPathSeparatorIfMissing(String path) {
        return StringUtils.appendIfMissing(path, "/", "/");
    }

    /**
     * <p>Remove the regex in the path pattern placeholder.</p>
     * <p>For example: </p>
     * <ul>
     * <li>'{@code /{year:\d{4}}/{month:\d{2}}}' &rarr; '{@code /{year}/{month}}'</li>
     * <li>'{@code /archives/{year:\d{4}}/{month:\d{2}}}' &rarr; '{@code /archives/{year}/{month}
     * }'</li>
     * <li>'{@code /archives/{year:\d{4}}/{slug}}' &rarr; '{@code /archives/{year}/{slug}}'</li>
     * </ul>
     *
     * @param pattern path pattern
     * @return Simplified path pattern
     */
    public static String simplifyPathPattern(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return StringUtils.EMPTY;
        }
        String[] parts = StringUtils.split(pattern, '/');
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("{") && part.endsWith("}")) {
                int colonIdx = part.indexOf(':');
                if (colonIdx != -1) {
                    parts[i] = part.substring(0, colonIdx) + part.charAt(part.length() - 1);
                }

            }
        }
        return combinePath(parts);
    }
}
