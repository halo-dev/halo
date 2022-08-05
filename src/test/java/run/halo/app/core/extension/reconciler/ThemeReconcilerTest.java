package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.theme.ThemePathPolicy;

/**
 * Tests for {@link ThemeReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ThemeReconcilerTest {

    @Mock
    private ExtensionClient extensionClient;

    @Mock
    private HaloProperties haloProperties;

    private File defaultTheme;

    private Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("halo-theme-");
        defaultTheme = ResourceUtils.getFile("classpath:themes/default");
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tempDirectory);
    }

    @Test
    void reconcileDelete() throws IOException {
        Path testWorkDir = tempDirectory.resolve("reconcile-delete");
        Files.createDirectory(testWorkDir);
        when(haloProperties.getWorkDir()).thenReturn(testWorkDir);

        ThemeReconciler themeReconciler = new ThemeReconciler(extensionClient, haloProperties);
        ThemePathPolicy themePathPolicy = new ThemePathPolicy(testWorkDir);

        Theme theme = new Theme();
        Metadata metadata = new Metadata();
        metadata.setName("theme-test");
        metadata.setDeletionTimestamp(Instant.now());
        theme.setMetadata(metadata);
        theme.setKind(Theme.KIND);
        theme.setApiVersion("theme.halo.run/v1alpha1");
        Theme.ThemeSpec themeSpec = new Theme.ThemeSpec();
        themeSpec.setSettingName("theme-test-setting");
        theme.setSpec(themeSpec);

        Path defaultThemePath = themePathPolicy.generate(theme);

        // copy to temp directory
        FileSystemUtils.copyRecursively(defaultTheme.toPath(), defaultThemePath);

        assertThat(testWorkDir).isNotEmptyDirectory();
        assertThat(defaultThemePath).exists();

        when(extensionClient.fetch(eq(Theme.class), eq(metadata.getName())))
            .thenReturn(Optional.of(theme));
        when(extensionClient.fetch(Setting.class, themeSpec.getSettingName()))
            .thenReturn(Optional.empty());

        themeReconciler.reconcile(new Reconciler.Request(metadata.getName()));

        verify(extensionClient, times(1)).fetch(eq(Theme.class), eq(metadata.getName()));
        verify(extensionClient, times(1)).fetch(eq(Setting.class), eq(themeSpec.getSettingName()));

        assertThat(Files.exists(testWorkDir)).isTrue();
        assertThat(Files.exists(defaultThemePath)).isFalse();
    }
}