package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Tests for {@link ThemeContextFilter}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class ThemeContextFilterTests {

    @Mock
    private SystemConfigurableEnvironmentFetcher systemEnvironmentFetcher;
    @Mock
    private HaloProperties haloProperties;

    private ThemeContextFilter themeContextFilter;

    @BeforeEach
    void setUp() {
        when(haloProperties.getWorkDir()).thenReturn(Paths.get("/tmp"));
        themeContextFilter = new ThemeContextFilter(systemEnvironmentFetcher, haloProperties);
    }

    @Test
    void defaultThemeContextFilter() {
        SystemSetting.Theme theme = new SystemSetting.Theme();
        theme.setActive("default");
        when(systemEnvironmentFetcher.get(eq(SystemSetting.THEME))).thenReturn(theme);

        assertThatThrownBy(ThemeContextHolder::getThemeContext).isInstanceOf(
            IllegalStateException.class);

        WebTestClient client = WebTestClient
            .bindToWebHandler(exchange -> {
                ThemeContext themeContext = ThemeContextHolder.getThemeContext();
                assertThat(themeContext).isNotNull();
                assertThat(themeContext.getThemeName()).isEqualTo("default");
                assertThat(themeContext.getPath()).isEqualTo(Paths.get("/tmp/themes/default"));
                assertThat(themeContext.isActive()).isTrue();
                return Mono.empty();
            })
            .webFilter(themeContextFilter)
            .build();
        verify(haloProperties, times(1)).getWorkDir();

        client.get().uri("/")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void otherThemeContextFilter() {
        assertThatThrownBy(ThemeContextHolder::getThemeContext).isInstanceOf(
            IllegalStateException.class);

        WebTestClient client = WebTestClient
            .bindToWebHandler(exchange -> {
                ThemeContext themeContext = ThemeContextHolder.getThemeContext();
                assertThat(themeContext).isNotNull();
                assertThat(themeContext.getThemeName()).isEqualTo("other");
                assertThat(themeContext.getPath()).isEqualTo(Paths.get("/tmp/themes/other"));
                assertThat(themeContext.isActive()).isFalse();
                return Mono.empty();
            })
            .webFilter(themeContextFilter)
            .build();

        client.get().uri("/?theme=other")
            .exchange()
            .expectStatus().isOk();
    }
}
