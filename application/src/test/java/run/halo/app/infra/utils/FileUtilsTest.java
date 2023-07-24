package run.halo.app.infra.utils;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;
import static run.halo.app.infra.utils.FileUtils.deleteFileSilently;
import static run.halo.app.infra.utils.FileUtils.jar;
import static run.halo.app.infra.utils.FileUtils.unzip;
import static run.halo.app.infra.utils.FileUtils.zip;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.test.StepVerifier;
import run.halo.app.infra.exception.AccessDeniedException;

class FileUtilsTest {

    @TempDir
    Path tempDirectory;

    @Nested
    class DirectoryTraversalTest {

        @Test
        void traversalTestWhenSuccess() {
            checkDirectoryTraversal("/etc/", "/etc/halo/halo/../test");
            checkDirectoryTraversal("/etc/", "/etc/halo/../test");
            checkDirectoryTraversal("/etc/", "/etc/test");
        }

        @Test
        void traversalTestWhenFailure() {
            assertThrows(AccessDeniedException.class,
                () -> checkDirectoryTraversal("/etc/", "/etc/../tmp"));
            assertThrows(AccessDeniedException.class,
                () -> checkDirectoryTraversal("/etc/", "/../tmp"));
            assertThrows(AccessDeniedException.class,
                () -> checkDirectoryTraversal("/etc/", "/tmp"));
        }

    }

    @Nested
    class ZipTest {

        @Test
        void zipFolderAndUnzip() throws IOException, URISyntaxException {
            var uri = requireNonNull(getClass().getClassLoader().getResource("folder-to-zip"))
                .toURI();
            var zipPath = tempDirectory.resolve("example.zip");
            zip(Paths.get(uri), zipPath);

            var unzipTarget = tempDirectory.resolve("example-folder");
            try (var zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                unzip(zis, unzipTarget);
            }

            var content = Files.readString(unzipTarget.resolve("examplefile"));
            assertEquals("Here is an example file.\n", content);
        }

        @Test
        void jarFolderAndUnzip() throws IOException, URISyntaxException {
            var uri = requireNonNull(getClass().getClassLoader().getResource("folder-to-zip"))
                .toURI();
            var zipPath = tempDirectory.resolve("example.zip");
            jar(Paths.get(uri), zipPath);

            var unzipTarget = tempDirectory.resolve("example-folder");
            try (var zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                unzip(zis, unzipTarget);
            }

            var content = Files.readString(unzipTarget.resolve("examplefile"));
            assertEquals("Here is an example file.\n", content);
        }

        @Test
        void zipFolderIfNoSuchFolder() {
            assertThrows(NoSuchFileException.class, () ->
                zip(Paths.get("no-such-folder"), tempDirectory.resolve("example.zip")));
        }

        @Test
        void jarFolderIfNoSuchFolder() {
            assertThrows(NoSuchFileException.class, () ->
                jar(Paths.get("no-such-folder"), tempDirectory.resolve("example.zip")));
        }

    }

    @Test
    void deleteFileSilentlyTest() throws IOException {
        StepVerifier.create(deleteFileSilently(null))
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(deleteFileSilently(tempDirectory))
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(
                deleteFileSilently(Files.createFile(tempDirectory.resolve("for-deleting"))))
            .expectNext(true)
            .verifyComplete();
    }

}