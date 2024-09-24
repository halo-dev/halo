package run.halo.app.webfilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.infra.webfilter.LocaleChangeWebFilter;

class LocaleChangeWebFilterTest {

    LocaleChangeWebFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LocaleChangeWebFilter();
    }

    @Test
    void shouldRespondLanguageCookie() {
        WebFilterChain webFilterChain = filterExchange -> {
            var languageCookie = filterExchange.getResponse().getCookies().getFirst("language");
            assertNotNull(languageCookie);
            assertEquals("zh-CN", languageCookie.getValue());
            return Mono.empty();
        };
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/home")
            .accept(MediaType.TEXT_HTML)
            .queryParam("language", "zh-CN")
            .build()
        );
        this.filter.filter(exchange, webFilterChain).block();
    }

    @Test
    void shouldRespondLanguageCookieWithUndefinedLanguageTag() {
        WebFilterChain webFilterChain = filterExchange -> {
            var languageCookie = filterExchange.getResponse().getCookies().getFirst("language");
            assertNotNull(languageCookie);
            assertEquals("und", languageCookie.getValue());
            return Mono.empty();
        };
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/home")
            .accept(MediaType.TEXT_HTML)
            .queryParam("language", "invalid_language_tag")
            .build()
        );
        this.filter.filter(exchange, webFilterChain).block();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequest")
    void shouldNotRespondLanguageCookieIfRequestNotMatch(MockServerHttpRequest mockRequest) {
        WebFilterChain webFilterChain = filterExchange -> {
            var languageCookie = filterExchange.getResponse().getCookies().getFirst("language");
            assertNull(languageCookie);
            return Mono.empty();
        };
        var exchange = MockServerWebExchange.from(mockRequest);
        this.filter.filter(exchange, webFilterChain).block();
    }

    static Stream<MockServerHttpRequest> provideInvalidRequest() {
        return Stream.of(
            MockServerHttpRequest.get("/home")
                .accept(MediaType.ALL)
                .queryParam("language", "zh-CN")
                .build(),
            MockServerHttpRequest.get("/home")
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("language", "zh-CN")
                .build(),
            MockServerHttpRequest.post("/home")
                .accept(MediaType.TEXT_HTML)
                .queryParam("language", "zh-CN")
                .build(),
            MockServerHttpRequest.get("/home")
                .accept(MediaType.TEXT_HTML)
                .build()
        );
    }
}