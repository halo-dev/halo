package run.halo.app.infra.utils;

import io.netty.channel.ChannelOption;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientConfig;
import reactor.netty.transport.ClientTransport;

@Slf4j
public enum HttpSecurityUtils {
    ;

    /**
     * Create a secure HttpClient that blocks connections to private and local IP addresses to
     * prevent SSRF attacks.
     *
     * @return a configured HttpClient instance with security measures in place
     */
    public static HttpClient secureHttpClient() {
        return HttpClient.create()
            .followRedirect(false)
            .responseTimeout(Duration.ofSeconds(10))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .resolvedAddressesSelector(new ClientTransport.ResolvedAddressSelector<>() {
                @Override
                public @Nullable List<? extends SocketAddress> apply(
                    HttpClientConfig config,
                    List<? extends SocketAddress> resolvedAddresses) {
                    return resolvedAddresses.stream()
                        .filter(address -> address instanceof InetSocketAddress)
                        .map(address -> (InetSocketAddress) address)
                        .filter(inetSocketAddress -> !isBlocked(inetSocketAddress.getAddress()))
                        .toList();
                }
            });
    }

    private static boolean isBlocked(InetAddress address) {
        return address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()
            || address.isMulticastAddress()
            || isUniqueLocalIpv6(address);
    }

    private static boolean isUniqueLocalIpv6(InetAddress address) {
        if (!(address instanceof Inet6Address inet6Address)) {
            return false;
        }
        return (inet6Address.getAddress()[0] & 0xfe) == 0xfc;
    }

}
