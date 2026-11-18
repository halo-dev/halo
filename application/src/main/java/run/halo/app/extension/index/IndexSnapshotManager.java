package run.halo.app.extension.index;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.infra.InitializationPhase;

/** Per-resource-type disposable cache shared by Halo and plugin lifecycles. */
@Slf4j
public class IndexSnapshotManager implements ApplicationContextAware, SmartLifecycle {
    /** Also excludes the legacy whole-application checkpoint from backups. */
    public static final String CHECKPOINT_PREFIX = "/system/index-snapshot";

    private static final String TYPE_CHECKPOINT_PREFIX = CHECKPOINT_PREFIX + "s/";
    // Bump when shared index semantics change. Individual index functions use ValueIndexSpec.version.
    static final String ENGINE_VERSION = "2";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final Path directory;
    private final ExtensionStoreRepository repository;
    private final IndexSnapshotWriteBarrier writes;
    private final IndexEngine engine;
    private final ObjectProvider<SchemeManager> schemes;
    private ApplicationContext applicationContext;
    private volatile boolean ready;
    private volatile boolean running;

    public IndexSnapshotManager(
            Path workDir,
            ExtensionStoreRepository repository,
            IndexSnapshotWriteBarrier writes,
            IndexEngine engine,
            ObjectProvider<SchemeManager> schemes) {
        this.directory = workDir.resolve("indices").resolve("snapshots");
        this.repository = repository;
        this.writes = writes;
        this.engine = engine;
        this.schemes = schemes;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent started && started.getApplicationContext() == applicationContext) {
            ready = true;
        }
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // Capture remaining core types after controllers stop, but before schemes/storage are destroyed.
        return InitializationPhase.CONTROLLERS.getPhase() - 1;
    }

    @Override
    public void stop() {
        try {
            if (ready) {
                saveOnShutdown();
            }
        } finally {
            running = false;
        }
    }

    /** Called with the scheme registration lock held, before exposing the new indices. */
    public void initialize(Scheme scheme, Runnable rebuild) {
        var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
        withFrozen(prefix, () -> {
            var saved = repository.findById(checkpointName(prefix)).block(TIMEOUT);
            // Must commit before restoring OR rebuilding: a crash must never leave the old cache authorized.
            repository.deleteById(checkpointName(prefix)).block(TIMEOUT);
            if (writes.isReusable(prefix) && saved != null && restore(scheme, saved)) {
                log.info(
                        "Restored indices for type {} from clean shutdown snapshot",
                        scheme.type().getName());
            } else {
                rebuild.run();
            }
            writes.initialized(prefix);
        });
    }

    private boolean restore(Scheme scheme, ExtensionStore saved) {
        var parts = new String(saved.getData(), StandardCharsets.UTF_8).split("\n", 2);
        if (parts.length != 2 || !identity(scheme).equals(parts[0])) {
            return false;
        }
        try (var in = Files.newInputStream(snapshotFile(scheme.type()))) {
            var snapshot = IndexSnapshotCodec.read(in);
            return parts[1].equals(snapshot.checkpoint())
                    && engine.getIndicesManager().get(scheme.type()).restore(snapshot);
        } catch (RuntimeException | IOException e) {
            log.warn(
                    "Cannot restore snapshot for {}; rebuilding indices",
                    scheme.type().getName(),
                    e);
            return false;
        }
    }

    /** Keep the gate held until indices are removed, so late writers cannot escape snapshot invalidation. */
    public void unregister(Scheme scheme, Runnable remove) {
        var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
        withFrozen(prefix, () -> {
            save(scheme, prefix);
            remove.run();
        });
    }

    void saveOnShutdown() {
        var manager = schemes.getObject();
        synchronized (manager) {
            for (var scheme : manager.schemes()) {
                var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
                try {
                    withFrozen(prefix, () -> save(scheme, prefix));
                } catch (RuntimeException e) {
                    log.warn(
                            "Could not capture index snapshot for {}",
                            scheme.type().getName(),
                            e);
                }
            }
        }
    }

    private void save(Scheme scheme, String prefix) {
        if (!writes.isInitialized(prefix) || !writes.isReusable(prefix)) {
            log.info("Skipping index snapshot for {}: {}", scheme.type().getName(), writes.reason(prefix));
            return;
        }
        // Root capture may be followed by SchemeInitializer unregistration. Without intervening
        // writes the published generation is already current; do not replace its file/token again.
        if (writes.hasCheckpoint(prefix)) {
            return;
        }
        try {
            var token = UUID.randomUUID().toString();
            var data = engine.getIndicesManager().get(scheme.type()).dump();
            write(scheme.type(), new IndicesSnapshot(token, data.indices()));
            // Recheck global invalidation (e.g. a concurrent backup restoration) before publishing.
            if (!writes.isReusable(prefix)) {
                return;
            }
            // Even a timed-out save may have committed. Writers must invalidate its row.
            writes.checkpointPublicationStarted(prefix);
            // Reserve an unusable marker first. Publishing is then a version-checked UPDATE,
            // so a late completion cannot INSERT authorization after a writer deleted it.
            var reserved = repository
                    .save(new ExtensionStore(checkpointName(prefix), new byte[0]))
                    // An aborted reservation can arrive after reinitialization consumed its row.
                    // Reuse its current version; publication must still win the optimistic update.
                    .onErrorResume(DuplicateKeyException.class, error -> repository.findById(checkpointName(prefix)))
                    .block(TIMEOUT);
            if (reserved == null) {
                throw new IllegalStateException("Checkpoint reservation returned no row");
            }
            reserved.setData((identity(scheme) + "\n" + token).getBytes(StandardCharsets.UTF_8));
            repository.save(reserved).block(TIMEOUT);
            log.info(
                    "Saved clean shutdown index snapshot checkpoint for {}",
                    scheme.type().getName());
        } catch (Exception e) {
            writes.invalidate(prefix, "snapshot persistence failed");
            log.warn(
                    "Failed to save index snapshot for {}; checkpoint publication may be incomplete",
                    scheme.type().getName(),
                    e);
        }
    }

    private void withFrozen(String prefix, Runnable operation) {
        try (var ignored = writes.freeze(prefix, TIMEOUT)) {
            operation.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writes.invalidate(prefix, "interrupted while draining writes");
            throw new IllegalStateException("Interrupted index lifecycle for " + prefix, e);
        } catch (RuntimeException e) {
            writes.invalidate(prefix, "index lifecycle failed");
            throw e;
        }
    }

    private static String identity(Scheme scheme) {
        return ENGINE_VERSION + "|" + scheme.type().getName() + "|" + scheme.groupVersionKind();
    }

    static String checkpointName(String prefix) {
        try {
            return TYPE_CHECKPOINT_PREFIX
                    + HexFormat.of()
                            .formatHex(MessageDigest.getInstance("SHA-256")
                                    .digest(prefix.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void write(Class<? extends Extension> type, IndicesSnapshot snapshot) throws IOException {
        Files.createDirectories(directory);
        var file = snapshotFile(type);
        // A killed process can leave temporary files. Never reuse their paths or follow symlinks.
        try (var leftovers = Files.newDirectoryStream(directory, file.getFileName() + "*.tmp")) {
            for (var leftover : leftovers) {
                if (Files.isRegularFile(leftover, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(leftover)) {
                    try {
                        Files.deleteIfExists(leftover);
                    } catch (IOException e) {
                        log.debug("Cannot remove abandoned snapshot temporary file {}", leftover, e);
                    }
                }
            }
        } catch (IOException e) {
            log.debug("Cannot scan abandoned snapshot temporary files for {}", type.getName(), e);
        }
        var temporary = Files.createTempFile(directory, file.getFileName() + ".", ".tmp");
        try {
            try (var out = Files.newOutputStream(temporary)) {
                IndexSnapshotCodec.write(snapshot, out);
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Path snapshotFile(Class<? extends Extension> type) {
        return directory.resolve(type.getName() + ".snapshot.gz");
    }
}
