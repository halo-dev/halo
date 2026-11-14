package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;

class IndexSpecVersionTest {

    @Test
    void defaultVersionShouldBeOne() {
        var spec = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> e.getMetadata().getName())
                .build();
        assertThat(spec.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldSetVersionOnSingleValueSpec() {
        var spec = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> e.getMetadata().getName())
                .version(3)
                .build();
        assertThat(spec.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldSetVersionOnMultiValueSpec() {
        var spec = IndexSpecs.<FakeExtension, String>multi("spec.tags", String.class)
                .indexFunc(e -> java.util.Set.of("a"))
                .version(2)
                .build();
        assertThat(spec.getVersion()).isEqualTo(2);
    }

    @Test
    void shouldRejectNonPositiveVersion() {
        var builder = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> e.getMetadata().getName());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> builder.version(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class FakeExtension implements Extension {
        private final Metadata metadata = new Metadata();

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(MetadataOperator metadata) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setApiVersion(String apiVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setKind(String kind) {
            throw new UnsupportedOperationException();
        }
    }
}
