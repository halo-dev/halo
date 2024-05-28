package run.halo.app.plugin;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginWrapper;
import org.springframework.util.ResourceUtils;

/**
 * Tests for {@link SpringComponentsFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SpringComponentsFinderTest {

    @Mock
    private PluginManager pluginManager;

    @InjectMocks
    private SpringComponentsFinder finder;

    @Test
    void shouldNotInvokeReadClasspathStorages() {
        assertThrows(UnsupportedOperationException.class,
            () -> finder.readClasspathStorages()
        );
    }

    @Test
    void shouldNotInvokeReadPluginsStorages() {
        assertThrows(UnsupportedOperationException.class,
            () -> finder.readPluginsStorages()
        );
    }

    @Test
    void shouldPutEntryIfPluginCreated() throws FileNotFoundException {
        var pluginWrapper = mockPluginWrapper();
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.CREATED);

        var event = new PluginStateEvent(pluginManager, pluginWrapper, null);
        finder.pluginStateChanged(event);

        var classNames = finder.findClassNames("fake-plugin");
        assertEquals(Set.of("run.halo.fake.FakePlugin"), classNames);
    }

    @Test
    void shouldRemoveEntryIfPluginUnloaded() throws FileNotFoundException {
        var pluginWrapper = mockPluginWrapper();
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.CREATED);

        var event = new PluginStateEvent(pluginManager, pluginWrapper, null);
        finder.pluginStateChanged(event);

        var classNames = finder.findClassNames("fake-plugin");
        assertFalse(classNames.isEmpty());

        when(pluginWrapper.getPluginState()).thenReturn(PluginState.UNLOADED);
        event = new PluginStateEvent(pluginManager, pluginWrapper, null);
        finder.pluginStateChanged(event);

        classNames = finder.findClassNames("fake-plugin");
        assertTrue(classNames.isEmpty());
    }

    private PluginWrapper mockPluginWrapper() throws FileNotFoundException {
        var pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginId()).thenReturn("fake-plugin");

        var pluginRootUrl = ResourceUtils.getURL("classpath:plugin/plugin-for-finder/");
        var classLoader = new URLClassLoader(new URL[] {pluginRootUrl});
        when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        return pluginWrapper;
    }
}