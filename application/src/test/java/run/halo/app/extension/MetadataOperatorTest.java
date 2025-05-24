package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MetadataOperatorTest {

    Instant now = Instant.now();

    @Test
    void testEqualsWithSameType() {
        assertTrue(MetadataOperator.equals(null, null));

        var left = createFullMetadata();
        var right = createFullMetadata();
        assertFalse(MetadataOperator.equals(left, null));
        assertFalse(MetadataOperator.equals(null, right));
        assertTrue(MetadataOperator.equals(left, right));

        left.setDeletionTimestamp(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setDeletionTimestamp(null);
        assertTrue(MetadataOperator.equals(left, right));

        left.setCreationTimestamp(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setCreationTimestamp(null);
        assertTrue(MetadataOperator.equals(left, right));

        left.setVersion(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setVersion(null);
        assertTrue(MetadataOperator.equals(left, right));

        left.setAnnotations(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setAnnotations(null);
        assertTrue(MetadataOperator.equals(left, right));

        left.setLabels(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setLabels(null);
        assertTrue(MetadataOperator.equals(left, right));

        left.setName(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setName(null);
        assertTrue(MetadataOperator.equals(left, right));

        left.setFinalizers(null);
        assertFalse(MetadataOperator.equals(left, right));
        right.setFinalizers(null);
        assertTrue(MetadataOperator.equals(left, right));
    }

    @Test
    void testEqualsWithDifferentType() {
        var mockMetadata = mock(MetadataOperator.class);
        when(mockMetadata.getName()).thenReturn("fake-name");
        when(mockMetadata.getLabels()).thenReturn(Map.of("fake-label-key", "fake-label-value"));
        when(mockMetadata.getAnnotations()).thenReturn(Map.of("fake-anno-key", "fake-anno-value"));
        when(mockMetadata.getVersion()).thenReturn(123L);
        when(mockMetadata.getCreationTimestamp()).thenReturn(now);
        when(mockMetadata.getDeletionTimestamp()).thenReturn(now);
        when(mockMetadata.getFinalizers())
            .thenReturn(Set.of("fake-finalizer-1", "fake-finalizer-2"));

        var metadata = createFullMetadata();
        assertTrue(MetadataOperator.equals(metadata, mockMetadata));
    }

    Metadata createFullMetadata() {
        var metadata = new Metadata();
        metadata.setName("fake-name");
        metadata.setLabels(Map.of("fake-label-key", "fake-label-value"));
        metadata.setAnnotations(Map.of("fake-anno-key", "fake-anno-value"));
        metadata.setVersion(123L);
        metadata.setCreationTimestamp(now);
        metadata.setDeletionTimestamp(now);
        metadata.setFinalizers(Set.of("fake-finalizer-2", "fake-finalizer-1"));
        return metadata;
    }
}