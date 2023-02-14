package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.theme.HaloViewResolver;

/**
 * Tests for {@link ViewNameResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(SpringExtension.class)
class ViewNameResolverTest {

    @Mock
    private HaloViewResolver haloViewResolver;

    @Mock
    private ThymeleafProperties thymeleafProperties;

    @InjectMocks
    private ViewNameResolver viewNameResolver;

    @BeforeEach
    void setUp() {
        when(thymeleafProperties.getSuffix()).thenReturn(ThymeleafProperties.DEFAULT_SUFFIX);

        when(haloViewResolver.resolveViewName(eq("post_news"), any()))
            .thenReturn(Mono.just(Mockito.mock(View.class)));
        when(haloViewResolver.resolveViewName(eq("post_docs"), any()))
            .thenReturn(Mono.just(new EmptyView()));

        when(haloViewResolver.resolveViewName(eq("post_nothing"), any()))
            .thenReturn(Mono.empty());
    }

    @Test
    void resolveViewNameOrDefault() throws URISyntaxException {
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        MockServerRequest request = MockServerRequest.builder()
            .uri(new URI("/")).method(HttpMethod.GET)
            .exchange(exchange)
            .build();

        viewNameResolver.resolveViewNameOrDefault(request, "post_news", "post")
            .as(StepVerifier::create)
            .expectNext("post_news")
            .verifyComplete();

        // post_docs.html
        String viewName = "post_docs" + thymeleafProperties.getSuffix();
        viewNameResolver.resolveViewNameOrDefault(request, viewName, "post")
            .as(StepVerifier::create)
            .expectNext("post_docs")
            .verifyComplete();

        viewNameResolver.resolveViewNameOrDefault(request, "post_nothing", "post")
            .as(StepVerifier::create)
            .expectNext("post")
            .verifyComplete();
    }

    @Test
    void processName() {
        assertThat(viewNameResolver.processName("post_news")).isEqualTo("post_news");
        assertThat(viewNameResolver.processName("post_news" + thymeleafProperties.getSuffix()))
            .isEqualTo("post_news");
        assertThat(viewNameResolver.processName("post_news.test"))
            .isEqualTo("post_news.test");
        assertThat(viewNameResolver.processName(null)).isNull();
    }
}