package run.halo.app.infra.utils;

import java.net.IDN;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Utilities for external URLs.
 *
 * @author johnniang
 */
public final class ExternalUrlUtils {

    private ExternalUrlUtils() {}

    /**
     * Converts the host of the given external URL to ASCII (punycode) if necessary.
     *
     * <p>For example, {@code https://abcd.中国} will be converted to {@code https://abcd.xn--fiqs8s}. The rest of the URL
     * remains unchanged.
     *
     * @param externalUrl external URL to convert, may be null
     * @return converted URL or the original URL if it does not need to be converted
     */
    @Nullable
    public static String toAscii(@Nullable String externalUrl) {
        if (externalUrl == null || !externalUrl.contains("://")) {
            return externalUrl;
        }
        var schemeSeparatorIndex = externalUrl.indexOf("://");
        var authorityStartIndex = schemeSeparatorIndex + 3;
        var authorityEndIndex = externalUrl.length();
        for (int i = authorityStartIndex; i < externalUrl.length(); i++) {
            var c = externalUrl.charAt(i);
            if (c == '/' || c == '?' || c == '#') {
                authorityEndIndex = i;
                break;
            }
        }
        if (authorityEndIndex <= authorityStartIndex) {
            return externalUrl;
        }
        var authority = externalUrl.substring(authorityStartIndex, authorityEndIndex);
        var convertedAuthority = toAsciiAuthority(authority);
        if (convertedAuthority.equals(authority)) {
            return externalUrl;
        }
        return externalUrl.substring(0, authorityStartIndex)
                + convertedAuthority
                + externalUrl.substring(authorityEndIndex);
    }

    private static String toAsciiAuthority(String authority) {
        var atIndex = authority.lastIndexOf('@');
        var userInfo = atIndex >= 0 ? authority.substring(0, atIndex + 1) : "";
        var hostPort = atIndex >= 0 ? authority.substring(atIndex + 1) : authority;
        if (StringUtils.isBlank(hostPort)) {
            return authority;
        }
        if (hostPort.startsWith("[")) {
            // IPv6 literal, no conversion is needed.
            return authority;
        }
        var host = hostPort;
        var port = "";
        var colonIndex = hostPort.lastIndexOf(':');
        if (colonIndex >= 0) {
            host = hostPort.substring(0, colonIndex);
            port = hostPort.substring(colonIndex);
        }
        if (StringUtils.isBlank(host)) {
            return authority;
        }
        try {
            return userInfo + IDN.toASCII(host) + port;
        } catch (IllegalArgumentException e) {
            // Keep the original authority if the host cannot be converted.
            return authority;
        }
    }
}
