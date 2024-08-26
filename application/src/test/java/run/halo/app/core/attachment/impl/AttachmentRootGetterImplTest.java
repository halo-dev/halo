package run.halo.app.core.attachment.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Tests for {@link AttachmentRootGetterImpl}.
 *
 * @author guqing
 * @since 2.19.0
 */
@ExtendWith(MockitoExtension.class)
class AttachmentRootGetterImplTest {
    @Mock
    private HaloProperties haloProperties;

    @InjectMocks
    private AttachmentRootGetterImpl localAttachmentDirGetter;

    @Test
    void get() {
        var rootPath = Path.of("/tmp");
        when(haloProperties.getWorkDir()).thenReturn(rootPath);
        var dir = localAttachmentDirGetter.get();
        assertThat(dir).isEqualTo(rootPath.resolve("attachments"));
    }
}