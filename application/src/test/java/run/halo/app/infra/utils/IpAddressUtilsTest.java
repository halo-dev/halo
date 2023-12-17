package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

class IpAddressUtilsTest {

    @Test
    void testGetIPAddressFromCloudflareProxy() {
        var request = MockServerHttpRequest.get("/")
            .header("CF-Connecting-IP", "127.0.0.1")
            .build();
        var expected = "127.0.0.1";
        var actual = IpAddressUtils.getClientIp(request);
        assertEquals(expected, actual);
    }

    @Test
    void testGetIPAddressFromXRealIpHeader() {
        var request = MockServerHttpRequest.get("/")
            .header("X-Real-IP", "127.0.0.1")
            .build();
        var expected = "127.0.0.1";
        var actual = IpAddressUtils.getClientIp(request);
        assertEquals(expected, actual);
    }

    @Test
    void testGetUnknownIPAddressWhenRemoteAddressIsNull() {
        var request = MockServerHttpRequest.get("/").build();
        var actual = IpAddressUtils.getClientIp(request);
        assertEquals(IpAddressUtils.UNKNOWN, actual);
    }

    @Test
    void testGetUnknownIPAddressWhenRemoteAddressIsUnresolved() {
        var request = MockServerHttpRequest.get("/")
            .remoteAddress(InetSocketAddress.createUnresolved("localhost", 8090))
            .build();
        var actual = IpAddressUtils.getClientIp(request);
        assertEquals(IpAddressUtils.UNKNOWN, actual);
    }

    @Test
    void testGetIPAddressWithMultipleHeaders() {
        var headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "127.0.0.1, 127.0.1.1");
        headers.add("Proxy-Client-IP", "127.0.0.2");
        headers.add("CF-Connecting-IP", "127.0.0.2");
        headers.add("WL-Proxy-Client-IP", "127.0.0.3");
        headers.add("HTTP_CLIENT_IP", "127.0.0.4");
        headers.add("HTTP_X_FORWARDED_FOR", "127.0.0.5");
        var request = MockServerHttpRequest.get("/")
            .headers(headers)
            .build();
        var expected = "127.0.0.1";
        var actual = IpAddressUtils.getClientIp(request);
        assertEquals(expected, actual);
    }
}