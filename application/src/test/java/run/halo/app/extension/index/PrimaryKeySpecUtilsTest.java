package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link PrimaryKeySpecUtils}.
 *
 * @author guqing
 * @since 2.12.0
 */
class PrimaryKeySpecUtilsTest {

    @Test
    void primaryKeyIndexSpec() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(FakeExtension.class);
        assertThat(spec.getName()).isEqualTo("metadata.name");
        assertThat(spec.getOrder()).isEqualTo(IndexSpec.OrderType.ASC);
        assertThat(spec.isUnique()).isTrue();
        assertThat(spec.getIndexFunc()).isNotNull();
        assertThat(spec.getIndexFunc().getObjectType()).isEqualTo(FakeExtension.class);

        var extension = new FakeExtension();
        extension.setMetadata(new Metadata());
        extension.getMetadata().setName("fake-name-1");

        assertThat(spec.getIndexFunc().getValues(extension))
            .isEqualTo(Set.of("fake-name-1"));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "FakeExtension", plural = "fakes",
        singular = "fake")
    static class FakeExtension extends AbstractExtension {

    }
}
