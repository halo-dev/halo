package run.halo.app.utils;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.support.HaloConst;

/**
 * Common utils
 *
 * @author ryanwang
 * @author johnniang
 * @author guqing
 * @date 2017-12-22
 */
@Slf4j
public class HaloUtils {

    public static final String URL_SEPARATOR = "/";
    private static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";

    @NonNull
    public static String ensureBoth(@NonNull String string, @NonNull String bothfix) {
        return ensureBoth(string, bothfix, bothfix);
    }

    @NonNull
    public static String ensureBoth(@NonNull String string, @NonNull String prefix,
        @NonNull String suffix) {
        return ensureSuffix(ensurePrefix(string, prefix), suffix);
    }

    /**
     * Ensures the string contain prefix.
     *
     * @param string string must not be blank
     * @param prefix prefix must not be blank
     * @return string contain prefix specified
     */
    @NonNull
    public static String ensurePrefix(@NonNull String string, @NonNull String prefix) {
        Assert.hasText(string, "String must not be blank");
        Assert.hasText(prefix, "Prefix must not be blank");

        return prefix + StringUtils.removeStart(string, prefix);
    }


    /**
     * Ensures the string contain suffix.
     *
     * @param string string must not be blank
     * @param suffix suffix must not be blank
     * @return string contain suffix specified
     */
    @NonNull
    public static String ensureSuffix(@NonNull String string, @NonNull String suffix) {
        Assert.hasText(string, "String must not be blank");
        Assert.hasText(suffix, "Suffix must not be blank");

        return StringUtils.removeEnd(string, suffix) + suffix;
    }

    /**
     * Composites partial url to full http url.
     *
     * @param partUrls partial urls must not be empty
     * @return full url
     */
    public static String compositeHttpUrl(@NonNull String... partUrls) {
        Assert.notEmpty(partUrls, "Partial url must not be blank");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < partUrls.length; i++) {
            String partUrl = partUrls[i];
            if (StringUtils.isBlank(partUrl)) {
                continue;
            }
            partUrl = StringUtils.removeStart(partUrl, URL_SEPARATOR);
            partUrl = StringUtils.removeEnd(partUrl, URL_SEPARATOR);
            if (i != 0) {
                builder.append(URL_SEPARATOR);
            }
            builder.append(partUrl);
        }

        return builder.toString();
    }

    /**
     * Desensitizes the plain text.
     *
     * @param plainText plain text must not be null
     * @param leftSize left size
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
     * @param times times
     * @param label label
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
     * @param originalUrl original url
     * @return normalized url.
     */
    @NonNull
    public static String normalizeUrl(@NonNull String originalUrl) {
        Assert.hasText(originalUrl, "Original Url must not be blank");

        if (StringUtils.startsWithAny(originalUrl, URL_SEPARATOR, HaloConst.PROTOCOL_HTTPS,
            HaloConst.PROTOCOL_HTTP)
            && !StringUtils.startsWith(originalUrl, "//")) {
            return originalUrl;
        }

        final int sepIndex = originalUrl.indexOf("://");
        String protocol;
        String body;
        if (sepIndex > 0) {
            protocol = StringUtils.substring(originalUrl, 0, sepIndex + 3);
            body = StringUtils.substring(originalUrl, sepIndex + 3, originalUrl.length());
        } else {
            protocol = "http://";
            body = originalUrl;
        }

        final int paramsSepIndex = StringUtils.indexOf(body, '?');
        String params = null;
        if (paramsSepIndex > 0) {
            params = StringUtils.substring(body, paramsSepIndex, body.length());
            body = StringUtils.substring(body, 0, paramsSepIndex);
        }

        if (StringUtils.isNotEmpty(body)) {
            // 去除开头的\或者/
            body = body.replaceAll("^[\\\\/]+", StringUtils.EMPTY);
            // 替换多个\或/为单个/
            body = body.replace("\\", "/").replaceAll("//+", "/");
        }

        final int pathSepIndex = StringUtils.indexOf(body, '/');
        String domain = body;
        String path = null;
        if (pathSepIndex > 0) {
            domain = StringUtils.substring(body, 0, pathSepIndex);
            path = StringUtils.substring(body, pathSepIndex, body.length());
        }
        return protocol + domain + StringUtils.defaultIfEmpty(path, StringUtils.EMPTY)
            + StringUtils.defaultIfEmpty(params, StringUtils.EMPTY);
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

    /**
     * Clean all html tag.
     *
     * @param content html document
     * @return text before cleaned
     */
    public static String cleanHtmlTag(String content) {
        if (StringUtils.isEmpty(content)) {
            return StringUtils.EMPTY;
        }
        return content.replaceAll(RE_HTML_MARK, StringUtils.EMPTY);
    }

    /**
     * Determine whether the collection is empty.
     *
     * @param collection collection
     * @return true if this collection not null and contains elements.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !CollectionUtils.isEmpty(collection);
    }

    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * @param str the String to remove characters from, may be null
     * @param prefixStripChars the characters to remove from start of str, null treated as
     *                        whitespace
     * @param suffixStripChars the characters to remove from end of str, null treated as whitespace
     * @return the stripped String, {@code null} if null String input
     */
    public static String strip(String str, final String prefixStripChars,
        final String suffixStripChars) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        str = StringUtils.stripStart(str, prefixStripChars);
        return StringUtils.stripEnd(str, suffixStripChars);
    }


    /**
     * 分页彩虹算法<br>
     * 来自：https://github.com/iceroot/iceroot/blob/master/src/main/java/com/icexxx/util/IceUtil.java<br>
     * 通过传入的信息，生成一个分页列表显示
     *
     * @param pageNo       当前页
     * @param totalPage    总页数
     * @param displayCount 每屏展示的页数
     * @return 分页条
     */
    public static int[] rainbow(int pageNo, int totalPage, int displayCount) {
        boolean isEven = displayCount % 2 == 0;
        int left = displayCount / 2;
        int right = displayCount / 2;

        int length = displayCount;
        if (isEven) {
            right++;
        }
        if (totalPage < displayCount) {
            length = totalPage;
        }
        int[] result = new int[length];
        if (totalPage >= displayCount) {
            if (pageNo <= left) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = i + 1;
                }
            } else if (pageNo > totalPage - right) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = i + totalPage - displayCount + 1;
                }
            } else {
                for (int i = 0; i < result.length; i++) {
                    result[i] = i + pageNo - left + (isEven ? 1 : 0);
                }
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = i + 1;
            }
        }
        return result;
    }

    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * generate png qrcode to byte array.
     *
     * @param content qrcode content
     * @param width qrcode width
     * @param height qrcode height
     * @return QR code byte array in png format
     * @throws UnsupportedOperationException If the QR code fails to be generated
     */
    public static byte[] generateQrCodeToPng(String content, int width, int height) {
        Map<EncodeHintType, Object> hints = new HashMap<>(3, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 纠错级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 空白
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix byteMatrix =
                qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            // Make the BufferedImage that are to hold the QRCode
            int matrixWidth = byteMatrix.getWidth();
            int matrixHeight = byteMatrix.getHeight();
            BufferedImage image =
                new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, matrixWidth, matrixHeight);
            // Paint and save the image using the ByteMatrix
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < matrixWidth; i++) {
                for (int j = 0; j < matrixWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return os.toByteArray();
        } catch (IOException | WriterException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
