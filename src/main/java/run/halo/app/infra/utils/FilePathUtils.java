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

    public static Path combinePath(String first, String... more) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(first, more);
        String unixPath = StringUtils.replace(path.normalize(), "\\", "/");
        return Paths.get(unixPath);
    }
}
