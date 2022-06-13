package run.halo.app.infra.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * Path manipulation tool class.
 *
 * @author guqing
 * @since 2.0.0
 */
@UtilityClass
public class PathUtils {

    /**
     * Combine paths based on the passed in path segments parameters.
     *
     * @param pathSegments Path segments to be combined
     * @return the combined path
     */
    public static String combinePath(String... pathSegments) {
        StringBuilder sb = new StringBuilder();
        for (String path : pathSegments) {
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
}
