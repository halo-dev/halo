package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.halo.app.infra.utils.FileNameUtils.randomFileName;
import static run.halo.app.infra.utils.FileNameUtils.removeFileExtension;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FileNameUtilsTest {

    @Nested
    class RemoveFileExtensionTest {

        @Test
        public void shouldNotRemoveExtIfNoExt() {
            assertEquals("halo", removeFileExtension("halo", true));
            assertEquals("halo", removeFileExtension("halo", false));
        }

        @Test
        public void shouldRemoveExtIfHasOnlyOneExt() {
            assertEquals("halo", removeFileExtension("halo.run", true));
            assertEquals("halo", removeFileExtension("halo.run", false));
        }

        @Test
        public void shouldNotRemoveExtIfDotfile() {
            assertEquals(".halo", removeFileExtension(".halo", true));
            assertEquals(".halo", removeFileExtension(".halo", false));
        }

        @Test
        public void shouldRemoveExtIfDotfileHasOneExt() {
            assertEquals(".halo", removeFileExtension(".halo.run", true));
            assertEquals(".halo", removeFileExtension(".halo.run", false));
        }

        @Test
        public void shouldRemoveExtIfHasTwoExt() {
            assertEquals("halo", removeFileExtension("halo.tar.gz", true));
            assertEquals("halo.tar", removeFileExtension("halo.tar.gz", false));
        }

        @Test
        public void shouldRemoveExtIfDotfileHasTwoExt() {
            assertEquals(".halo", removeFileExtension(".halo.tar.gz", true));
            assertEquals(".halo.tar", removeFileExtension(".halo.tar.gz", false));
        }

        @Test
        void shouldReturnNullIfFilenameIsNull() {
            assertNull(removeFileExtension(null, true));
            assertNull(removeFileExtension(null, false));
        }
    }

    @Nested
    class AppendRandomFileNameTest {
        @Test
        void normalFileName() {
            String randomFileName = randomFileName("halo.run", 3);
            assertEquals(12, randomFileName.length());
            assertTrue(randomFileName.startsWith("halo-"));
            assertTrue(randomFileName.endsWith(".run"));

            randomFileName = randomFileName(".run", 3);
            assertEquals(7, randomFileName.length());
            assertTrue(randomFileName.endsWith(".run"));

            randomFileName = randomFileName("halo", 3);
            assertEquals(8, randomFileName.length());
            assertTrue(randomFileName.startsWith("halo-"));
        }
    }
}
