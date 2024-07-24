package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.zafarkhaja.semver.Version;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.RuntimeMode;
import org.springframework.context.ApplicationContext;
import run.halo.app.infra.SystemVersionSupplier;

@ExtendWith(MockitoExtension.class)
class HaloPluginManagerTest {

    @Mock
    PluginProperties pluginProperties;

    @Mock
    SystemVersionSupplier systemVersionSupplier;

    @Mock
    PluginsRootGetter pluginsRootGetter;

    @Mock
    ApplicationContext rootContext;

    @InjectMocks
    HaloPluginManager pluginManager;

    @TempDir
    Path tempDir;

    @Test
    void shouldGetDependentsWhilePluginsNotResolved() throws Exception {
        when(pluginProperties.getRuntimeMode()).thenReturn(RuntimeMode.DEPLOYMENT);
        when(systemVersionSupplier.get()).thenReturn(Version.of(1, 2, 3));
        when(pluginsRootGetter.get()).thenReturn(tempDir);
        pluginManager.afterPropertiesSet();
        // if we don't invoke resolves
        var dependents = pluginManager.getDependents("fake-plugin");
        assertTrue(dependents.isEmpty());
    }

    @Test
    void shouldGetDependentsWhilePluginsResolved() throws Exception {
        when(pluginProperties.getRuntimeMode()).thenReturn(RuntimeMode.DEPLOYMENT);
        when(systemVersionSupplier.get()).thenReturn(Version.of(1, 2, 3));
        when(pluginsRootGetter.get()).thenReturn(tempDir);
        pluginManager.afterPropertiesSet();
        pluginManager.loadPlugins();
        // if we don't invoke resolves
        var dependents = pluginManager.getDependents("fake-plugin");
        assertTrue(dependents.isEmpty());
    }


}
