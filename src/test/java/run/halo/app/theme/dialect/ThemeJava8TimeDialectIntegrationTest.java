package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static run.halo.app.theme.ThemeLocaleContextResolver.TIME_ZONE_COOKIE_NAME;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.extras.java8time.expression.Temporals;
import reactor.core.publisher.Mono;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeResolver;

/**
 * Tests for {@link ThemeJava8TimeDialect}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ThemeJava8TimeDialectIntegrationTest {
    private static final Instant INSTANT = Instant.now();

    // @Autowired
    @SpyBean
    private ThemeResolver themeResolver;

    private URL defaultThemeUrl;

    @Autowired
    private WebTestClient webTestClient;

    private TimeZone defaultTimeZone;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        defaultThemeUrl = ResourceUtils.getURL("classpath:themes/default");
        when(themeResolver.getTheme(any(ServerHttpRequest.class)))
            .thenReturn(Mono.just(createDefaultContext()));
        defaultTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    void temporalsInAmerica() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        TimeZone.setDefault(timeZone);

        assertTemporals(timeZone);
    }

    @Test
    void temporalsInChina() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(timeZone);

        assertTemporals(timeZone);
    }


    @Test
    void timeZoneFromCookie() {
        TimeZone timeZone = TimeZone.getTimeZone("Africa/Accra");
        String formatTime = timeZoneTemporalFormat(timeZone.toZoneId());

        webTestClient.get()
            .uri("/timezone?language=zh")
            .cookie(TIME_ZONE_COOKIE_NAME, timeZone.toZoneId().toString())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo(String.format("<p>%s</p>\n", formatTime));
    }

    @Test
    void invalidTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("invalid/timezone");
        //the GMT zone if the given ID cannot be understood.
        assertThat(timeZone.toZoneId().toString()).isEqualTo("GMT");
    }

    private void assertTemporals(TimeZone timeZone) {
        String formatTime = timeZoneTemporalFormat(timeZone.toZoneId());
        webTestClient.get()
            .uri("/timezone?language=zh")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo(String.format("<p>%s</p>\n", formatTime));
    }

    private String timeZoneTemporalFormat(ZoneId zoneId) {
        Temporals temporals = new Temporals(Locale.CHINESE, zoneId);
        return temporals.format(INSTANT, "yyyy-MM-dd HH:mm:ss");
    }

    ThemeContext createDefaultContext() {
        return ThemeContext.builder()
            .name("default")
            .path(Paths.get(defaultThemeUrl.getPath()))
            .active(true)
            .build();
    }

    @TestConfiguration
    static class MessageResolverConfig {

        @Bean
        RouterFunction<ServerResponse> routeTestIndex() {
            return RouterFunctions
                .route(RequestPredicates.GET("/timezone")
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    request -> ServerResponse.ok().render("timezone", Map.of("instants", INSTANT)));
        }
    }
}