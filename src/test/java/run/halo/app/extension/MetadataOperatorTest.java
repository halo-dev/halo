package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.MetadataOperator.metadataDeepEquals;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MetadataOperatorTest {

    Instant now = Instant.now();

    @Test
    void testMetadataDeepEqualsWithSameType() {
        assertTrue(metadataDeepEquals(null, null));

        var left = createFullMetadata();
        var right = createFullMetadata();
        assertFalse(metadataDeepEquals(left, null));
        assertFalse(metadataDeepEquals(null, right));
        assertTrue(metadataDeepEquals(left, right));

        left.setDeletionTimestamp(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setDeletionTimestamp(null);
        assertTrue(metadataDeepEquals(left, right));

        left.setCreationTimestamp(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setCreationTimestamp(null);
        assertTrue(metadataDeepEquals(left, right));

        left.setVersion(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setVersion(null);
        assertTrue(metadataDeepEquals(left, right));

        left.setAnnotations(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setAnnotations(null);
        assertTrue(metadataDeepEquals(left, right));

        left.setLabels(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setLabels(null);
        assertTrue(metadataDeepEquals(left, right));

        left.setName(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setName(null);
        assertTrue(metadataDeepEquals(left, right));

        left.setFinalizers(null);
        assertFalse(metadataDeepEquals(left, right));
        right.setFinalizers(null);
        assertTrue(metadataDeepEquals(left, right));
    }

    @Test
    void testMetadataDeepEqualsWithDifferentType() {
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
        assertTrue(metadataDeepEquals(metadata, mockMetadata));
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