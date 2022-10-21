package run.halo.app.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.exception.FileOperationException;

/**
 * Filename utilities.
 *
 * @author johnniang
 * @date 3/26/19
 */
public class FilenameUtils {

    private static final Pattern FILENAME_RESERVED_CHARS_PATTERN =
        Pattern.compile("[\\\\/:*?\"<>|.]");

    private static final Pattern FILENAME_WIN_RESERVED_NAMES_PATTERN =
        Pattern.compile("^(CON|PRM|AUX|NUL|COM[0-9]|LPT[0-9])$");

    private static final int FILENAME_MAX_LENGTH = 200;

    private FilenameUtils() {
    }

    /**
     * Gets base name of file name. <br>
     * eg: <br>
     * filename: /home/test/test.txt <br>
     * basename: test
     *
     * @param filename filename must not be blank
     * @return basename of the given file name
     */
    @NonNull
    public static String getBasename(@NonNull String filename) {
        Assert.hasText(filename, "Filename must not be blank");

        // Find the last slash
        int separatorLastIndex = StringUtils.lastIndexOf(filename, File.separatorChar);

        if (separatorLastIndex == filename.length() - 1) {
            return StringUtils.EMPTY;
        }

        if (separatorLastIndex >= 0 && separatorLastIndex < filename.length() - 1) {
            filename = filename.substring(separatorLastIndex + 1);
        }

        // Find last dot
        int dotLastIndex = StringUtils.lastIndexOf(filename, '.');

        String[] split = filename.split("\\.");

        List<String> extList = Arrays.asList("gz", "bz2");

        if (extList.contains(split[split.length - 1]) && split.length >= 3) {
            return filename.substring(0, filename.substring(0, dotLastIndex).lastIndexOf('.'));
        }

        if (dotLastIndex < 0) {
            return filename;
        }

        return filename.substring(0, dotLastIndex);
    }

    /**
     * Gets extension of the file name. <br>
     * <code>
     * eg: <br>
     * filename: /home/test/test.txt <br>
     * extension: txt
     * </code>
     *
     * @param filename filename must not be blank
     * @return an extension of the given file name
     */
    @NonNull
    public static String getExtension(@NonNull String filename) {
        Assert.hasText(filename, "Filename must not be blank");

        // Find the last slash
        int separatorLastIndex = StringUtils.lastIndexOf(filename, File.separatorChar);

        if (separatorLastIndex == filename.length() - 1) {
            return StringUtils.EMPTY;
        }

        if (separatorLastIndex >= 0 && separatorLastIndex < filename.length() - 1) {
            filename = filename.substring(separatorLastIndex + 1);
        }

        // Find last dot
        int dotLastIndex = StringUtils.lastIndexOf(filename, '.');

        if (dotLastIndex < 0) {
            return StringUtils.EMPTY;
        }

        String[] split = filename.split("\\.");

        List<String> extList = Arrays.asList("gz", "bz2");

        if (extList.contains(split[split.length - 1]) && split.length >= 3) {
            return filename.substring(filename.substring(0, dotLastIndex).lastIndexOf('.') + 1);
        }

        return filename.substring(dotLastIndex + 1);
    }

    /**
     * @param filename filename
     * @return sanitized filename, without any reserved character
     */
    public static String sanitizeFilename(String filename) {
        String sanitizedFilename =
            FILENAME_RESERVED_CHARS_PATTERN.matcher(filename.trim()).replaceAll("");
        if (StringUtils.isEmpty(sanitizedFilename)) {
            throw new FileOperationException("文件名不合法: " + filename);
        }
        Matcher matcher = FILENAME_WIN_RESERVED_NAMES_PATTERN.matcher(sanitizedFilename);
        if (matcher.matches()) {
            sanitizedFilename = sanitizedFilename + "_file";
        }
        return sanitizedFilename.length() < FILENAME_MAX_LENGTH ? sanitizedFilename :
            sanitizedFilename.substring(0, FILENAME_MAX_LENGTH).trim();
    }
}
