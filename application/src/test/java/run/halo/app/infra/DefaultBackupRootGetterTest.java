package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.properties.HaloProperties;

@ExtendWith(MockitoExtension.class)
class DefaultBackupRootGetterTest {

    @Mock
    HaloProperties haloProperties;

    @InjectMocks
    DefaultBackupRootGetter backupRootGetter;

    @Test
    void shouldGetBackupRootFromWorkDir() {
        when(haloProperties.getWorkDir()).thenReturn(Path.of("workdir"));
        var backupRoot = this.backupRootGetter.get();
        assertEquals(Path.of("workdir", "backups"), backupRoot);
        verify(haloProperties).getWorkDir();
    }


}