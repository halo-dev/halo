package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;

class ResourceReferenceTest {
    @Test
    void rejectsNullSource() {
        assertThatThrownBy(() -> ResourceReference.from(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsSourceWithoutMetadata() {
        assertThatThrownBy(() -> ResourceReference.from(new Post())).isInstanceOf(NullPointerException.class);
    }

    @Test
    void externalIdentityDoesNotInventAnExtensionType() {
        var subject = ResourceReference.external("shop.tenant-a", "product", "42");
        assertThat(subject.isExtension()).isFalse();
        assertThat(subject.gvk()).isNull();
        assertThat(subject.name()).isEqualTo("42");
        assertThat(subject).isNotEqualTo(ResourceReference.external("shop.tenant-b", "product", "42"));
        assertThat(subject).isNotEqualTo(new ResourceReference(Post.GVK, "42"));
    }

    @Test
    void rejectsAmbiguousOrIncompleteIdentityKinds() {
        assertThatThrownBy(() -> ResourceReference.external("", "product", "42"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ResourceReference.external("shop", "", "42"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ResourceReference(Post.GVK, "42", "shop", "product"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void capturesIdentityWithoutRetainingMutableMetadata() {
        var source = new Post();
        var metadata = new Metadata();
        metadata.setName("original");
        source.setMetadata(metadata);

        var reference = ResourceReference.from(source);
        metadata.setName("changed");

        assertThat(reference).isEqualTo(new ResourceReference(Post.GVK, "original"));
    }
}
