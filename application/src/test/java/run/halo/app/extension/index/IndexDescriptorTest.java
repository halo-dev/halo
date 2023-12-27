package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.extension.FakeExtension;

/**
 * Tests for {@link IndexDescriptor}.
 *
 * @author guqing
 * @since 2.12.0
 */
class IndexDescriptorTest {

    @Test
    void equalsVerifier() {
        var spec1 = new IndexSpec()
            .setName("metadata.name")
            .setOrder(IndexSpec.OrderType.ASC)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName())
            )
            .setUnique(true);

        var descriptor = new IndexDescriptor(spec1);
        var descriptor2 = new IndexDescriptor(spec1);
        assertThat(descriptor).isEqualTo(descriptor2);

        var spec2 = new IndexSpec()
            .setName("metadata.name")
            .setOrder(IndexSpec.OrderType.DESC)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
                e -> e.getMetadata().getName())
            )
            .setUnique(false);
        var descriptor3 = new IndexDescriptor(spec2);
        assertThat(descriptor).isEqualTo(descriptor3);
    }
}
