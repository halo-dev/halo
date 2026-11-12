package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.AccessDeniedException;

class ThemeResolverPathTraversalTest {

    @TempDir
    Path tempDir;

    ThemeResolver themeResolver;

    Path themeRoot;

    @BeforeEach
    void setUp() throws Exception {
        themeRoot = tempDir.resolve("themes");
        Files.createDirectories(themeRoot.resolve("default-theme"));
        ThemeRootGetter themeRootGetter = () -> themeRoot;
        themeResolver = new ThemeResolver(
                mock(SystemConfigFetcher.class),
                themeRootGetter,
                mock(RoleService.class),
                mock(ReactiveExtensionClient.class));
    }

    @Test
    void resolveThemePathAllowsThemeUnderRoot() {
        var path = themeResolver.resolveThemePath("default-theme");
        assertThat(path).isEqualByComparingTo(themeRoot.resolve("default-theme").normalize());
    }

    @Test
    void resolveThemePathRejectsTraversal() {
        assertThatThrownBy(() -> themeResolver.resolveThemePath("../outside"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
