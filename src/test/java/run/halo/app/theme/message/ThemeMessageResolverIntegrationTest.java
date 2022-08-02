package run.halo.app.theme.message;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeResolver;

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
    private ThemeResolver themeResolver;
    private URL defaultThemeUrl;
    private URL otherThemeUrl;

    Function<ServerHttpRequest, ThemeContext> themeContextFunction;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        themeContextFunction = themeResolver.getThemeContextFunction();
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
        this.themeResolver.setThemeContextFunction(themeContextFunction);
    }

    @Test
    void messageResolverWhenDefaultTheme() {
        themeResolver.setThemeContextFunction(request -> createDefaultContext());
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
        themeResolver.setThemeContextFunction(request -> createDefaultContext());
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
        themeResolver.setThemeContextFunction(request -> createDefaultContext());
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
    void switchTheme() {
        themeResolver.setThemeContextFunction(request -> createDefaultContext());
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
        themeResolver.setThemeContextFunction(request -> createOtherContext());
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

    ThemeContext createDefaultContext() {
        return ThemeContext.builder()
            .name("default")
            .path(Paths.get(defaultThemeUrl.getPath()))
            .active(true)
            .build();
    }

    ThemeContext createOtherContext() {
        return ThemeContext.builder()
            .name("other")
            .path(Paths.get(otherThemeUrl.getPath()))
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
