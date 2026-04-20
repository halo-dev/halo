package run.halo.app.infra;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;
import org.springframework.util.StringUtils;

final class PublicRemoteUriValidator {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    void validate(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Remote URL must not be null.");
        }
        var scheme = uri.getScheme();
        if (!StringUtils.hasText(scheme)
            || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only HTTP(S) remote URLs are allowed.");
        }
        if (StringUtils.hasText(uri.getUserInfo())) {
            throw new IllegalArgumentException("Remote URLs with user info are not allowed.");
        }
        var host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            throw new IllegalArgumentException("Remote URL host must not be blank.");
        }
        final InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to resolve remote URL host.", e);
        }
        for (InetAddress address : addresses) {
            if (isBlocked(address)) {
                throw new IllegalArgumentException("Remote URL host resolves to a non-public "
                    + "address.");
            }
        }
    }

    private boolean isBlocked(InetAddress address) {
        return address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()
            || address.isMulticastAddress()
            || isUniqueLocalIpv6(address);
    }

    private boolean isUniqueLocalIpv6(InetAddress address) {
        if (!(address instanceof Inet6Address inet6Address)) {
            return false;
        }
        return (inet6Address.getAddress()[0] & 0xfe) == 0xfc;
    }
}
