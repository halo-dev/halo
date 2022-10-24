package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ExtensionOperatorTest {

    @Test
    void testIsNotDeleted() {
        var ext = mock(ExtensionOperator.class);
        var metadata = mock(Metadata.class);
        when(metadata.getDeletionTimestamp()).thenReturn(null);
        when(ext.getMetadata()).thenReturn(metadata);

        assertTrue(ExtensionOperator.isNotDeleted().test(ext));

        when(metadata.getDeletionTimestamp()).thenReturn(Instant.now());
        assertFalse(ExtensionOperator.isNotDeleted().test(ext));
    }

    @Test
    void testIsDeleted() {
        var ext = mock(ExtensionOperator.class);

        when(ext.getMetadata()).thenReturn(null);
        assertFalse(ExtensionOperator.isDeleted(ext));

        var metadata = mock(Metadata.class);
        when(ext.getMetadata()).thenReturn(metadata);
        when(metadata.getDeletionTimestamp()).thenReturn(null);
        assertFalse(ExtensionOperator.isDeleted(ext));

        when(metadata.getDeletionTimestamp()).thenReturn(Instant.now());
        assertTrue(ExtensionOperator.isDeleted(ext));
    }
}