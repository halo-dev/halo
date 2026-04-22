package run.halo.app.infra.utils;

import io.netty.channel.ChannelOption;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
public enum HttpSecurityUtils {
    ;

    /**
     * Create a secure HttpClient that blocks connections to private, local, and special-use IP
     * addresses to prevent SSRF attacks.
     *
     * @return a configured HttpClient instance with security measures in place
     */
    public static HttpClient secureHttpClient() {
        return HttpClient.create()
            .followRedirect(false)
            .responseTimeout(Duration.ofSeconds(10))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .resolvedAddressesSelector((config, resolvedAddresses) -> resolvedAddresses.stream()
                .filter(address -> address instanceof InetSocketAddress)
                .map(address -> (InetSocketAddress) address)
                .filter(inetSocketAddress -> !isBlocked(inetSocketAddress.getAddress()))
                .toList()
            );
    }

    public static ExchangeFilterFunction maxResponseSizeFilter(long maxByteCount) {
        return (request, next) -> next.exchange(request)
            .map(response -> response.mutate()
                .body(body -> {
                    var countDown = new AtomicLong(maxByteCount);
                    return body.delayUntil(dataBuffer -> {
                        long remainder = countDown.addAndGet(-dataBuffer.readableByteCount());
                        if (remainder < 0) {
                            // No need to release the data buffer here as it will be released
                            // after the error is propagated and handled by the framework.
                            return Mono.error(new DataBufferLimitException(
                                "Response body exceeds the maximum allowed size of "
                                    + maxByteCount + " bytes"
                            ));
                        }
                        return Mono.empty();
                    });
                })
                .build());
    }

    static boolean isBlocked(InetAddress address) {
        return address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()
            || address.isMulticastAddress()
            || isSpecialUseIpv4(address)
            || isUniqueLocalIpv6(address);
    }

    static boolean isSpecialUseIpv4(InetAddress address) {
        if (!(address instanceof Inet4Address inet4Address)) {
            return false;
        }
        byte[] bytes = inet4Address.getAddress();
        int first = Byte.toUnsignedInt(bytes[0]);
        int second = Byte.toUnsignedInt(bytes[1]);

        // Used for "self-identification" during boot-up or to represent the default route.
        return first == 0
            // Shared Address Space for Carrier-Grade NAT (CGN), often used by ISPs to share one
            // public IP among many subscribers.
            || (first == 100 && second >= 64 && second <= 127)
            // Benchmarking and performance testing between subnets.
            || (first == 198 && (second == 18 || second == 19))
            // Reserved (formerly Class E) for future or experimental use and should not be used
            // in typical configurations.
            || first >= 240;
    }

    static boolean isUniqueLocalIpv6(InetAddress address) {
        if (!(address instanceof Inet6Address inet6Address)) {
            return false;
        }
        return (inet6Address.getAddress()[0] & 0xfe) == 0xfc;
    }

}
