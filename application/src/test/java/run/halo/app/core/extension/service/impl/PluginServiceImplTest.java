package run.halo.app.core.extension.service.impl;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.zafarkhaja.semver.Version;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.exception.PluginAlreadyExistsException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.YamlPluginFinder;

@ExtendWith(MockitoExtension.class)
class PluginServiceImplTest {

    @Mock
    SystemVersionSupplier systemVersionSupplier;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    PluginProperties pluginProperties;

    @Mock
    HaloPluginManager pluginManager;

    @InjectMocks
    PluginServiceImpl pluginService;

    @Test
    void getPresetsTest() {
        var presets = pluginService.getPresets();
        StepVerifier.create(presets)
            .assertNext(plugin -> {
                assertEquals("fake-plugin", plugin.getMetadata().getName());
                assertEquals("0.0.2", plugin.getSpec().getVersion());
                assertEquals(PluginState.RESOLVED, plugin.getStatus().getPhase());
            })
            .verifyComplete();
    }

    @Test
    void getPresetIfNotFound() {
        var plugin = pluginService.getPreset("not-found-plugin");
        StepVerifier.create(plugin)
            .verifyComplete();
    }

    @Test
    void getPresetIfFound() {
        var plugin = pluginService.getPreset("fake-plugin");
        StepVerifier.create(plugin)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Nested
    class InstallOrUpdateTest {

        Path fakePluginPath;

        Path tempDirectory;

        @BeforeEach
        void setUp() throws URISyntaxException, IOException {
            tempDirectory = Files.createTempDirectory("halo-ut-plugin-service-impl-");
            fakePluginPath = tempDirectory.resolve("plugin-0.0.2.jar");
            var fakePluingUri = requireNonNull(
                getClass().getClassLoader().getResource("plugin/plugin-0.0.2")).toURI();
            FileUtils.jar(Paths.get(fakePluingUri), tempDirectory.resolve("plugin-0.0.2.jar"));

            lenient().when(pluginProperties.getPluginsRoot())
                .thenReturn(tempDirectory.resolve("plugins").toString());

            lenient().when(systemVersionSupplier.get()).thenReturn(Version.valueOf("0.0.0"));
        }

        @AfterEach
        void cleanUp() {
            FileUtils.deleteRecursivelyAndSilently(tempDirectory);
        }

        @Test
        void installWhenPluginExists() {
            var existingPlugin = new YamlPluginFinder().find(fakePluginPath);
            when(client.fetch(Plugin.class, "fake-plugin")).thenReturn(Mono.just(existingPlugin));
            var plugin = pluginService.install(fakePluginPath);
            StepVerifier.create(plugin)
                .expectError(PluginAlreadyExistsException.class)
                .verify();

            verify(client).fetch(Plugin.class, "fake-plugin");
            verify(systemVersionSupplier).get();
        }

        @Test
        void installWhenPluginNotExist() {
            when(client.fetch(Plugin.class, "fake-plugin")).thenReturn(Mono.empty());
            var createdPlugin = mock(Plugin.class);
            when(client.create(isA(Plugin.class))).thenReturn(Mono.just(createdPlugin));
            var plugin = pluginService.install(fakePluginPath);
            StepVerifier.create(plugin)
                .expectNext(createdPlugin)
                .verifyComplete();

            verify(client).fetch(Plugin.class, "fake-plugin");
            verify(systemVersionSupplier).get();
            verify(pluginProperties).getPluginsRoot();
            verify(client).create(isA(Plugin.class));
        }

        @Test
        void upgradeWhenPluginNameMismatch() {
            var plugin = pluginService.upgrade("non-fake-plugin", fakePluginPath);
            StepVerifier.create(plugin)
                .expectError(ServerWebInputException.class)
                .verify();

            verify(client, never()).fetch(Plugin.class, "fake-plugin");
        }

        @Test
        void upgradeWhenPluginNotFound() {
            when(client.fetch(Plugin.class, "fake-plugin")).thenReturn(Mono.empty());
            var plugin = pluginService.upgrade("fake-plugin", fakePluginPath);
            StepVerifier.create(plugin)
                .expectError(ServerWebInputException.class)
                .verify();

            verify(client).fetch(Plugin.class, "fake-plugin");
        }

        @Test
        void upgradeNormally() {
            final var prevPlugin = mock(Plugin.class);
            final var spec = mock(Plugin.PluginSpec.class);
            final var updatedPlugin = mock(Plugin.class);
            Metadata metadata = new Metadata();
            metadata.setName("fake-plugin");
            when(prevPlugin.getMetadata()).thenReturn(metadata);

            when(prevPlugin.getSpec()).thenReturn(spec);
            when(spec.getEnabled()).thenReturn(true);

            when(client.fetch(Plugin.class, "fake-plugin"))
                .thenReturn(Mono.just(prevPlugin))
                .thenReturn(Mono.just(prevPlugin))
                .thenReturn(Mono.empty());

            when(client.get(Plugin.class, "fake-plugin"))
                .thenReturn(Mono.just(prevPlugin));

            when(client.update(isA(Plugin.class))).thenReturn(Mono.just(updatedPlugin));

            var plugin = pluginService.upgrade("fake-plugin", fakePluginPath);

            StepVerifier.create(plugin)
                .expectNext(updatedPlugin).verifyComplete();

            verify(client, times(1)).fetch(Plugin.class, "fake-plugin");
            verify(client, times(0)).delete(isA(Plugin.class));
            verify(client).<Plugin>update(argThat(p -> p.getSpec().getEnabled()));
        }
    }

    @Test
    void reload() {
        // given
        String pluginName = "test-plugin";
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginManager.getPlugin(pluginName)).thenReturn(pluginWrapper);
        when(pluginWrapper.getPluginPath())
            .thenReturn(Paths.get("/tmp/plugins/fake-plugin.jar"));
        Plugin plugin = new Plugin();
        plugin.setMetadata(new Metadata());
        plugin.getMetadata().setName(pluginName);
        plugin.setSpec(new Plugin.PluginSpec());
        when(client.get(Plugin.class, pluginName)).thenReturn(Mono.just(plugin));
        when(client.update(plugin)).thenReturn(Mono.just(plugin));

        // when
        Mono<Plugin> result = pluginService.reload(pluginName);

        // then
        assertDoesNotThrow(() -> result.block());
        verify(client, times(1)).update(
            argThat(p -> {
                String reloadPath = p.getMetadata().getAnnotations().get(PluginConst.RELOAD_ANNO);
                assertThat(reloadPath).isEqualTo("/tmp/plugins/fake-plugin.jar");
                return true;
            })
        );
        verify(pluginWrapper, times(1)).getPluginPath();
    }
}
