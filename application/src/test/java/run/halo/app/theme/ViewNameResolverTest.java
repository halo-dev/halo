package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link DefaultViewNameResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(SpringExtension.class)
class ViewNameResolverTest {

    @Mock
    ThemeResolver themeResolver;

    @Mock
    ThymeleafProperties thymeleafProperties;

    @InjectMocks
    DefaultViewNameResolver viewNameResolver;

    @TempDir
    Path themePath;

    @BeforeEach
    void setUp() throws IOException {
        when(thymeleafProperties.getSuffix()).thenReturn(ThymeleafProperties.DEFAULT_SUFFIX);
    }

    @Test
    void resolveViewNameOrDefault() throws URISyntaxException, IOException {
        var templatesPath = themePath.resolve("templates");
        if (!Files.exists(templatesPath)) {
            Files.createDirectory(templatesPath);
        }
        Files.createFile(templatesPath.resolve("post_news.html"));
        Files.createFile(templatesPath.resolve("post_docs.html"));


        var exchange = Mockito.mock(ServerWebExchange.class);
        when(themeResolver.getTheme(exchange))
            .thenReturn(Mono.fromSupplier(() -> ThemeContext.builder()
                .name("fake-theme")
                .path(themePath)
                .active(true)
                .build())
            );

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
            .expectNext(viewName)
            .verifyComplete();

        viewNameResolver.resolveViewNameOrDefault(request, "post_nothing", "post")
            .as(StepVerifier::create)
            .expectNext("post")
            .verifyComplete();
    }

    @Test
    void processName() {
        var suffix = thymeleafProperties.getSuffix();
        assertThat(viewNameResolver.computeResourceName("post_news"))
            .isEqualTo("post_news" + suffix);
        assertThat(
            viewNameResolver.computeResourceName("post_news" + suffix))
            .isEqualTo("post_news" + suffix);
        assertThat(viewNameResolver.computeResourceName("post_news.test"))
            .isEqualTo("post_news.test" + suffix);

        assertThatThrownBy(() -> viewNameResolver.computeResourceName(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Name must not be null");
    }
}
