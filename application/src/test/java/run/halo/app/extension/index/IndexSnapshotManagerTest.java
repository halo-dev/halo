package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;

class IndexSnapshotManagerTest {

    @TempDir
    Path workDir;

    private IndexSnapshotManager newManager() {
        return new IndexSnapshotManager(workDir.resolve("indices"));
    }

    @Test
    void saveAndLoadShouldRoundTrip() {
        var manager = newManager();
        var snapshot = new IndicesSnapshot(
                List.of(new IndexSnapshot(
                        "spec.slug",
                        "fp",
                        "java.lang.String",
                        List.of(new IndexSnapshot.Entry(List.of("hello"), List.of("p1"))),
                        List.of())),
                Map.of("p1", 3L));
        manager.save(FakeType.class, snapshot);

        var loaded = manager.load(FakeType.class);

        assertThat(loaded).contains(snapshot);
        // other type has no snapshot
        assertThat(manager.load(OtherType.class)).isEmpty();
    }

    @Test
    void loadShouldReturnEmptyForCorruptedFileAndDeleteIt() throws Exception {
        var manager = newManager();
        var file = workDir.resolve("indices").resolve(FakeType.class.getName() + ".snapshot.gz");
        Files.createDirectories(file.getParent());
        Files.write(file, new byte[] {1, 2, 3, 4, 5});

        assertThat(manager.load(FakeType.class)).isEmpty();
        assertThat(file).doesNotExist();
    }

    @Test
    void loadShouldReturnEmptyForInvalidCountFileAndDeleteIt() throws Exception {
        var manager = newManager();
        var file = workDir.resolve("indices").resolve(FakeType.class.getName() + ".snapshot.gz");
        Files.createDirectories(file.getParent());
        Files.write(file, IndexSnapshotCodecTest.craftedStreamWithIndexCount(-1));

        assertThat(manager.load(FakeType.class)).isEmpty();
        assertThat(file).doesNotExist();
    }

    @Test
    void saveFailureShouldNotThrow() {
        // 指向一个不可创建的路径（文件当目录用）制造失败
        var blocker = workDir.resolve("blocker");
        try {
            Files.writeString(blocker, "x");
            var manager = new IndexSnapshotManager(blocker.resolve("indices"));
            manager.save(FakeType.class, new IndicesSnapshot(List.of(), Map.of()));
            // no exception expected
        } catch (Exception e) {
            throw new AssertionError("save should swallow IO errors", e);
        }
    }

    static class FakeType implements Extension {
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

    static class OtherType extends FakeType {}
}
