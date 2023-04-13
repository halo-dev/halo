package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ContentTemplateHeadProcessor}.
 *
 * @author guqing
 * @since 2.5.0
 */
class ContentTemplateHeadProcessorTest {

    @Nested
    class ExcerptToMetaDescriptionTest {
        @Test
        void toMetaWhenExcerptIsNull() {
            List<Map<String, String>> htmlMetas = new ArrayList<>();
            htmlMetas.add(createMetaMap("keywords", "test"));
            var result = ContentTemplateHeadProcessor.excerptToMetaDescriptionIfAbsent(htmlMetas,
                null);
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsEntry("name", "keywords");
            assertThat(result.get(1)).containsEntry("name", "description")
                .containsEntry("content", "");
        }

        @Test
        void toMetaWhenWhenHtmlMetaIsNull() {
            var result = ContentTemplateHeadProcessor.excerptToMetaDescriptionIfAbsent(null,
                null);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsEntry("name", "description")
                .containsEntry("content", "");
        }

        @Test
        void toMetaWhenWhenExcerptNotEmpty() {
            List<Map<String, String>> htmlMetas = new ArrayList<>();
            htmlMetas.add(createMetaMap("keywords", "test"));
            var result = ContentTemplateHeadProcessor.excerptToMetaDescriptionIfAbsent(htmlMetas,
                "test excerpt");
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsEntry("name", "keywords");
            assertThat(result.get(1)).containsEntry("name", "description")
                .containsEntry("content", "test excerpt");
        }

        @Test
        void toMetaWhenWhenDescriptionExistsAndEmpty() {
            List<Map<String, String>> htmlMetas = new ArrayList<>();
            htmlMetas.add(createMetaMap("keywords", "test"));
            htmlMetas.add(createMetaMap("description", ""));
            var result = ContentTemplateHeadProcessor.excerptToMetaDescriptionIfAbsent(htmlMetas,
                "test excerpt");
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsEntry("name", "keywords");
            assertThat(result.get(1)).containsEntry("name", "description")
                .containsEntry("content", "test excerpt");
        }

        @Test
        void toMetaWhenWhenDescriptionExistsAndNotEmpty() {
            List<Map<String, String>> htmlMetas = new ArrayList<>();
            htmlMetas.add(createMetaMap("keywords", "test"));
            htmlMetas.add(createMetaMap("description", "test description"));
            var result = ContentTemplateHeadProcessor.excerptToMetaDescriptionIfAbsent(htmlMetas,
                "test excerpt");
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsEntry("name", "keywords");
            assertThat(result.get(1)).containsEntry("name", "description")
                .containsEntry("content", "test description");
        }

        Map<String, String> createMetaMap(String nameValue, String contentValue) {
            Map<String, String> metaMap = new HashMap<>();
            metaMap.put("name", nameValue);
            metaMap.put("content", contentValue);
            return metaMap;
        }
    }
}
