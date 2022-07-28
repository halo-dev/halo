package run.halo.app.infra.utils;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.thymeleaf.util.StringUtils;

/**
 * File system path utils.
 *
 * @author guqing
 * @since 2.0.0
 */
public class FilePathUtils {
    private FilePathUtils() {
    }

    public static String cleanPath(final String path) {

        if (path == null) {
            return null;
        }

        // First replace Windows folder separators with UNIX's
        String unixPath = StringUtils.replace(path, "\\", "/");

        // Some shortcuts, just in case this is empty or simply has no '.' or '..' (and no
        // double-/ we should simplify)
        if (unixPath.length() == 0 || (!unixPath.contains("/.") && !unixPath.contains("//"))) {
            return unixPath;
        }

        // We make sure path starts with '/' in order to simplify the algorithm
        boolean rootBased = (unixPath.charAt(0) == '/');
        unixPath = (rootBased ? unixPath : ('/' + unixPath));

        // We will traverse path in reverse order, looking for '.' and '..' tokens and processing
        // them
        final StringBuilder strBuilder = new StringBuilder(unixPath.length());

        int index = unixPath.lastIndexOf('/');
        int pos = unixPath.length() - 1;
        int topCount = 0;
        while (index
            >= 0) { // will always be 0 for the last iteration, as we prefixed the path with '/'

            final int tokenLen = pos - index;

            if (tokenLen > 0) {

                if (tokenLen == 1 && unixPath.charAt(index + 1) == '.') {
                    // Token is '.' -> just ignore it
                } else if (tokenLen == 2 && unixPath.charAt(index + 1) == '.'
                    && unixPath.charAt(index + 2) == '.') {
                    // Token is '..' -> count as a 'top' operation
                    topCount++;
                } else if (topCount > 0) {
                    // Whatever comes here has been removed by a 'top' operation, so ignore
                    topCount--;
                } else {
                    // Token is OK, just add (with its corresponding '/')
                    strBuilder.insert(0, unixPath, index, (index + tokenLen + 1));
                }

            }

            pos = index - 1;
            index = (pos >= 0 ? unixPath.lastIndexOf('/', pos) : -1);

        }

        // Add all 'top' tokens appeared at the very beginning of the path
        for (int i = 0; i < topCount; i++) {
            strBuilder.insert(0, "/..");
        }

        // Perform last cleanup
        if (!rootBased) {
            strBuilder.deleteCharAt(0);
        }

        return strBuilder.toString();
    }

    public static Path combinePath(String first, String... more) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(first, more);
        return Paths.get(cleanPath(path.toString()));
    }
}
