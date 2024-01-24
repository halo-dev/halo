package run.halo.app.plugin;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileNotFoundException;
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
    void containsPlugin() {
        boolean exist = springComponentsFinder.containsComponentsStorage("NotExist");
        assertThat(exist).isFalse();

        assertThatThrownBy(() -> springComponentsFinder.containsComponentsStorage(null))
            .hasMessage("The pluginId cannot be null");
    }

}