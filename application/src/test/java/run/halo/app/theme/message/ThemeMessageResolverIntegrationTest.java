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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SetupStateCache;
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

    @SpyBean
    private ThemeResolver themeResolver;

    private URL defaultThemeUrl;

    private URL otherThemeUrl;

    @SpyBean
    private SetupStateCache setupStateCache;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() throws FileNotFoundException, URISyntaxException {
        when(setupStateCache.get()).thenReturn(true);
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
            .expectBody(String.class)
            .isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Title</title>
                </head>
                <body>
                index
                <div>zh</div>
                <div>欢迎来到首页</div>
                </body>
                </html>
                """);
    }

    @Test
    void messageResolverForEnLanguageWhenDefaultTheme() {
        webTestClient.get()
            .uri("/?language=en")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Title</title>
                </head>
                <body>
                index
                <div>en</div>
                <div>Welcome to the index</div>
                </body>
                </html>
                """);
    }

    @Test
    void shouldUseDefaultWhenLanguageNotSupport() {
        webTestClient.get()
            .uri("/index?language=foo")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Title</title>
                </head>
                <body>
                index
                <div>foo</div>
                <div>欢迎来到首页</div>
                </body>
                </html>
                """);
    }

    @Test
    void switchTheme() throws URISyntaxException {
        webTestClient.get()
            .uri("/index?language=zh")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Title</title>
                </head>
                <body>
                index
                <div>zh</div>
                <div>欢迎来到首页</div>
                </body>
                </html>
                """);

        // For other theme
        when(themeResolver.getTheme(any(ServerWebExchange.class)))
            .thenReturn(Mono.just(createOtherContext()));
        webTestClient.get()
            .uri("/index?language=zh")
            .exchange()
            .expectBody(String.class)
            .isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Other theme title</title>
                </head>
                <body>
                <p>Other 首页</p>
                </body>
                </html>
                """);
        webTestClient.get()
            .uri("/index?language=en")
            .exchange()
            .expectBody(String.class)
            .isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Other theme title</title>
                </head>
                <body>
                <p>other index</p>
                </body>
                </html>
                """);
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
