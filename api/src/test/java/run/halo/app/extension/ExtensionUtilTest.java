package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExtensionUtilTest {

    @Test
    void testIsNotDeleted() {
        var ext = mock(ExtensionOperator.class);

        when(ext.getMetadata()).thenReturn(null);
        assertFalse(ExtensionUtil.isDeleted(ext));

        var metadata = mock(Metadata.class);
        when(ext.getMetadata()).thenReturn(metadata);
        when(metadata.getDeletionTimestamp()).thenReturn(null);
        assertFalse(ExtensionUtil.isDeleted(ext));

        when(metadata.getDeletionTimestamp()).thenReturn(Instant.now());
        assertTrue(ExtensionUtil.isDeleted(ext));
    }

    @Test
    void addFinalizers() {
        var metadata = new Metadata();
        assertNull(metadata.getFinalizers());
        assertTrue(ExtensionUtil.addFinalizers(metadata, Set.of("fake")));

        assertEquals(Set.of("fake"), metadata.getFinalizers());

        assertFalse(ExtensionUtil.addFinalizers(metadata, Set.of("fake")));
        assertEquals(Set.of("fake"), metadata.getFinalizers());

        assertTrue(ExtensionUtil.addFinalizers(metadata, Set.of("another-fake")));
        assertEquals(Set.of("fake", "another-fake"), metadata.getFinalizers());
    }

    @Test
    void removeFinalizers() {
        var metadata = new Metadata();
        assertFalse(ExtensionUtil.removeFinalizers(metadata, Set.of("fake")));
        assertNull(metadata.getFinalizers());

        metadata.setFinalizers(new HashSet<>(Set.of("fake")));
        assertTrue(ExtensionUtil.removeFinalizers(metadata, Set.of("fake")));
        assertEquals(Set.of(), metadata.getFinalizers());
    }

}
