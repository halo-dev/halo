package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static run.halo.app.extension.index.query.Queries.equal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ListOptions}.
 *
 * @author guqing
 * @since 2.17.0
 */
class ListOptionsTest {

    @Nested
    class ListOptionsBuilderTest {

        @Test
        void shouldBuildWithFieldAndLabelSelectors() {
            var listOptions = ListOptions.builder()
                .labelSelector()
                .eq("key-1", "value-1")
                .notEq("key-2", "value-1")
                .exists("key-3")
                .end()
                .andQuery(equal("spec.slug", "fake-slug"))
                .orQuery(equal("spec.slug", "test"))
                .build();
            assertEquals("""
                ((spec.slug = fake-slug OR spec.slug = test) \
                AND ((metadata.labels['key-1'] = 'value-1' \
                AND metadata.labels['key-2'] <> 'value-1') \
                AND EXISTS metadata.labels['key-3']))\
                """, listOptions.toCondition().toString());
        }

        @Test
        void shouldBuildLabelSelectorOnly() {
            var listOptions = ListOptions.builder()
                .labelSelector()
                .notEq("key-2", "value-1")
                .end()
                .build();
            assertEquals("""
                metadata.labels['key-2'] <> 'value-1'\
                """, listOptions.toCondition().toString());
        }

        @Test
        void shouldBuildFieldSelectorOnly() {
            var listOptions = ListOptions.builder()
                .andQuery(equal("spec.slug", "fake-slug"))
                .orQuery(equal("spec.slug", "test"))
                .build();
            assertEquals("""
                (spec.slug = fake-slug OR spec.slug = test)\
                """, listOptions.toCondition().toString());
        }
    }
}
