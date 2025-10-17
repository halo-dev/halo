package run.halo.app.core.attachment.thumbnail;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.infra.properties.AttachmentProperties;
import run.halo.app.infra.properties.HaloProperties;

@ExtendWith(MockitoExtension.class)
class DefaultLocalThumbnailServiceTest {

    @Mock
    AttachmentRootGetter attachmentRootGetter;

    @Mock
    HaloProperties haloProperties;

    @Mock
    AttachmentProperties.ThumbnailProperties thumbnailProperties;

    DefaultLocalThumbnailService generator;

    Path source;

    @TempDir
    Path attachmentRoot;

    @BeforeEach
    void setUp() throws IOException {
        var attachmentProperties = mock(AttachmentProperties.class);
        when(attachmentProperties.getThumbnail()).thenReturn(thumbnailProperties);
        when(haloProperties.getAttachment()).thenReturn(attachmentProperties);
        when(thumbnailProperties.isDisabled()).thenReturn(false);
        when(thumbnailProperties.getConcurrentThreads()).thenReturn(1);
        var imagePath =
            ResourceUtils.getFile("classpath:static/images/halo-logo-401x401.png").toPath();
        lenient().when(attachmentRootGetter.get()).thenReturn(attachmentRoot);
        this.source = attachmentRoot.resolve("static").resolve("hal-logo-401x401.png");
        Files.createDirectories(this.source.getParent());
        Files.copy(imagePath, this.source);

        this.generator =
            new DefaultLocalThumbnailService(this.attachmentRootGetter, this.haloProperties);
        var executorService = MoreExecutors.newDirectExecutorService();
        this.generator.setExecutorService(executorService);
    }

    @AfterEach
    void cleanUp() throws Exception {
        this.generator.destroy();
    }

    @Test
    void shouldGenerateThumbnail() {
        this.generator.generate(source, ThumbnailSize.S)
            .as(StepVerifier::create)
            .assertNext(resource -> {
                assertTrue(resource.isReadable());
                assertDoesNotThrow(() -> {
                    var thumbnailSize = resource.contentLength();
                    var sourceSize = Files.size(source);
                    assertTrue(thumbnailSize < sourceSize);
                });
            })
            .verifyComplete();
    }

    @Test
    void shouldReplaceWithSourceIfSizeIsLarger() {
        this.generator.generate(source, ThumbnailSize.M)
            .as(StepVerifier::create)
            .assertNext(resource -> {
                assertTrue(resource.isReadable());
                assertDoesNotThrow(() -> {
                    var thumbnailSize = resource.contentLength();
                    var sourceSize = Files.size(source);
                    assertEquals(thumbnailSize, sourceSize);
                });
            })
            .verifyComplete();
    }

    @Test
    void shouldDisableThumbnailGeneration() {
        when(thumbnailProperties.isDisabled()).thenReturn(true);
        this.generator.generate(source, ThumbnailSize.S)
            .as(StepVerifier::create)
            .assertNext(resource -> {
                try {
                    assertEquals(resource.getFile(), source.toFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }
}