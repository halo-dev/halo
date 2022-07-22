package run.halo.app.plugin;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginClassLoader;
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

    private File testFile;
    @Mock
    private HaloPluginManager pluginManager;
    @Mock
    private PluginWrapper pluginWrapper;
    @Mock
    private PluginClassLoader pluginClassLoader;

    @InjectMocks
    private SpringComponentsFinder springComponentsFinder;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        testFile = ResourceUtils.getFile("classpath:plugin/test-plugin-components.idx");
    }

    @Test
    void readPluginStorageToMemory() throws FileNotFoundException {
        boolean contains = springComponentsFinder.containsComponentsStorage("fakePlugin");
        assertThat(contains).isFalse();

        when(pluginWrapper.getPluginId()).thenReturn("fakePlugin");
        when(pluginWrapper.getPluginClassLoader()).thenReturn(pluginClassLoader);
        when(pluginClassLoader.getResourceAsStream(any()))
            .thenReturn(new FileInputStream(testFile));

        springComponentsFinder.readPluginStorageToMemory(pluginWrapper);

        contains = springComponentsFinder.containsComponentsStorage("fakePlugin");
        assertThat(contains).isTrue();

        verify(pluginClassLoader, times(1)).getResourceAsStream(any());

        // repeat it
        springComponentsFinder.readPluginStorageToMemory(pluginWrapper);
        verify(pluginClassLoader, times(1)).getResourceAsStream(any());
    }

    @Test
    void containsPlugin() {
        boolean exist = springComponentsFinder.containsComponentsStorage("NotExist");
        assertThat(exist).isFalse();

        assertThatThrownBy(() -> springComponentsFinder.containsComponentsStorage(null))
            .hasMessage("The pluginId cannot be null");
    }

    @Test
    void removeComponentsCache() {
        springComponentsFinder.putComponentsStorage("fakePlugin", Set.of("A"));
        boolean contains = springComponentsFinder.containsComponentsStorage("fakePlugin");
        assertThat(contains).isTrue();

        springComponentsFinder.removeComponentsStorage("fakePlugin");

        contains = springComponentsFinder.containsComponentsStorage("fakePlugin");
        assertThat(contains).isFalse();
    }
}