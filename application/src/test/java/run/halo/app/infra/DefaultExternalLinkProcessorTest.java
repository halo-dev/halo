package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.filter.reactive.ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

/**
 * Tests for {@link DefaultExternalLinkProcessor}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultExternalLinkProcessorTest {

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @InjectMocks
    DefaultExternalLinkProcessor externalLinkProcessor;


    @Test
    void processWhenLinkIsEmpty() {
        assertThat(externalLinkProcessor.processLink((String) null)).isNull();
        assertThat(externalLinkProcessor.processLink("")).isEmpty();
    }

    @Test
    void process() throws MalformedURLException {
        when(externalUrlSupplier.getRaw()).thenReturn(null);
        assertThat(externalLinkProcessor.processLink("/test")).isEqualTo("/test");

        when(externalUrlSupplier.getRaw()).thenReturn(URI.create("https://halo.run").toURL());
        assertThat(externalLinkProcessor.processLink("/test")).isEqualTo("https://halo.run/test");

        assertThat(externalLinkProcessor.processLink("https://guqing.xyz/test"))
            .isEqualTo("https://guqing.xyz/test");

        when(externalUrlSupplier.getRaw()).thenReturn(URI.create("https://halo.run/").toURL());
        assertThat(externalLinkProcessor.processLink("/test")).isEqualTo("https://halo.run/test");
        assertThat(externalLinkProcessor.processLink("https://halo.run/test"))
            .isEqualTo("https://halo.run/test");
    }

    @ParameterizedTest
    @MethodSource("processUriTestWithoutServerWebExchangeArguments")
    void processUriWithoutServerWebExchange(String link, String expectedLink)
        throws MalformedURLException {
        lenient().when(externalUrlSupplier.getRaw())
            .thenReturn(new URL("https://www.halo.run/context-path"));
        externalLinkProcessor.processLink(URI.create(link))
            .as(StepVerifier::create)
            .expectNext(URI.create(expectedLink))
            .verifyComplete();
    }

    static Stream<Arguments> processUriTestWithoutServerWebExchangeArguments() {
        return Stream.of(
            Arguments.of("http://localhost:8090/halo", "http://localhost:8090/halo"),
            Arguments.of("/halo", "https://www.halo.run/context-path/halo"),
            Arguments.of("halo", "https://www.halo.run/context-path/halo"),
            Arguments.of("/halo?query", "https://www.halo.run/context-path/halo?query"),
            Arguments.of(
                "/halo?query#fragment", "https://www.halo.run/context-path/halo?query#fragment"
            ),
            Arguments.of("/halo/subpath", "https://www.halo.run/context-path/halo/subpath"),
            Arguments.of("/halo/中文", "https://www.halo.run/context-path/halo/%E4%B8%AD%E6%96%87"),
            Arguments.of("/halo/ooo%2Fooo", "https://www.halo.run/context-path/halo/ooo%2Fooo")
        );
    }

    @ParameterizedTest
    @MethodSource("processUriTestWithServerWebExchangeArguments")
    void processUriWithServerWebExchange(String link, String expectLink)
        throws MalformedURLException {
        lenient().when(externalUrlSupplier.getRaw())
            .thenReturn(URI.create("https://www.halo.run").toURL());
        var request = mock(ServerHttpRequest.class);
        var exchange = mock(ServerWebExchange.class);
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(externalUrlSupplier.getURL(request)).thenReturn(
            new URL("https://antoher.halo.run/context-path"));
        externalLinkProcessor.processLink(URI.create(link))
            .contextWrite(context -> context.put(EXCHANGE_CONTEXT_ATTRIBUTE, exchange))
            .as(StepVerifier::create)
            .expectNext(URI.create(expectLink))
            .verifyComplete();
    }

    static Stream<Arguments> processUriTestWithServerWebExchangeArguments() {
        return Stream.of(
            Arguments.of("http://localhost:8090/halo?query#fragment",
                "http://localhost:8090/halo?query#fragment"),
            Arguments.of("/halo", "https://antoher.halo.run/context-path/halo"),
            Arguments.of("halo", "https://antoher.halo.run/context-path/halo"),
            Arguments.of("/halo?query", "https://antoher.halo.run/context-path/halo?query"),
            Arguments.of("/halo?query#fragment",
                "https://antoher.halo.run/context-path/halo?query#fragment"),
            Arguments.of("/halo/subpath", "https://antoher.halo.run/context-path/halo/subpath"),
            Arguments.of("/halo/中文",
                "https://antoher.halo.run/context-path/halo/%E4%B8%AD%E6%96%87"),
            Arguments.of("/halo/ooo%2Fooo", "https://antoher.halo.run/context-path/halo/ooo%2Fooo")
        );
    }

}
