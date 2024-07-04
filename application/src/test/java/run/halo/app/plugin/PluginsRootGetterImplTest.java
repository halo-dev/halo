package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.properties.HaloProperties;

@ExtendWith(MockitoExtension.class)
class PluginsRootGetterImplTest {

    @Mock
    HaloProperties haloProperties;

    @InjectMocks
    PluginsRootGetterImpl pluginsRootGetter;

    @Test
    void shouldGetterPluginsRootCorrectly() {
        var haloWorkDir = Paths.get("halo-work-dir");
        when(haloProperties.getWorkDir()).thenReturn(haloWorkDir);
        assertEquals(haloWorkDir.resolve("plugins"), pluginsRootGetter.get());
    }

}
