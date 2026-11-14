package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;

class IndexFingerprintsTest {

    @Test
    void sameSpecShouldProduceSameFingerprint() {
        var a = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x")
                .unique(true)
                .build();
        var b = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "y")
                .unique(true)
                .build();
        assertThat(IndexFingerprints.fingerprint(a)).isEqualTo(IndexFingerprints.fingerprint(b));
    }

    @Test
    void differentVersionShouldProduceDifferentFingerprint() {
        var v1 = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x")
                .build();
        var v2 = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x")
                .version(2)
                .build();
        assertThat(IndexFingerprints.fingerprint(v1)).isNotEqualTo(IndexFingerprints.fingerprint(v2));
    }

    @Test
    void differentStructureShouldProduceDifferentFingerprint() {
        var base = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x")
                .build();
        var unique = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x")
                .unique(true)
                .build();
        var multi = IndexSpecs.<FakeExtension, String>multi("spec.slug", String.class)
                .indexFunc(e -> Set.of("x"))
                .build();
        var otherKeyType = IndexSpecs.<FakeExtension, Long>single("spec.slug", Long.class)
                .indexFunc(e -> 1L)
                .build();
        assertThat(IndexFingerprints.fingerprint(base))
                .isNotEqualTo(IndexFingerprints.fingerprint(unique))
                .isNotEqualTo(IndexFingerprints.fingerprint(multi))
                .isNotEqualTo(IndexFingerprints.fingerprint(otherKeyType));
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
