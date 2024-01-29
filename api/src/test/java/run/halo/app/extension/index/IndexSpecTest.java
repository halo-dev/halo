package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Tests for {@link IndexSpec}.
 *
 * @author guqing
 * @since 2.12.0
 */
class IndexSpecTest {

    @Test
    void equalsVerifier() {
        var spec1 = new IndexSpec()
            .setName("metadata.name")
            .setOrder(IndexSpec.OrderType.ASC)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName())
            )
            .setUnique(true);

        var spec2 = new IndexSpec()
            .setName("metadata.name")
            .setOrder(IndexSpec.OrderType.ASC)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName())
            )
            .setUnique(true);

        assertThat(spec1).isEqualTo(spec2);
        assertThat(spec1.equals(spec2)).isTrue();
        assertThat(spec1.hashCode()).isEqualTo(spec2.hashCode());

        var spec3 = new IndexSpec()
            .setName("metadata.name")
            .setOrder(IndexSpec.OrderType.DESC)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName())
            )
            .setUnique(false);
        assertThat(spec1).isEqualTo(spec3);
        assertThat(spec1.equals(spec3)).isTrue();
        assertThat(spec1.hashCode()).isEqualTo(spec3.hashCode());

        var spec4 = new IndexSpec()
            .setName("slug")
            .setOrder(IndexSpec.OrderType.ASC)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName())
            )
            .setUnique(true);
        assertThat(spec1.equals(spec4)).isFalse();
        assertThat(spec1).isNotEqualTo(spec4);
    }

    @Test
    void equalAnotherObject() {
        var spec3 = new IndexSpec()
            .setName("metadata.name");
        assertThat(spec3.equals(new Object())).isFalse();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "Fake", plural = "fakes", singular = "fake")
    static class FakeExtension extends AbstractExtension {
        private String slug;
    }
}
