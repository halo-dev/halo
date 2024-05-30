package run.halo.app.core.extension.service.impl;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.zafarkhaja.semver.Version;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginDescriptor;
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
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.SpringPluginManager;
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
    SpringPluginManager pluginManager;

    @InjectMocks
    PluginServiceImpl pluginService;

    @Test
    void getPresetsTest() {
        var presets = pluginService.getPresets();
        StepVerifier.create(presets)
            .assertNext(plugin -> {
                assertEquals("fake-plugin", plugin.getMetadata().getName());
                assertEquals("0.0.2", plugin.getSpec().getVersion());
                assertEquals(Plugin.Phase.PENDING, plugin.getStatus().getPhase());
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
    class InstallUpdateReloadTest {

        Path fakePluginPath;

        @TempDir
        Path tempDirectory;

        @BeforeEach
        void setUp() throws URISyntaxException, IOException {
            fakePluginPath = tempDirectory.resolve("plugin-0.0.2.jar");
            var fakePluingUri = requireNonNull(
                getClass().getClassLoader().getResource("plugin/plugin-0.0.2")).toURI();
            FileUtils.jar(Paths.get(fakePluingUri), tempDirectory.resolve("plugin-0.0.2.jar"));

            lenient().when(pluginProperties.getPluginsRoot())
                .thenReturn(tempDirectory.resolve("plugins").toString());

            lenient().when(systemVersionSupplier.get()).thenReturn(Version.valueOf("0.0.0"));
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
            var oldFakePlugin = createPlugin("fake-plugin", plugin -> {
                plugin.getSpec().setEnabled(true);
                plugin.getSpec().setVersion("0.0.1");
            });

            when(client.fetch(Plugin.class, "fake-plugin"))
                .thenReturn(Mono.just(oldFakePlugin))
                .thenReturn(Mono.just(oldFakePlugin))
                .thenReturn(Mono.empty());

            when(client.update(oldFakePlugin)).thenReturn(Mono.just(oldFakePlugin));

            var plugin = pluginService.upgrade("fake-plugin", fakePluginPath);

            StepVerifier.create(plugin)
                .expectNext(oldFakePlugin)
                .verifyComplete();

            verify(client).fetch(Plugin.class, "fake-plugin");
            verify(client).update(oldFakePlugin);
            assertTrue(oldFakePlugin.getSpec().getEnabled());
            assertEquals("0.0.2", oldFakePlugin.getSpec().getVersion());
            assertEquals(
                tempDirectory.resolve("plugins").resolve("fake-plugin-0.0.2.jar").toString(),
                oldFakePlugin.getMetadata().getAnnotations().get(PluginConst.PLUGIN_PATH));
        }

        @Test
        void shouldNotReloadIfLoadLocationIsNotReady() {
            var pluginName = "test-plugin";

            var testPlugin = createPlugin(pluginName, plugin -> {
            });

            when(client.get(Plugin.class, pluginName)).thenReturn(Mono.just(testPlugin));

            pluginService.reload(pluginName)
                .as(StepVerifier::create)
                .consumeErrorWith(t -> {
                    assertInstanceOf(IllegalStateException.class, t);
                    assertEquals("Load location of plugin has not been populated.",
                        t.getMessage());
                })
                .verify();

            verify(client).get(Plugin.class, pluginName);
        }

        @Test
        void shouldReloadIfLoadLocationReady() {
            var pluginName = "test-plugin";

            var testPlugin = createPlugin(pluginName, plugin -> {
                plugin.getStatus().setLoadLocation(fakePluginPath.toUri());
            });

            when(client.get(Plugin.class, pluginName)).thenReturn(Mono.just(testPlugin));
            when(client.update(testPlugin)).thenReturn(Mono.just(testPlugin));

            pluginService.reload(pluginName)
                .as(StepVerifier::create)
                .expectNext(testPlugin)
                .verifyComplete();

            assertEquals(fakePluginPath.toString(),
                testPlugin.getMetadata().getAnnotations().get(PluginConst.PLUGIN_PATH));
            verify(client).get(Plugin.class, pluginName);
            verify(client).update(testPlugin);
        }

    }


    @Test
    void generateJsBundleVersionTest() {
        var plugin1 = mock(PluginWrapper.class);
        var plugin2 = mock(PluginWrapper.class);
        var plugin3 = mock(PluginWrapper.class);
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(plugin1, plugin2, plugin3));

        var descriptor1 = mock(PluginDescriptor.class);
        var descriptor2 = mock(PluginDescriptor.class);
        var descriptor3 = mock(PluginDescriptor.class);
        when(plugin1.getDescriptor()).thenReturn(descriptor1);
        when(plugin2.getDescriptor()).thenReturn(descriptor2);
        when(plugin3.getDescriptor()).thenReturn(descriptor3);

        when(plugin1.getPluginId()).thenReturn("fake-1");
        when(plugin2.getPluginId()).thenReturn("fake-2");
        when(plugin3.getPluginId()).thenReturn("fake-3");

        when(descriptor1.getVersion()).thenReturn("1.0.0");
        when(descriptor2.getVersion()).thenReturn("2.0.0");
        when(descriptor3.getVersion()).thenReturn("3.0.0");

        var str = "fake-1:1.0.0fake-2:2.0.0fake-3:3.0.0";
        var result = Hashing.sha256().hashUnencodedChars(str).toString();
        assertThat(result.length()).isEqualTo(64);

        pluginService.generateJsBundleVersion()
            .as(StepVerifier::create)
            .consumeNextWith(version -> assertThat(version).isEqualTo(result))
            .verifyComplete();

        var plugin4 = mock(PluginWrapper.class);
        var descriptor4 = mock(PluginDescriptor.class);
        when(plugin4.getDescriptor()).thenReturn(descriptor4);
        when(plugin4.getPluginId()).thenReturn("fake-4");
        when(descriptor4.getVersion()).thenReturn("3.0.0");
        var str2 = "fake-1:1.0.0fake-2:2.0.0fake-4:3.0.0";
        var result2 = Hashing.sha256().hashUnencodedChars(str2).toString();
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(plugin1, plugin2, plugin4));
        pluginService.generateJsBundleVersion()
            .as(StepVerifier::create)
            .consumeNextWith(version -> assertThat(version).isEqualTo(result2))
            .verifyComplete();

        assertThat(result).isNotEqualTo(result2);
    }

    @Nested
    class PluginStateChangeTest {

        @Test
        void shouldEnablePluginIfPluginWasNotStarted() {
            var plugin = createPlugin("fake-plugin", p -> {
                p.getSpec().setEnabled(false);
                p.statusNonNull().setPhase(Plugin.Phase.RESOLVED);
            });

            when(client.get(Plugin.class, "fake-plugin")).thenReturn(Mono.just(plugin))
                .thenReturn(Mono.fromSupplier(() -> {
                    plugin.statusNonNull().setPhase(Plugin.Phase.STARTED);
                    return plugin;
                }));
            when(client.update(plugin)).thenReturn(Mono.just(plugin));

            pluginService.changeState("fake-plugin", true, false)
                .as(StepVerifier::create)
                .expectNext(plugin)
                .verifyComplete();

            assertTrue(plugin.getSpec().getEnabled());
        }

        @Test
        void shouldDisablePluginIfAlreadyStarted() {
            var plugin = createPlugin("fake-plugin", p -> {
                p.getSpec().setEnabled(true);
                p.statusNonNull().setPhase(Plugin.Phase.STARTED);
            });

            when(client.get(Plugin.class, "fake-plugin")).thenReturn(Mono.just(plugin))
                .thenReturn(Mono.fromSupplier(() -> {
                    plugin.getStatus().setPhase(Plugin.Phase.STOPPED);
                    return plugin;
                }));
            when(client.update(plugin)).thenReturn(Mono.just(plugin));

            pluginService.changeState("fake-plugin", false, false)
                .as(StepVerifier::create)
                .expectNext(plugin)
                .verifyComplete();
            assertFalse(plugin.getSpec().getEnabled());
        }
    }

    Plugin createPlugin(String name, Consumer<Plugin> pluginConsumer) {
        var plugin = new Plugin();
        plugin.setMetadata(new Metadata());
        plugin.getMetadata().setName(name);
        plugin.setSpec(new Plugin.PluginSpec());
        plugin.setStatus(new Plugin.PluginStatus());
        pluginConsumer.accept(plugin);
        return plugin;
    }
}
