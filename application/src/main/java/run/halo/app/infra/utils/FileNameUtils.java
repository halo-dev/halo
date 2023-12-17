package run.halo.app.infra.utils;

import com.google.common.io.Files;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public final class FileNameUtils {

    private FileNameUtils() {
    }

    public static String removeFileExtension(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        var extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    /**
     * Append random string after file name.
     * <pre>
     * Case 1: halo.run -> halo-xyz.run
     * Case 2: .run -> xyz.run
     * Case 3: halo -> halo-xyz
     * </pre>
     *
     * @param filename is name of file.
     * @param length is for generating random string with specific length.
     * @return File name with random string.
     */
    public static String randomFileName(String filename, int length) {
        var nameWithoutExt = Files.getNameWithoutExtension(filename);
        var ext = Files.getFileExtension(filename);
        var random = RandomStringUtils.randomAlphabetic(length).toLowerCase();
        if (StringUtils.isBlank(nameWithoutExt)) {
            return random + "." + ext;
        }
        if (StringUtils.isBlank(ext)) {
            return nameWithoutExt + "-" + random;
        }
        return nameWithoutExt + "-" + random + "." + ext;
    }
}
