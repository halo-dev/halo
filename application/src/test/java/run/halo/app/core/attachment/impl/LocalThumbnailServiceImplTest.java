package run.halo.app.core.attachment.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static run.halo.app.core.attachment.impl.LocalThumbnailServiceImpl.endpointFor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.impl.LocalThumbnailServiceImpl.ThumbnailInfo;
import run.halo.app.core.extension.attachment.LocalThumbnail;
import run.halo.app.infra.ExternalUrlSupplier;

/**
 * Tests for {@link LocalThumbnailServiceImpl}.
 *
 * @author guqing
 * @since 2.19.0
 */
@ExtendWith(MockitoExtension.class)
class LocalThumbnailServiceImplTest {

    @Mock
    private AttachmentRootGetter attachmentWorkDirGetter;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @InjectMocks
    private LocalThumbnailServiceImpl localThumbnailService;

    @Test
    void endpointForTest() {
        var endpoint = endpointFor("example.jpg", "2024", ThumbnailSize.L);
        assertThat(endpoint).isEqualTo("/upload/thumbnails/2024/w1200/example.jpg");
    }

    @Test
    void buildThumbInfoTest(@TempDir Path tempDir) throws IOException {
        var year = LocalThumbnailServiceImpl.getYear();
        var storePath = tempDir.resolve("thumbnails")
            .resolve(year).resolve("w1200");
        ThumbnailInfo.from(new URL("https://halo.run/example.jpg"),
                ThumbnailSize.L, tempDir, "2024")
            .as(StepVerifier::create)
            .consumeNextWith(info -> {
                assertThat(info.fileName()).isEqualTo("example.jpg");
                assertThat(info.filePath())
                    .isEqualTo(storePath.resolve("example.jpg"));
            })
            .verifyComplete();
        // mock file exists
        if (!Files.exists(storePath)) {
            Files.createDirectories(storePath);
        }
        Files.createFile(storePath.resolve("example.jpg"));
        ThumbnailInfo.from(new URL("https://halo.run/example.jpg"),
                ThumbnailSize.L, tempDir, "2024")
            .as(StepVerifier::create)
            .consumeNextWith(info -> {
                assertThat(info.fileName()).isEqualTo("example.jpg");
                assertThat(info.filePath())
                    .isNotEqualTo(storePath.resolve("example.jpg"));
            })
            .verifyComplete();
    }

    @Test
    void getThumbnailFileNameTest() throws MalformedURLException {
        var fileName =
            LocalThumbnailServiceImpl.getThumbnailFileName(new URL("https://halo.run/example.jpg"));
        assertThat(fileName).isEqualTo("example.jpg");

        fileName = LocalThumbnailServiceImpl.getThumbnailFileName(new URL("https://halo.run/"));
        assertThat(fileName).isNotBlank();

        fileName = LocalThumbnailServiceImpl.getThumbnailFileName(
            new URL("https://halo.run/.1fasfg(*&^%$.jpg"));
        assertThat(fileName).isNotBlank();
    }

    @Test
    void toFilePathTest() {
        when(attachmentWorkDirGetter.get()).thenReturn(Path.of("/tmp"));
        var path = localThumbnailService.toFilePath("/thumbnails/2024/w1200/example.jpg");
        assertThat(path).isEqualTo(Path.of("/tmp/thumbnails/2024/w1200/example.jpg"));
    }

    @Test
    void signatureForImageUriTest() throws MalformedURLException {
        when(externalUrlSupplier.getRaw()).thenReturn(new URL("http://localhost:8090"));
        var signature =
            localThumbnailService.signatureForImageUri("http://localhost:8090/example.jpg");
        assertThat(signature).isEqualTo(LocalThumbnail.signatureFor("/example.jpg"));

        signature =
            localThumbnailService.signatureForImageUri("https://localhost:8090/example.jpg");
        assertThat(signature).isEqualTo(LocalThumbnail.signatureFor("/example.jpg"));

        signature =
            localThumbnailService.signatureForImageUri("http://localhost:8091/example.jpg");
        assertThat(signature).isEqualTo(
            LocalThumbnail.signatureFor("http://localhost:8091/example.jpg"));

        signature =
            localThumbnailService.signatureForImageUri("localhost:8090/example.jpg");
        assertThat(signature).isEqualTo(
            LocalThumbnail.signatureFor("localhost:8090/example.jpg"));

        when(externalUrlSupplier.getRaw()).thenReturn(null);
        signature =
            localThumbnailService.signatureForImageUri("http://localhost:8090/example.jpg");
        assertThat(signature).isEqualTo(
            LocalThumbnail.signatureFor("http://localhost:8090/example.jpg"));
    }
}
