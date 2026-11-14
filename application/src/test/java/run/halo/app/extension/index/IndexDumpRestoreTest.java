package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;

class IndexDumpRestoreTest {

    @Test
    void singleValueIndexShouldRoundTrip() {
        var spec =
                (SingleValueIndexSpec<FakePost, String>) IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                        .indexFunc(p -> p.slug)
                        .unique(true)
                        .nullable(false)
                        .build();
        var index = new SingleValueIndex<>(spec);
        insert(index, new FakePost("post-1", "hello", Instant.parse("2026-01-01T00:00:00Z")));
        insert(index, new FakePost("post-2", "world", Instant.parse("2026-01-02T00:00:00Z")));

        var snapshot = index.dump();
        assertThat(snapshot.name()).isEqualTo("spec.slug");
        assertThat(snapshot.fingerprint()).isEqualTo(IndexFingerprints.fingerprint(spec));

        var restored = new SingleValueIndex<>(spec);
        restored.restore(snapshot);
        assertThat(restored.equal("hello")).containsExactly("post-1");
        assertThat(restored.equal("world")).containsExactly("post-2");
        assertThat(restored.getKey("post-2")).isEqualTo("world");
    }

    @Test
    void singleValueIndexWithInstantKeyShouldRoundTrip() {
        var spec = (SingleValueIndexSpec<FakePost, Instant>)
                IndexSpecs.<FakePost, Instant>single("spec.publishTime", Instant.class)
                        .indexFunc(p -> p.publishTime)
                        .build();
        var index = new SingleValueIndex<>(spec);
        var t = Instant.parse("2026-06-01T12:00:00Z");
        insert(index, new FakePost("post-1", "a", t));

        var restored = new SingleValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.equal(t)).containsExactly("post-1");
    }

    @Test
    void singleValueIndexWithEnumKeyShouldRoundTrip() {
        var spec = (SingleValueIndexSpec<FakePost, Visibility>)
                IndexSpecs.<FakePost, Visibility>single("spec.visible", Visibility.class)
                        .indexFunc(p -> p.visible)
                        .nullable(false)
                        .build();
        var index = new SingleValueIndex<>(spec);
        var post = new FakePost("post-1", "a", null);
        post.visible = Visibility.PUBLIC;
        insert(index, post);

        var restored = new SingleValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.equal(Visibility.PUBLIC)).containsExactly("post-1");
        assertThat(restored.getKey("post-1")).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void multiValueIndexShouldRoundTrip() {
        var spec = (MultiValueIndexSpec<FakePost, String>) IndexSpecs.<FakePost, String>multi("spec.tags", String.class)
                .indexFunc(p -> p.tags)
                .build();
        var index = new MultiValueIndex<>(spec);
        var post = new FakePost("post-1", "a", null);
        post.tags = Set.of("java", "halo");
        insert(index, post);

        var restored = new MultiValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.equal("java")).containsExactly("post-1");
        assertThat(restored.getKeys("post-1")).containsExactlyInAnyOrder("java", "halo");
    }

    @Test
    void labelIndexShouldRoundTrip() {
        var index = new LabelIndex<FakePost>();
        var post = new FakePost("post-1", "a", null);
        post.getMetadata().setLabels(Map.of("app", "halo", "env", "prod"));
        insert(index, post);

        var restored = new LabelIndex<FakePost>();
        var snapshot = index.dump();
        assertThat(snapshot.fingerprint()).isEqualTo(IndexFingerprints.LABEL_FINGERPRINT);
        restored.restore(snapshot);
        assertThat(restored.equal("app", "halo")).containsExactly("post-1");
        assertThat(restored.exists("env")).containsExactly("post-1");
    }

    @Test
    void nullKeysShouldRoundTrip() {
        var spec = (SingleValueIndexSpec<FakePost, Instant>)
                IndexSpecs.<FakePost, Instant>single("spec.publishTime", Instant.class)
                        .indexFunc(p -> p.publishTime)
                        .nullable(true)
                        .build();
        var index = new SingleValueIndex<>(spec);
        insert(index, new FakePost("post-1", "a", null));

        var restored = new SingleValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.isNull()).containsExactly("post-1");
    }

    private static <E extends Extension, K extends Comparable<K>> void insert(Index<E, K> index, E extension) {
        var operation = index.prepareInsert(extension);
        operation.prepare();
        operation.commit();
    }

    enum Visibility {
        PUBLIC,
        INTERNAL
    }

    static class FakePost implements Extension {
        private final Metadata metadata = new Metadata();
        String slug;
        Instant publishTime;
        Set<String> tags = Set.of();
        Visibility visible;

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
