package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link ThumbnailGenerator}.
 *
 * @author guqing
 * @since 2.19.0
 */
@ExtendWith(MockitoExtension.class)
class ThumbnailGeneratorTest {

    @Test
    void sanitizeFileName() {
        String sanitizedFileName = ThumbnailGenerator.sanitizeFileName("example.jpg");
        assertThat(sanitizedFileName).isEqualTo("example.jpg");

        sanitizedFileName = ThumbnailGenerator.sanitizeFileName("exampl./e$%^7*—=.jpg");
        assertThat(sanitizedFileName).isEqualTo("exampl.e7.jpg");
    }

    @Nested
    class ImageDownloaderTest {
        private final ThumbnailGenerator.ImageDownloader imageDownloader =
            new ThumbnailGenerator.ImageDownloader();

        private Path tempFile;

        @AfterEach
        void tearDown() throws IOException {
            if (tempFile != null && Files.exists(tempFile)) {
                Files.delete(tempFile);
            }
        }

        @Test
        void testDownloadImage_Success() throws Exception {
            var imageUrl = new URL("https://example.com/sample-image.jpg");
            URL spyImageUrl = spy(imageUrl);
            String mockImageData = "fakeImageData";
            InputStream mockInputStream = new ByteArrayInputStream(mockImageData.getBytes());

            var urlConnection = mock(HttpURLConnection.class);
            doAnswer(invocation -> urlConnection).when(spyImageUrl).openConnection();
            doReturn(mockInputStream).when(urlConnection).getInputStream();

            var path = imageDownloader.downloadFileInternal(spyImageUrl);
            assertThat(path).isNotNull();
            tempFile = path;
            assertThat(Files.exists(path)).isTrue();
            try {
                assertThat(Files.size(path)).isEqualTo(mockImageData.length());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String fileName = path.getFileName().toString();
            assertThat(fileName).endsWith(".tmp");
        }

        @Test
        void downloadImage_FileSizeLimitExceeded() throws Exception {
            String largeImageUrl = "https://example.com/large-image.jpg";
            URL spyImageUrl = spy(new URL(largeImageUrl));

            // larger than MAX_FILE_SIZE
            var fileSizeByte = ThumbnailGenerator.MAX_FILE_SIZE + 10;
            byte[] largeImageData = new byte[fileSizeByte];
            InputStream mockInputStream = new ByteArrayInputStream(largeImageData);
            var urlConnection = mock(HttpURLConnection.class);
            doAnswer(invocation -> urlConnection).when(spyImageUrl).openConnection();
            doReturn(mockInputStream).when(urlConnection).getInputStream();
            assertThatThrownBy(() -> imageDownloader.downloadFileInternal(spyImageUrl))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("File size exceeds the limit");
        }
    }
}
