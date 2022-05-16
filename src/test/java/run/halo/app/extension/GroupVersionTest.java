package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GroupVersionTest {

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAPIVersionIsIllegal() {
        assertThrows(IllegalArgumentException.class, () -> GroupVersion.parseAPIVersion(null),
            "apiVersion is null");
        assertThrows(IllegalArgumentException.class, () -> GroupVersion.parseAPIVersion(""),
            "apiVersion is empty");
        assertThrows(IllegalArgumentException.class, () -> GroupVersion.parseAPIVersion("    "),
            "apiVersion is blank");
        assertThrows(IllegalArgumentException.class, () -> GroupVersion.parseAPIVersion("a/b/c"),
            "apiVersion contains more than 1 '/'");
    }

    @Test
    void shouldReturnGroupVersionCorrectly() {
        assertEquals(new GroupVersion("", "v1"), GroupVersion.parseAPIVersion("v1"),
            "only contains version");
        assertEquals(new GroupVersion("core.halo.run", "v1"),
            GroupVersion.parseAPIVersion("core.halo.run/v1"), "only contains version");
    }
}