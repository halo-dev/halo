package run.halo.app.infra.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * Ip address utils.
 * Code from internet.
 */
@Slf4j
public class IpAddressUtils {
    public static final String UNKNOWN = "unknown";

    private static final String[] IP_HEADER_NAMES = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "CF-Connecting-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR",
    };

    /**
     * Gets the IP address from request.
     *
     * @param request is server http request
     * @return IP address if found, otherwise {@link #UNKNOWN}.
     */
    public static String getClientIp(ServerHttpRequest request) {
        for (String header : IP_HEADER_NAMES) {
            String ipList = request.getHeaders().getFirst(header);
            if (StringUtils.hasText(ipList) && !UNKNOWN.equalsIgnoreCase(ipList)) {
                String[] ips = ipList.trim().split("[,;]");
                for (String ip : ips) {
                    if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                        return ip;
                    }
                }
            }
        }
        var remoteAddress = request.getRemoteAddress();
        return remoteAddress == null || remoteAddress.isUnresolved()
            ? UNKNOWN : remoteAddress.getAddress().getHostAddress();
    }


    /**
     * Gets the ip address from request.
     *
     * @param request http request
     * @return ip address if found, otherwise {@link #UNKNOWN}.
     */
    public static String getIpAddress(ServerRequest request) {
        try {
            return getClientIp(request.exchange().getRequest());
        } catch (Exception e) {
            log.warn("Failed to obtain client IP, and fallback to unknown.", e);
            return UNKNOWN;
        }
    }

}
