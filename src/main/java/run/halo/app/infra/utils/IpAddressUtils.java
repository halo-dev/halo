package run.halo.app.infra.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * Ip address utils.
 * Code from internet.
 */
public class IpAddressUtils {
    private static final String UNKNOWN = "unknown";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

    /**
     * Gets the ip address from request.
     *
     * @param request http request
     * @return ip address if found, otherwise {@link #UNKNOWN}.
     */
    public static String getIpAddress(ServerRequest request) {
        try {
            return getIpAddressInternal(request);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    private static String getIpAddressInternal(ServerRequest request) {
        HttpHeaders httpHeaders = request.headers().asHttpHeaders();
        String xrealIp = httpHeaders.getFirst(X_REAL_IP);
        String xforwardedFor = httpHeaders.getFirst(X_FORWARDED_FOR);

        if (StringUtils.isNotEmpty(xforwardedFor) && !UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            // After multiple reverse proxies, there will be multiple IP values. The first IP is
            // the real IP
            int index = xforwardedFor.indexOf(",");
            if (index != -1) {
                return xforwardedFor.substring(0, index);
            } else {
                return xforwardedFor;
            }
        }
        xforwardedFor = xrealIp;
        if (StringUtils.isNotEmpty(xforwardedFor) && !UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            return xforwardedFor;
        }
        if (StringUtils.isBlank(xforwardedFor) || UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            xforwardedFor = httpHeaders.getFirst(PROXY_CLIENT_IP);
        }
        if (StringUtils.isBlank(xforwardedFor) || UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            xforwardedFor = httpHeaders.getFirst(WL_PROXY_CLIENT_IP);
        }
        if (StringUtils.isBlank(xforwardedFor) || UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            xforwardedFor = httpHeaders.getFirst(HTTP_CLIENT_IP);
        }
        if (StringUtils.isBlank(xforwardedFor) || UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            xforwardedFor = httpHeaders.getFirst(HTTP_X_FORWARDED_FOR);
        }
        if (StringUtils.isBlank(xforwardedFor) || UNKNOWN.equalsIgnoreCase(xforwardedFor)) {
            xforwardedFor = request.remoteAddress()
                .map(remoteAddress -> remoteAddress.getAddress().getHostAddress())
                .orElse(UNKNOWN);
        }
        return xforwardedFor;
    }
}
