package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static run.halo.app.core.extension.Plugin.BUILT_IN_KEEPER_FINALIZER;
import static run.halo.app.core.extension.Plugin.SYSTEM_RESERVED_LABEL_KEY;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ExtensionInitializedEvent;
import run.halo.app.infra.exception.PluginAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class BuiltInPluginsInitializerTest {

    @Mock
    ExtensionClient client;

    @Mock
    PluginService pluginService;

    @Mock
    ResourcePatternResolver resourcePatternResolver;

    @Mock
    PluginFinder pluginFinder;

    @InjectMocks
    BuiltInPluginsInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer.setResourcePatternResolver(resourcePatternResolver);
        initializer.setPluginFinder(pluginFinder);
    }

    @Test
    void shouldCreateBuiltInPlugins() throws IOException {
        var resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("fake-plugin.jar");
        var pluginUri = URI.create("file:///fake-plugin.jar");
        var pluginPath = Path.of(pluginUri);
        when(resource.getURI()).thenReturn(pluginUri);
        when(resourcePatternResolver.getResources(isA(String.class)))
            .thenReturn(new Resource[] {resource});

        var fakePlugin = createPlugin();
        when(pluginFinder.find(pluginPath)).thenReturn(fakePlugin);
        when(pluginService.install(pluginPath)).thenReturn(Mono.just(fakePlugin));
        doNothing().when(client).update(fakePlugin);
        var event = mock(ExtensionInitializedEvent.class);

        initializer.onApplicationEvent(event);

        assertEquals("true", fakePlugin.getMetadata().getLabels().get(SYSTEM_RESERVED_LABEL_KEY));
        assertTrue(fakePlugin.getMetadata().getFinalizers().contains(BUILT_IN_KEEPER_FINALIZER));
    }

    @Test
    void shouldUpgradeBuiltInPlugins() throws IOException {
        var resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("fake-plugin.jar");
        var pluginUri = URI.create("file:///fake-plugin.jar");
        var pluginPath = Path.of(pluginUri);
        when(resource.getURI()).thenReturn(pluginUri);
        when(resourcePatternResolver.getResources(isA(String.class)))
            .thenReturn(new Resource[] {resource});

        var fakePlugin = createPlugin();
        when(pluginFinder.find(pluginPath)).thenReturn(fakePlugin);
        when(pluginService.install(pluginPath))
            .thenReturn(Mono.error(new PluginAlreadyExistsException("fake-plugin")));
        when(pluginService.upgrade("fake-plugin", pluginPath))
            .thenReturn(Mono.just(fakePlugin));
        doNothing().when(client).update(fakePlugin);
        var event = mock(ExtensionInitializedEvent.class);

        initializer.onApplicationEvent(event);

        assertEquals("true", fakePlugin.getMetadata().getLabels().get(SYSTEM_RESERVED_LABEL_KEY));
        assertTrue(fakePlugin.getMetadata().getFinalizers().contains(BUILT_IN_KEEPER_FINALIZER));
    }

    @Test
    void shouldResetDeletionTimestamp() throws IOException {
        var resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("fake-plugin.jar");
        var pluginUri = URI.create("file:///fake-plugin.jar");
        var pluginPath = Path.of(pluginUri);
        when(resource.getURI()).thenReturn(pluginUri);
        when(resourcePatternResolver.getResources(isA(String.class)))
            .thenReturn(new Resource[] {resource});

        var fakePlugin = createPlugin();
        fakePlugin.getMetadata().setDeletionTimestamp(Instant.now());
        when(pluginFinder.find(pluginPath)).thenReturn(fakePlugin);
        when(pluginService.install(pluginPath)).thenReturn(Mono.just(fakePlugin));
        doNothing().when(client).update(fakePlugin);

        var event = mock(ExtensionInitializedEvent.class);
        initializer.onApplicationEvent(event);

        assertNull(fakePlugin.getMetadata().getDeletionTimestamp());
    }

    Plugin createPlugin() {
        var plugin = new Plugin();
        plugin.setMetadata(new Metadata());
        plugin.getMetadata().setName("fake-plugin");
        return plugin;
    }

}