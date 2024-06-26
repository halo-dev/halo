package run.halo.app.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static run.halo.app.extension.index.query.QueryFactory.equal;

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
        void buildTest() {
            var listOptions = ListOptions.builder()
                .labelSelector()
                .eq("key-1", "value-1")
                .notEq("key-2", "value-1")
                .exists("key-3")
                .end()
                .andQuery(equal("spec.slug", "fake-slug"))
                .orQuery(equal("spec.slug", "test"))
                .build();
            System.out.println(listOptions);
            assertThat(listOptions.toString()).isEqualTo(
                "fieldSelector: (spec.slug = 'fake-slug' OR spec.slug = 'test'), labelSelector: "
                    + "(key-1 equal value-1, key-2 not_equal value-1, key-3 EXISTS)");
        }

        @Test
        void buildTest2() {
            var listOptions = ListOptions.builder()
                .labelSelector()
                .notEq("key-2", "value-1")
                .end()
                .fieldQuery(equal("spec.slug", "fake-slug"))
                .build();
            assertThat(listOptions.toString())
                .isEqualTo(
                    "fieldSelector: (spec.slug = 'fake-slug'), labelSelector: (key-2 not_equal "
                        + "value-1)");
        }
    }
}
