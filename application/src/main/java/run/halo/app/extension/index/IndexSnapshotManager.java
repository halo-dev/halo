package run.halo.app.extension.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import run.halo.app.extension.Extension;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Persists and loads index snapshots under {@code <work-dir>/indices/}. All failures are logged and degraded to a full
 * index rebuild; nothing here may break startup or shutdown.
 *
 * <p>Registered explicitly as a bean in {@code ExtensionConfiguration} instead of component scanning, so that sliced
 * test contexts scanning this package do not require {@link HaloProperties}.
 *
 * @since 2.26.0
 */
@Slf4j
public class IndexSnapshotManager {

    private final Path snapshotDir;

    public IndexSnapshotManager(HaloProperties haloProperties) {
        this(haloProperties.getWorkDir().resolve("indices"));
    }

    /** Also usable in tests with an arbitrary directory. */
    public IndexSnapshotManager(Path snapshotDir) {
        this.snapshotDir = snapshotDir;
    }

    public void save(Class<? extends Extension> type, IndicesSnapshot snapshot) {
        var file = snapshotFile(type);
        var tempFile = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(snapshotDir);
            try (OutputStream out = Files.newOutputStream(tempFile)) {
                IndexSnapshotCodec.write(snapshot, out);
            }
            moveAtomically(tempFile, file);
        } catch (Exception e) {
            log.error("Failed to save index snapshot for type {}", type.getName(), e);
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
        }
    }

    public Optional<IndicesSnapshot> load(Class<? extends Extension> type) {
        var file = snapshotFile(type);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(file)) {
            return Optional.of(IndexSnapshotCodec.read(in));
        } catch (RuntimeException | IOException e) {
            log.warn(
                    "Index snapshot for type {} is corrupted ({}), falling back to full build",
                    type.getName(),
                    e.getMessage());
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
            return Optional.empty();
        }
    }

    /**
     * Some file systems (e.g. moving across stores) do not support {@link StandardCopyOption#ATOMIC_MOVE}; fall back to
     * a regular move instead of dropping the snapshot.
     */
    private static void moveAtomically(Path tempFile, Path file) throws IOException {
        try {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path snapshotFile(Class<? extends Extension> type) {
        return snapshotDir.resolve(type.getName() + ".snapshot.gz");
    }
}
