package run.halo.app.infra.utils;

import com.google.common.io.Files;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public final class FileNameUtils {

    private FileNameUtils() {
    }

    /**
     * Check whether the file name has an extension.
     *
     * @param filename is name of file.
     * @return True if file name has extension, otherwise false.
     */
    public static boolean hasFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        var extensionRegex = ".*\\.[a-zA-Z0-9]+$";
        return Pattern.matches(extensionRegex, filename);
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
        return renameFilename(
            filename, () -> RandomStringUtils.secure().nextAlphabetic(length), false
        );
    }

    public static String renameFilename(
        String filename,
        Supplier<String> renameSupplier,
        boolean excludeBasename) {
        var nameWithoutExt = Files.getNameWithoutExtension(filename);
        var ext = Files.getFileExtension(filename);
        var rename = renameSupplier.get();
        if (StringUtils.isBlank(nameWithoutExt)) {
            return rename + "." + ext;
        }
        if (StringUtils.isBlank(ext)) {
            if (excludeBasename) {
                return rename;
            }
            return nameWithoutExt + "-" + rename;
        }
        if (excludeBasename) {
            return rename + "." + ext;
        }
        return nameWithoutExt + "-" + rename + "." + ext;
    }
}
