package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link IndexAttributeFactory}.
 *
 * @author guqing
 * @since 2.12.0
 */
class IndexAttributeFactoryTest {

    @Test
    void shouldCreateMultiValueAttribute() {
        var attribute = IndexAttributeFactory.multiValueAttribute(FakeExtension.class,
            FakeExtension::getTags);
        assertThat(attribute).isNotNull();
        assertThat(attribute.getObjectType()).isEqualTo(FakeExtension.class);
        var extension = new FakeExtension();
        extension.setMetadata(new Metadata());
        extension.getMetadata().setName("fake-name-1");
        extension.setTags(Set.of("tag1", "tag2"));
        Assertions.assertEquals(
            Set.of(new UnknownKey("tag1"), new UnknownKey("tag2")),
            attribute.getValues(extension)
        );
    }

    @Test
    void shouldCreateSingleValueAttribute() {
        var attribute = IndexAttributeFactory.simpleAttribute(FakeExtension.class,
            FakeExtension::getName);
        assertThat(attribute).isNotNull();
        assertThat(attribute.getObjectType()).isEqualTo(FakeExtension.class);
        var extension = new FakeExtension();
        extension.setMetadata(new Metadata());
        extension.getMetadata().setName("fake-name-2");
        extension.setName("Fake Extension Name");
        Assertions.assertEquals(
            Set.of(new UnknownKey("Fake Extension Name")),
            attribute.getValues(extension)
        );
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "FakeExtension", plural = "fakes",
        singular = "fake")
    static class FakeExtension extends AbstractExtension {

        private Set<String> tags;

        private String name;

    }
}
