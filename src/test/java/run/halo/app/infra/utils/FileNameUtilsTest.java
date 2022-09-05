package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileNameUtilsTest {

    @Test
    public void shouldNotRemovExtIfNoExt() {
        assertEquals("halo", FileNameUtils.removeFileExtension("halo", true));
        assertEquals("halo", FileNameUtils.removeFileExtension("halo", false));
    }

    @Test
    public void shouldRemoveExtIfHasOnlyOneExt() {
        assertEquals("halo", FileNameUtils.removeFileExtension("halo.run", true));
        assertEquals("halo", FileNameUtils.removeFileExtension("halo.run", false));
    }

    @Test
    public void shouldNotRemoveExtIfDotfile() {
        assertEquals(".halo", FileNameUtils.removeFileExtension(".halo", true));
        assertEquals(".halo", FileNameUtils.removeFileExtension(".halo", false));
    }

    @Test
    public void shouldRemoveExtIfDotfileHasOneExt() {
        assertEquals(".halo", FileNameUtils.removeFileExtension(".halo.run", true));
        assertEquals(".halo", FileNameUtils.removeFileExtension(".halo.run", false));
    }

    @Test
    public void shouldRemoveExtIfHasTwoExt() {
        assertEquals("halo", FileNameUtils.removeFileExtension("halo.tar.gz", true));
        assertEquals("halo.tar", FileNameUtils.removeFileExtension("halo.tar.gz", false));
    }

    @Test
    public void shouldRemoveExtIfDotfileHasTwoExt() {
        assertEquals(".halo", FileNameUtils.removeFileExtension(".halo.tar.gz", true));
        assertEquals(".halo.tar", FileNameUtils.removeFileExtension(".halo.tar.gz", false));
    }
}