package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link DefaultViewNameResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
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
    void setUp() {
        lenient().when(thymeleafProperties.getSuffix()).thenReturn(ThymeleafProperties.DEFAULT_SUFFIX);
    }

    @Test
    void resolveViewNameOrDefault() throws URISyntaxException, IOException {
        var request = requestWithTheme();
        var templatesPath = createTemplatesDirectory();
        Files.createFile(templatesPath.resolve("post_news.html"));
        Files.createFile(templatesPath.resolve("post_docs.html"));

        viewNameResolver
                .resolveViewNameOrDefault(request, "post_news", "post")
                .as(StepVerifier::create)
                .expectNext("post_news")
                .verifyComplete();

        // post_docs.html
        String viewName = "post_docs" + thymeleafProperties.getSuffix();
        viewNameResolver
                .resolveViewNameOrDefault(request, viewName, "post")
                .as(StepVerifier::create)
                .expectNext(viewName)
                .verifyComplete();

        viewNameResolver
                .resolveViewNameOrDefault(request, "post_nothing", "post")
                .as(StepVerifier::create)
                .expectNext("post")
                .verifyComplete();
    }

    @Test
    void shouldResolveNestedCustomTemplate() throws URISyntaxException, IOException {
        var request = requestWithTheme();
        var customDir = Files.createDirectories(createTemplatesDirectory().resolve("custom"));
        Files.createFile(customDir.resolve("news.html"));

        viewNameResolver
                .resolveViewNameOrDefault(request, "custom/news", "post")
                .as(StepVerifier::create)
                .expectNext("custom/news")
                .verifyComplete();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldFallbackWhenViewNameIsBlank(String viewName) throws URISyntaxException {
        viewNameResolver
                .resolveViewNameOrDefault(requestWithoutTheme(), viewName, "post")
                .as(StepVerifier::create)
                .expectNext("post")
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"../secret", "foo/../secret", "..\\secret", "/absolute", "foo\\bar", "post::fragment"})
    void shouldFallbackWhenViewNameIsUnsafe(String viewName) throws URISyntaxException {
        viewNameResolver
                .resolveViewNameOrDefault(requestWithoutTheme(), viewName, "post")
                .as(StepVerifier::create)
                .expectNext("post")
                .verifyComplete();
    }

    @Test
    void shouldFallbackWhenNormalizedPathEscapesTemplates() throws IOException {
        var templatesPath = createTemplatesDirectory();
        Files.createFile(templatesPath.resolve("post_news.html"));
        Files.createFile(themePath.resolve("secret.html"));
        var themeContext = ThemeContext.builder()
                .name("fake-theme")
                .path(themePath)
                .active(true)
                .build();

        assertThat(viewNameResolver.resolveContainedViewName(themeContext, "../secret", "post"))
                .isEqualTo("post");
        assertThat(viewNameResolver.resolveContainedViewName(themeContext, "post_news", "post"))
                .isEqualTo("post_news");
    }

    @Test
    void processName() {
        var suffix = thymeleafProperties.getSuffix();
        assertThat(viewNameResolver.computeResourceName("post_news")).isEqualTo("post_news" + suffix);
        assertThat(viewNameResolver.computeResourceName("post_news" + suffix)).isEqualTo("post_news" + suffix);
        assertThat(viewNameResolver.computeResourceName("post_news.test")).isEqualTo("post_news.test" + suffix);

        assertThatThrownBy(() -> viewNameResolver.computeResourceName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must not be null");
    }

    @Test
    void shouldRejectUnsafeViewNames() {
        assertThat(DefaultViewNameResolver.isSafeViewName("post_news")).isTrue();
        assertThat(DefaultViewNameResolver.isSafeViewName("custom/news")).isTrue();
        assertThat(DefaultViewNameResolver.isSafeViewName("../secret")).isFalse();
        assertThat(DefaultViewNameResolver.isSafeViewName("foo/../secret")).isFalse();
        assertThat(DefaultViewNameResolver.isSafeViewName("/absolute")).isFalse();
        assertThat(DefaultViewNameResolver.isSafeViewName("foo\\bar")).isFalse();
        assertThat(DefaultViewNameResolver.isSafeViewName("post::fragment")).isFalse();
    }

    private Path createTemplatesDirectory() throws IOException {
        var templatesPath = themePath.resolve("templates");
        if (!Files.exists(templatesPath)) {
            Files.createDirectory(templatesPath);
        }
        return templatesPath;
    }

    private ServerRequest requestWithTheme() throws URISyntaxException {
        var request = requestWithoutTheme();
        when(themeResolver.getTheme(request.exchange()))
                .thenReturn(Mono.fromSupplier(() -> ThemeContext.builder()
                        .name("fake-theme")
                        .path(themePath)
                        .active(true)
                        .build()));
        return request;
    }

    private ServerRequest requestWithoutTheme() throws URISyntaxException {
        return MockServerRequest.builder()
                .uri(new URI("/"))
                .method(HttpMethod.GET)
                .exchange(Mockito.mock(ServerWebExchange.class))
                .build();
    }
}
