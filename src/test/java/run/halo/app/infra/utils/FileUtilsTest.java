package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.exception.AccessDeniedException;

class FileUtilsTest {

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
}