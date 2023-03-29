package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.retry.RetryException;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;
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

    @Mock
    private SystemVersionSupplier systemVersionSupplier;

    @Mock
    private File defaultTheme;

    private Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("halo-theme-");
        defaultTheme = ResourceUtils.getFile("classpath:themes/default");
        lenient().when(systemVersionSupplier.get()).thenReturn(Version.valueOf("0.0.0"));
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

        final ThemeReconciler themeReconciler =
            new ThemeReconciler(extensionClient, haloProperties, systemVersionSupplier);
        final ThemePathPolicy themePathPolicy = new ThemePathPolicy(testWorkDir);

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

        verify(extensionClient, times(2)).fetch(eq(Theme.class), eq(metadata.getName()));
        verify(extensionClient, times(2)).fetch(eq(Setting.class), eq(themeSpec.getSettingName()));

        verify(extensionClient, times(2)).list(eq(AnnotationSetting.class), any(), any());

        assertThat(Files.exists(testWorkDir)).isTrue();
        assertThat(Files.exists(defaultThemePath)).isFalse();
    }

    @Test
    void reconcileDeleteRetry() {
        Theme theme = fakeTheme();
        final MetadataOperator metadata = theme.getMetadata();

        Path testWorkDir = tempDirectory.resolve("reconcile-delete");
        when(haloProperties.getWorkDir()).thenReturn(testWorkDir);

        final ThemeReconciler themeReconciler =
            new ThemeReconciler(extensionClient, haloProperties, systemVersionSupplier);

        final int[] retryFlags = {0, 0};
        when(extensionClient.fetch(eq(Setting.class), eq("theme-test-setting")))
            .thenAnswer((Answer<Optional<Setting>>) invocation -> {
                retryFlags[0]++;
                // retry 2 times
                if (retryFlags[0] < 3) {
                    return Optional.of(new Setting());
                }
                return Optional.empty();
            });

        when(extensionClient.list(eq(AnnotationSetting.class), any(), eq(null)))
            .thenAnswer((Answer<List<AnnotationSetting>>) invocation -> {
                retryFlags[1]++;
                // retry 2 times
                if (retryFlags[1] < 3) {
                    return List.of(new AnnotationSetting());
                }
                return List.of();
            });

        themeReconciler.reconcile(new Reconciler.Request(metadata.getName()));

        String settingName = theme.getSpec().getSettingName();
        verify(extensionClient, times(2)).fetch(eq(Theme.class), eq(metadata.getName()));
        verify(extensionClient, times(3)).fetch(eq(Setting.class), eq(settingName));
        verify(extensionClient, times(3)).list(eq(AnnotationSetting.class), any(), eq(null));
    }

    @Test
    void reconcileDeleteRetryWhenThrowException() {
        Theme theme = fakeTheme();

        Path testWorkDir = tempDirectory.resolve("reconcile-delete");
        when(haloProperties.getWorkDir()).thenReturn(testWorkDir);

        final ThemeReconciler themeReconciler =
            new ThemeReconciler(extensionClient, haloProperties, systemVersionSupplier);

        final int[] retryFlags = {0};
        when(extensionClient.fetch(eq(Setting.class), eq("theme-test-setting")))
            .thenAnswer((Answer<Optional<Setting>>) invocation -> {
                retryFlags[0]++;
                // retry 2 times
                if (retryFlags[0] < 2) {
                    return Optional.of(new Setting());
                }
                throw new RetryException("retry exception.");
            });

        String settingName = theme.getSpec().getSettingName();
        assertThatThrownBy(
            () -> themeReconciler.reconcile(new Reconciler.Request(theme.getMetadata().getName())))
            .isInstanceOf(RetryException.class)
            .hasMessage("retry exception.");

        verify(extensionClient, times(2)).fetch(eq(Setting.class), eq(settingName));
    }

    @Test
    void reconcileStatus() {
        when(systemVersionSupplier.get()).thenReturn(Version.valueOf("2.3.0"));
        Path testWorkDir = tempDirectory.resolve("reconcile-delete");
        when(haloProperties.getWorkDir()).thenReturn(testWorkDir);

        final ThemeReconciler themeReconciler =
            new ThemeReconciler(extensionClient, haloProperties, systemVersionSupplier);
        Theme theme = fakeTheme();
        theme.setStatus(null);
        theme.getSpec().setRequires(">2.3.0");
        when(extensionClient.fetch(eq(Theme.class), eq("fake-theme")))
            .thenReturn(Optional.of(theme));
        themeReconciler.reconcileStatus("fake-theme");
        ArgumentCaptor<Theme> themeUpdateCaptor = ArgumentCaptor.forClass(Theme.class);
        verify(extensionClient).update(themeUpdateCaptor.capture());
        Theme value = themeUpdateCaptor.getValue();
        assertThat(value.getStatus()).isNotNull();
        assertThat(value.getStatus().getConditions().peekFirst().getType())
            .isEqualTo(Theme.ThemePhase.FAILED.name());
        assertThat(value.getStatus().getPhase())
            .isEqualTo(Theme.ThemePhase.FAILED);

        theme.getSpec().setRequires(">=2.3.0");
        when(extensionClient.fetch(eq(Theme.class), eq("fake-theme")))
            .thenReturn(Optional.of(theme));
        themeReconciler.reconcileStatus("fake-theme");
        verify(extensionClient, times(2)).update(themeUpdateCaptor.capture());
        assertThat(themeUpdateCaptor.getValue().getStatus().getPhase())
            .isEqualTo(Theme.ThemePhase.READY);
    }

    private Theme fakeTheme() {
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
        lenient().when(extensionClient.fetch(eq(Theme.class), eq(metadata.getName())))
            .thenReturn(Optional.of(theme));
        return theme;
    }

    @Test
    void themeSettingDefaultValue() throws IOException, JSONException {
        Path testWorkDir = tempDirectory.resolve("reconcile-setting-value");
        Files.createDirectory(testWorkDir);
        when(haloProperties.getWorkDir()).thenReturn(testWorkDir);

        final ThemeReconciler themeReconciler =
            new ThemeReconciler(extensionClient, haloProperties, systemVersionSupplier);

        Theme theme = new Theme();
        Metadata metadata = new Metadata();
        metadata.setName("theme-test");
        theme.setMetadata(metadata);
        theme.setKind(Theme.KIND);
        theme.setApiVersion("theme.halo.run/v1alpha1");
        Theme.ThemeSpec themeSpec = new Theme.ThemeSpec();
        themeSpec.setSettingName(null);
        theme.setSpec(themeSpec);

        when(extensionClient.fetch(eq(Theme.class), eq(metadata.getName())))
            .thenReturn(Optional.of(theme));
        Reconciler.Result reconcile =
            themeReconciler.reconcile(new Reconciler.Request(metadata.getName()));
        assertThat(reconcile.reEnqueue()).isFalse();
        verify(extensionClient, times(3)).fetch(eq(Theme.class), eq(metadata.getName()));

        // setting exists
        themeSpec.setSettingName("theme-test-setting");
        assertThat(theme.getSpec().getConfigMapName()).isNull();
        ArgumentCaptor<Theme> captor = ArgumentCaptor.forClass(Theme.class);
        themeReconciler.reconcile(new Reconciler.Request(metadata.getName()));
        verify(extensionClient, times(6))
            .fetch(eq(Theme.class), eq(metadata.getName()));
        verify(extensionClient, times(3))
            .update(captor.capture());
        Theme value = captor.getValue();
        assertThat(value.getSpec().getConfigMapName()).isNotNull();

        // populate setting name and configMap name and configMap not exists
        themeSpec.setSettingName("theme-test-setting");
        themeSpec.setConfigMapName("theme-test-configmap");
        when(extensionClient.fetch(eq(ConfigMap.class), any()))
            .thenReturn(Optional.empty());
        when(extensionClient.fetch(eq(Setting.class), eq(themeSpec.getSettingName())))
            .thenReturn(Optional.of(getFakeSetting()));
        themeReconciler.reconcile(new Reconciler.Request(metadata.getName()));
        verify(extensionClient, times(2))
            .fetch(eq(Setting.class), eq(themeSpec.getSettingName()));
        ArgumentCaptor<ConfigMap> configMapCaptor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(extensionClient, times(1)).create(any(ConfigMap.class));
        verify(extensionClient, times(1)).create(configMapCaptor.capture());
        ConfigMap defaultValueConfigMap = configMapCaptor.getValue();
        Map<String, String> data = defaultValueConfigMap.getData();
        JSONAssert.assertEquals("""
                {
                    "sns": "{\\"email\\":\\"example@exmple.com\\"}"
                }
                """,
            JsonUtils.objectToJson(data),
            true);
    }

    private static Setting getFakeSetting() {
        String settingJson = """
            {
                "apiVersion": "v1alpha1",
                "kind": "Setting",
                "metadata": {
                    "name": "theme-default-setting"
                },
                "spec": {
                    "forms": [{
                        "formSchema": [
                            {
                                "$el": "h1",
                                "children": "Register"
                            },
                            {
                                "$formkit": "text",
                                "label": "Email",
                                "name": "email",
                                "value": "example@exmple.com"
                            },
                            {
                                "$formkit": "password",
                                "label": "Password",
                                "name": "password",
                                "validation": "required|length:5,16",
                                "value": null
                            }
                        ],
                        "group": "sns",
                        "label": "社交资料"
                    }]
                }
            }
            """;
        return JsonUtils.jsonToObject(settingJson, Setting.class);
    }
}