package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import run.halo.app.theme.router.ModelConst;

class HaloUtilsTest {

    @Test
    void checkNoCache() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        var request = MockServerRequest.builder()
            .exchange(exchange)
            .build();
        var applied = HaloUtils.noCache().apply(request);
        assertEquals(applied, request);
        assertTrue(() -> exchange.getRequiredAttribute(ModelConst.NO_CACHE));
    }

    @ParameterizedTest
    @CsvSource({
        "http://example.com/path with spaces, http://example.com/path%20with%20spaces",
        "https://example.com/üñîçødé, https://example.com/%C3%BC%C3%B1%C3%AE%C3%A7%C3%B8d%C3%A9",
        "ftp://example.com/special?param=äöü, ftp://example.com/special?param=%C3%A4%C3%B6%C3%BC",
        "http://example.com/normal-path, http://example.com/normal-path",
        "http://example.com/路径, http://example.com/%E8%B7%AF%E5%BE%84",
        "http://example.com/space%20space, http://example.com/space%20space",
        "http://example.com/100%100, http://example.com/100%100"
    })
    void shouldConvertUriSafely(String uri, String expected) {
        assertEquals(expected, HaloUtils.safeToUri(uri).toString());
    }
}