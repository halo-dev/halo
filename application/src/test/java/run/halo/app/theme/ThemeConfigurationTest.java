package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ThemeConfigurationTest {
    @Mock
    private ThemeRootGetter themeRootGetter;

    @InjectMocks
    private ThemeConfiguration themeConfiguration;

    private final Path themeRoot = Paths.get("/tmp/.halo/themes");

    @BeforeEach
    void setUp() {
        when(themeRootGetter.get()).thenReturn(themeRoot);
    }

    @Test
    void themeAssets() {
        Path path = themeConfiguration.getThemeAssetsPath("fake-theme", "hello.jpg");
        assertThat(path).isEqualTo(themeRoot.resolve("fake-theme/templates/assets/hello.jpg"));

        path = themeConfiguration.getThemeAssetsPath("fake-theme", "./hello.jpg");
        assertThat(path).isEqualTo(themeRoot.resolve("fake-theme/templates/assets/./hello.jpg"));

        assertThatThrownBy(() -> {
            themeConfiguration.getThemeAssetsPath("fake-theme", "../../hello.jpg");
        }).isInstanceOf(AccessDeniedException.class)
            .hasMessage(
                "403 FORBIDDEN \"Directory traversal detected: /tmp/"
                    + ".halo/themes/fake-theme/templates/assets/../../hello.jpg\"");

        path = themeConfiguration.getThemeAssetsPath("fake-theme", "%2e%2e/f.jpg");
        assertThat(path).isEqualTo(themeRoot.resolve("fake-theme/templates/assets/%2e%2e/f.jpg"));

        path = themeConfiguration.getThemeAssetsPath("fake-theme", "f/./../p.jpg");
        assertThat(path).isEqualTo(themeRoot.resolve("fake-theme/templates/assets/f/./../p.jpg"));

        path = themeConfiguration.getThemeAssetsPath("fake-theme", "f../p.jpg");
        assertThat(path).isEqualTo(themeRoot.resolve("fake-theme/templates/assets/f../p.jpg"));
    }
}
