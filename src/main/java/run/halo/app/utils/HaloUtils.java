package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.UUID;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;

/**
 * Common utils
 *
 * @author ryanwang
 * @date : 2017/12/22
 */
@Slf4j
public class HaloUtils {

    /**
     * Desensitizes the plain text.
     *
     * @param plainText plain text must not be null
     * @param leftSize  left size
     * @param rightSize right size
     * @return desensitization
     */
    public static String desensitize(@NonNull String plainText, int leftSize, int rightSize) {
        Assert.hasText(plainText, "Plain text must not be blank");

        if (leftSize < 0) {
            leftSize = 0;
        }

        if (leftSize > plainText.length()) {
            leftSize = plainText.length();
        }

        if (rightSize < 0) {
            rightSize = 0;
        }

        if (rightSize > plainText.length()) {
            rightSize = plainText.length();
        }

        if (plainText.length() < leftSize + rightSize) {
            rightSize = plainText.length() - leftSize;
        }

        int remainSize = plainText.length() - rightSize - leftSize;

        String left = StringUtils.left(plainText, leftSize);
        String right = StringUtils.right(plainText, rightSize);
        return StringUtils.rightPad(left, remainSize + leftSize, '*') + right;
    }

    /**
     * Changes file separator to url separator.
     *
     * @param pathname full path name must not be blank.
     * @return text with url separator
     */
    public static String changeFileSeparatorToUrlSeparator(@NonNull String pathname) {
        Assert.hasText(pathname, "Path name must not be blank");

        return pathname.replace(FILE_SEPARATOR, "/");
    }

    /**
     * Time format.
     *
     * @param totalSeconds seconds
     * @return formatted time
     */
    @NonNull
    public static String timeFormat(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 second";
        }

        StringBuilder timeBuilder = new StringBuilder();

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 3600 % 60;

        if (hours > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(hours, "hour", "hours"));
        }

        if (minutes > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(minutes, "minute", "minutes"));
        }

        if (seconds > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(seconds, "second", "seconds"));
        }

        return timeBuilder.toString();
    }

    /**
     * Pluralize the times label format.
     *
     * @param times       times
     * @param label       label
     * @param pluralLabel plural label
     * @return pluralized format
     */
    @NonNull
    public static String pluralize(long times, @NonNull String label, @NonNull String pluralLabel) {
        Assert.hasText(label, "Label must not be blank");
        Assert.hasText(pluralLabel, "Plural label must not be blank");

        if (times <= 0) {
            return "no " + pluralLabel;
        }

        if (times == 1) {
            return times + " " + label;
        }

        return times + " " + pluralLabel;
    }

    /**
     * Gets random uuid without dash.
     *
     * @return random uuid without dash
     */
    @NonNull
    public static String randomUUIDWithoutDash() {
        return StringUtils.remove(UUID.randomUUID().toString(), '-');
    }

    /**
     * Initialize url if blank.
     *
     * @param url url can be blank
     * @return initial url
     */
    @NonNull
    public static String initializeUrlIfBlank(@Nullable String url) {
        if (!StringUtils.isBlank(url)) {
            return url;
        }
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Normalize url.
     *
     * @param url url must not be blank
     * @return normalized url
     */
    @NonNull
    public static String normalizeUrl(@NonNull String url) {
        Assert.hasText(url, "Url must not be blank");

        StringUtils.removeEnd(url, "html");
        StringUtils.removeEnd(url, "htm");

        return SlugUtils.slugify(url);
    }

    /**
     * Gets machine IP address.
     *
     * @return current machine IP address.
     */
    public static String getMachineIP() {
        InetAddress machineAddress;
        try {
            machineAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            machineAddress = InetAddress.getLoopbackAddress();
        }
        return machineAddress.getHostAddress();
    }
}
