package run.halo.app.theme.message;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.BiFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.util.context.Context;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeReactiveContextFilter;

/**
 * Tests for {@link ThemeMessageResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThemeMessageResolverIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ThemeReactiveContextFilter themeReactiveContextFilter;

    private BiFunction<ServerWebExchange, Context, Context> defaultThemeContextFunction;
    private URL defaultThemeUrl;
    private URL otherThemeUrl;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        defaultThemeContextFunction = themeReactiveContextFilter.getThemeContextFunction();
        webTestClient = WebTestClient
            .bindToApplicationContext(applicationContext)
            .configureClient()
            .responseTimeout(Duration.ofMinutes(1))
            .build();

        defaultThemeUrl = ResourceUtils.getURL("classpath:themes/default");
        otherThemeUrl = ResourceUtils.getURL("classpath:themes/other");
    }

    @AfterEach
    void tearDown() {
        themeReactiveContextFilter.setThemeContextFunction(defaultThemeContextFunction);
    }

    @Test
    void messageResolverWhenDefaultTheme() {
        themeReactiveContextFilter.setThemeContextFunction((exchange, context) -> {
            ThemeContext defaultContext = createDefaultContext();
            return context.put(ThemeContext.THEME_CONTEXT_KEY, defaultContext);
        });

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
                <p>欢迎来到首页</p>
                </body>
                </html>
                """);
    }

    @Test
    void messageResolverForEnLanguageWhenDefaultTheme() {
        themeReactiveContextFilter.setThemeContextFunction((exchange, context) -> {
            ThemeContext defaultContext = createDefaultContext();
            return context.put(ThemeContext.THEME_CONTEXT_KEY, defaultContext);
        });
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
                <p>Welcome to the index</p>
                </body>
                </html>
                """);
    }

    @Test
    void shouldUseDefaultWhenLanguageNotSupport() {
        themeReactiveContextFilter.setThemeContextFunction((exchange, context) -> {
            ThemeContext defaultContext = createDefaultContext();
            return context.put(ThemeContext.THEME_CONTEXT_KEY, defaultContext);
        });
        webTestClient.get()
            .uri("/?language=foo")
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
                <p>欢迎来到首页</p>
                </body>
                </html>
                """);
    }

    @Test
    void switchTheme() {
        // For default theme
        themeReactiveContextFilter.setThemeContextFunction((exchange, context) -> {
            ThemeContext defaultContext = createDefaultContext();
            return context.put(ThemeContext.THEME_CONTEXT_KEY, defaultContext);
        });
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
                <p>欢迎来到首页</p>
                </body>
                </html>
                """);

        // For other theme
        themeReactiveContextFilter.setThemeContextFunction((exchange, context) -> {
            ThemeContext otherContext = createOtherContext();
            return context.put(ThemeContext.THEME_CONTEXT_KEY, otherContext);
        });
        webTestClient.get()
            .uri("/?language=zh")
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
            .uri("/?language=en")
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

    ThemeContext createDefaultContext() {
        try {
            return ThemeContext.builder()
                .themeName("default")
                .path(Paths.get(defaultThemeUrl.toURI()))
                .isActive(true)
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    ThemeContext createOtherContext() {
        try {
            return ThemeContext.builder()
                .themeName("other")
                .path(Paths.get(otherThemeUrl.toURI()))
                .isActive(false)
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
