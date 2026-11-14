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
    void dumpAndRestoreShouldRoundTrip() {
        var indices = newIndices();
        var post = new FakePost("post-1", "hello", null);
        post.getMetadata().setVersion(5L);
        post.tags = Set.of("java");
        indices.insert(post);

        var snapshot = indices.dump();
        assertThat(snapshot.versions()).containsEntry("post-1", 5L);

        var restored = newIndices();
        restored.restore(snapshot);
        assertThat(valueIndex(restored, "spec.slug").equal("hello")).containsExactly("post-1");
        assertThat(valueIndex(restored, "spec.tags").equal("java")).containsExactly("post-1");
        assertThat(restored.dump().versions()).containsEntry("post-1", 5L);
    }

    @Test
    void staleUpdateShouldBeSkippedByVersionGuard() {
        var indices = newIndices();
        var v2 = new FakePost("post-1", "new", null);
        v2.getMetadata().setVersion(2L);
        indices.insert(v2);

        var v1 = new FakePost("post-1", "old", null);
        v1.getMetadata().setVersion(1L);
        indices.update(v1);

        assertThat(valueIndex(indices, "spec.slug").equal("new")).containsExactly("post-1");
        assertThat(valueIndex(indices, "spec.slug").equal("old")).isEmpty();
    }

    @Test
    void staleInsertShouldBeSkippedByVersionGuard() {
        var indices = newIndices();
        var v2 = new FakePost("post-1", "new", null);
        v2.getMetadata().setVersion(2L);
        indices.update(v2);

        // a deferred insert with an older version must not resurrect stale content
        var v1 = new FakePost("post-1", "old", null);
        v1.getMetadata().setVersion(1L);
        indices.insert(v1);

        assertThat(valueIndex(indices, "spec.slug").equal("new")).containsExactly("post-1");
        assertThat(valueIndex(indices, "spec.slug").equal("old")).isEmpty();
        assertThat(indices.dump().versions()).containsEntry("post-1", 2L);
    }

    @Test
    void concurrentUpdatesShouldKeepHighestVersion() throws InterruptedException {
        var indices = newIndices();
        var threadCount = 16;
        var ready = new java.util.concurrent.CountDownLatch(threadCount);
        var start = new java.util.concurrent.CountDownLatch(1);
        var done = new java.util.concurrent.CountDownLatch(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            var version = (long) i;
            var thread = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                var post = new FakePost("post-1", "v" + version, null);
                post.getMetadata().setVersion(version);
                indices.update(post);
                done.countDown();
            });
            thread.start();
        }
        assertThat(ready.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

        // the highest version must win no matter in which order the updates committed
        assertThat(valueIndex(indices, "spec.slug").equal("v" + threadCount)).containsExactly("post-1");
        assertThat(indices.dump().versions()).containsEntry("post-1", (long) threadCount);
    }

    @Test
    void deleteByNameShouldRemoveAllIndexEntries() {
        var indices = newIndices();
        var post = new FakePost("post-1", "hello", null);
        post.getMetadata().setVersion(1L);
        post.tags = Set.of("java");
        indices.insert(post);

        indices.deleteByName("post-1");

        assertThat(valueIndex(indices, "spec.slug").equal("hello")).isEmpty();
        assertThat(valueIndex(indices, "spec.tags").equal("java")).isEmpty();
        assertThat(indices.dump().versions()).doesNotContainKey("post-1");
    }

    @Test
    void updateIndicesShouldOnlyTouchNamedIndices() {
        var indices = newIndices();
        var post = new FakePost("post-1", "hello", null);
        post.getMetadata().setVersion(1L);
        indices.updateIndices(post, Set.of("spec.slug"));

        assertThat(valueIndex(indices, "spec.slug").equal("hello")).containsExactly("post-1");
        assertThat(indices.dump().versions()).containsEntry("post-1", 1L);
        // tags index was not updated
        post.tags = Set.of("java");
        indices.updateIndices(post, Set.of("spec.tags"));
        assertThat(valueIndex(indices, "spec.tags").equal("java")).containsExactly("post-1");
    }

    @Test
    void currentFingerprintsShouldCoverAllIndices() {
        var indices = newIndices();
        assertThat(indices.currentFingerprints()).containsKeys("spec.slug", "spec.tags", "metadata.labels");
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
