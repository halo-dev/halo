package run.halo.app.theme.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeResolver;

/**
 * Tests for {@link ThemeMessageResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureWebTestClient
public class ThemeMessageResolverIntegrationTest {

    @MockitoSpyBean
    private ThemeResolver themeResolver;

    private URL defaultThemeUrl;

    private URL otherThemeUrl;

    @MockitoSpyBean
    private InitializationStateGetter initializationStateGetter;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() throws FileNotFoundException, URISyntaxException {
        when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(true));
        defaultThemeUrl = ResourceUtils.getURL("classpath:themes/default");
        otherThemeUrl = ResourceUtils.getURL("classpath:themes/other");

        when(themeResolver.getTheme(any(ServerWebExchange.class)))
            .thenReturn(Mono.just(createDefaultContext()));
    }

    @Test
    void messageResolverWhenDefaultTheme() {
        webTestClient.get()
            .uri("/?language=zh")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .xpath("/html/body/div[1]").isEqualTo("zh")
            .xpath("/html/body/div[2]").isEqualTo("欢迎来到首页");
    }

    @Test
    void messageResolverForEnLanguageWhenDefaultTheme() {
        webTestClient.get()
            .uri("/?language=en")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .xpath("/html/body/div[1]").isEqualTo("en")
            .xpath("/html/body/div[2]").isEqualTo("Welcome to the index");
    }

    @Test
    void shouldUseDefaultWhenLanguageNotSupport() {
        webTestClient.get()
            .uri("/index?language=foo")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            // make sure the "templates/index.properties" file is precedence over the
            // "i18n/default.properties".
            .xpath("/html/head/title").isEqualTo("Title from index.properties")
            .xpath("/html/body/div[1]").isEqualTo("foo")
            .xpath("/html/body/div[2]").isEqualTo("欢迎来到首页");
    }

    @Test
    void switchTheme() throws URISyntaxException {
        webTestClient.get()
            .uri("/index?language=zh")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .xpath("/html/head/title").isEqualTo("来自 index_zh.properties 的标题")
            .xpath("/html/body/div[1]").isEqualTo("zh")
            .xpath("/html/body/div[2]").isEqualTo("欢迎来到首页")
        ;

        // For other theme
        when(themeResolver.getTheme(any(ServerWebExchange.class)))
            .thenReturn(Mono.just(createOtherContext()));
        webTestClient.get()
            .uri("/index?language=zh")
            .exchange()
            .expectBody()
            .xpath("/html/head/title").isEqualTo("Other theme title")
            .xpath("/html/body/p").isEqualTo("Other 首页");

        webTestClient.get()
            .uri("/index?language=en")
            .exchange()
            .expectBody()
            .xpath("/html/head/title").isEqualTo("Other theme title")
            .xpath("/html/body/p").isEqualTo("other index");
    }

    ThemeContext createDefaultContext() throws URISyntaxException {
        return ThemeContext.builder()
            .name("default")
            .path(Path.of(defaultThemeUrl.toURI()))
            .active(true)
            .build();
    }

    ThemeContext createOtherContext() throws URISyntaxException {
        return ThemeContext.builder()
            .name("other")
            .path(Path.of(otherThemeUrl.toURI()))
            .active(false)
            .build();
    }

    @TestConfiguration
    static class MessageResolverConfig {
        @Bean
        RouterFunction<ServerResponse> routeTestIndex() {
            return RouterFunctions
                .route(RequestPredicates.GET("/").or(RequestPredicates.GET("/index"))
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    request -> ServerResponse.ok().render("index"));
        }
    }
}
