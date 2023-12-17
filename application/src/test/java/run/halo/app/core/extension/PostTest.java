package run.halo.app.core.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.MetadataOperator;

class PostTest {

    @Test
    void staticIsPublishedTest() {
        var test = (Function<Map<String, String>, Boolean>) (labels) -> {
            var metadata = Mockito.mock(MetadataOperator.class);
            when(metadata.getLabels()).thenReturn(labels);
            return Post.isPublished(metadata);
        };
        assertEquals(false, test.apply(Map.of()));
        assertEquals(false, test.apply(Map.of("content.halo.run/published", "false")));
        assertEquals(false, test.apply(Map.of("content.halo.run/published", "False")));
        assertEquals(false, test.apply(Map.of("content.halo.run/published", "0")));
        assertEquals(false, test.apply(Map.of("content.halo.run/published", "1")));
        assertEquals(false, test.apply(Map.of("content.halo.run/published", "T")));
        assertEquals(false, test.apply(Map.of("content.halo.run/published", "")));
        assertEquals(true, test.apply(Map.of("content.halo.run/published", "true")));
        assertEquals(true, test.apply(Map.of("content.halo.run/published", "True")));
    }
}