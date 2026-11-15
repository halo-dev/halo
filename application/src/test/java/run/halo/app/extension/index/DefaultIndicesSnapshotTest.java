package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;

class DefaultIndicesSnapshotTest {

    private static DefaultIndices<FakePost> newIndices() {
        var slugSpec =
                (SingleValueIndexSpec<FakePost, String>) IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                        .indexFunc(p -> p.slug)
                        .nullable(false)
                        .build();
        var tagsSpec =
                (MultiValueIndexSpec<FakePost, String>) IndexSpecs.<FakePost, String>multi("spec.tags", String.class)
                        .indexFunc(p -> p.tags)
                        .build();
        return new DefaultIndices<>(
                List.of(new SingleValueIndex<>(slugSpec), new MultiValueIndex<>(tagsSpec), new LabelIndex<>()));
    }

    @SuppressWarnings("unchecked")
    private static ValueIndexQuery<String> valueIndex(DefaultIndices<FakePost> indices, String name) {
        return (ValueIndexQuery<String>) indices.getIndex(name);
    }

    @Test
    void completeSnapshotShouldRestoreWithoutVersionGuards() {
        var original = newIndices();
        var old = new FakePost("post-1", "old", null);
        old.getMetadata().setVersion(7L);
        original.insert(old);
        var restored = newIndices();
        assertThat(restored.restore(original.dump())).isTrue();
        assertThat(valueIndex(restored, "spec.slug").equal("old")).containsExactly("post-1");
        restored.delete(old);
        var recreated = new FakePost("post-1", "new", null);
        recreated.getMetadata().setVersion(1L);
        restored.insert(recreated);
        assertThat(valueIndex(restored, "spec.slug").equal("old")).isEmpty();
        assertThat(valueIndex(restored, "spec.slug").equal("new")).containsExactly("post-1");
    }

    @Test
    void changedSpecShouldRejectWholeSnapshot() {
        var original = newIndices();
        original.insert(new FakePost("post-1", "old", null));
        var snapshot = original.dump();
        var changed = new IndicesSnapshot(snapshot.indices().stream()
                .map(index -> new IndexSnapshot(
                        index.name(), "different", index.keyType(), index.entries(), index.nullKeys()))
                .toList());
        var restored = newIndices();
        assertThat(restored.restore(changed)).isFalse();
        assertThat(valueIndex(restored, "spec.slug").equal("old")).isEmpty();
    }

    @Test
    void invalidKeyShouldDiscardPartiallyRestoredIndices() {
        var original = newIndices();
        original.insert(new FakePost("post-1", "old", null));
        var broken = new IndicesSnapshot(original.dump().indices().stream()
                .map(index -> index.name().equals("spec.tags")
                        ? new IndexSnapshot(
                                index.name(),
                                index.fingerprint(),
                                index.keyType(),
                                List.of(new IndexSnapshot.Entry(List.of(), List.of("post-1"))),
                                List.of())
                        : index)
                .toList());
        var restored = newIndices();
        assertThat(restored.restore(broken)).isFalse();
        assertThat(valueIndex(restored, "spec.slug").equal("old")).isEmpty();
        restored.insert(new FakePost("post-2", "current", null));
        assertThat(valueIndex(restored, "spec.slug").equal("current")).containsExactly("post-2");
    }

    static class FakePost implements Extension {
        private final Metadata metadata = new Metadata();
        String slug;
        Instant publishTime;
        Set<String> tags = Set.of();

        FakePost(String name, String slug, Instant publishTime) {
            metadata.setName(name);
            this.slug = slug;
            this.publishTime = publishTime;
        }

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
