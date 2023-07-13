package run.halo.app.migration.impl;

import static com.fasterxml.jackson.core.JsonEncoding.UTF8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.core.io.buffer.DataBufferUtils.releaseConsumer;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.PathMatcher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.migration.Backup;
import run.halo.app.migration.MigrationService;

@Slf4j
@Service
public class MigrationServiceImpl implements MigrationService {

    private final ExtensionStoreRepository repository;

    private PathMatcher pathMatcher;

    private final HaloProperties haloProperties;

    private final Set<String> excludes = Set.of(
        "**/.git/**",
        "**/node_modules/**",
        "backups/**",
        "db/**",
        "logs/**",
        "**/.idea/**",
        "**/.vscode/**"
    );

    public MigrationServiceImpl(ExtensionStoreRepository repository,
        HaloProperties haloProperties) {
        this.repository = repository;
        this.haloProperties = haloProperties;
        this.pathMatcher = new AntPathMatcher();
    }

    /**
     * This method is only for testing.
     *
     * @param pathMatcher new path matcher.
     */
    void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    // @Transactional(readOnly = true)
    public Mono<Void> backup(Backup backup) {
        try {
            // create temporary folder to store all backup files into single files.
            var tempDir = Files.createTempDirectory("halo-full-backup-");
            return backupExtensions(tempDir)
                .and(backupWorkDir(tempDir))
                .and(packageBackup(tempDir, backup))
                .doFinally(signalType -> {
                    try {
                        deleteRecursively(tempDir);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    @Transactional
    public Mono<Void> restore(Publisher<DataBuffer> content) {
        try {
            var tempDir = Files.createTempDirectory("halo-restore-");
            return unpackBackup(content, tempDir)
                .and(restoreExtensions(tempDir))
                .and(restoreWorkdir(tempDir))
                // check the integration
                .doFinally(signalType -> FileUtils.deleteRecursivelyAndSilently(tempDir))
                .subscribeOn(Schedulers.boundedElastic());
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public Mono<Void> cleanup(Backup backup) {
        return Mono.<Void>fromRunnable(() -> {
            var status = backup.getStatus();
            if (status == null || status.getFilename() == null) {
                return;
            }
            var filename = status.getFilename();
            var backupsRoot = haloProperties.getWorkDir().resolve("backups");
            var backupFile = backupsRoot.resolve(filename);
            try {
                FileUtils.checkDirectoryTraversal(backupsRoot, backupFile);
                Files.deleteIfExists(backupFile);
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> restoreWorkdir(Path backupRoot) {
        var workdir = backupRoot.resolve("workdir");
        return Mono.fromRunnable(() -> {
            try {
                FileSystemUtils.copyRecursively(workdir, haloProperties.getWorkDir());
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private Mono<Void> restoreExtensions(Path backupRoot) {
        var extensionsPath = backupRoot.resolve("extensions.data");
        var jsonMapper = JsonMapper.builder()
            .defaultPrettyPrinter(new MinimalPrettyPrinter())
            .build();
        var reader = jsonMapper.readerFor(ExtensionStore.class);
        return Mono.<Void, MappingIterator<ExtensionStore>>using(
            () -> reader.readValues(extensionsPath.toFile()),
            itr -> Flux.<ExtensionStore>create(sink -> {
                    while (itr.hasNext()) {
                        sink.next(itr.next());
                    }
                    sink.complete();
                })
                // reset version
                .doOnNext(extensionStore -> extensionStore.setVersion(null))
                .buffer(100)
                // We might encounter OptimisticLockingFailureException when saving extension store,
                // So we have to delete all extension stores before saving.
                .flatMap(extensionStores -> repository.deleteAll(extensionStores)
                    .thenMany(repository.saveAll(extensionStores)))
                .doOnNext(extensionStore ->
                    log.info("Restored extension store: {}", extensionStore.getName()))
                .then(),
            itr -> {
                try {
                    itr.close();
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            });
    }

    private Mono<Void> unpackBackup(Publisher<DataBuffer> content, Path target) {
        return Mono.create(sink -> {
            try (var pipedIs = new PipedInputStream();
                 var pipedOs = new PipedOutputStream(pipedIs);
                 var zipIs = new ZipInputStream(pipedIs)) {
                DataBufferUtils.write(content, pipedOs)
                    .subscribe(
                        releaseConsumer(),
                        sink::error,
                        null,
                        Context.of(sink.contextView()));
                FileUtils.unzip(zipIs, target);
                sink.success();
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

    private Mono<Void> packageBackup(Path baseDir, Backup backup) {
        return Mono.fromRunnable(() -> {
            try {
                var backupsFolder = haloProperties.getWorkDir().resolve("backups");
                Files.createDirectories(backupsFolder);
                var backupName = backup.getMetadata().getName();
                var startTimestamp = backup.getStatus().getStartTimestamp();
                var dateTimeFormatter = DateTimeFormatter
                    .ofPattern("yyyyMMddHHmmss")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());
                var timePart = dateTimeFormatter.format(startTimestamp);
                var backupFile = backupsFolder.resolve(timePart + '-' + backupName + ".zip");
                FileUtils.zip(baseDir, backupFile);
                backup.getStatus().setFilename(backupFile.getFileName().toString());
                backup.getStatus().setSize(Files.size(backupFile));
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private Mono<Void> backupWorkDir(Path baseDir) {
        return Mono.fromRunnable(() -> {
            try {
                var workdirPath = Files.createDirectory(baseDir.resolve("workdir"));
                copyRecursively(haloProperties.getWorkDir(), workdirPath);
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private boolean shouldExclude(Path path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Path must be relative.");
        }
        return excludes.stream().anyMatch(pattern -> pathMatcher.match(pattern, path.toString()));
    }

    private void copyRecursively(Path src, Path target) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                if (!shouldExclude(src.relativize(file))) {
                    Files.copy(file, target.resolve(src.relativize(file)), REPLACE_EXISTING);
                }
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
                if (shouldExclude(src.relativize(dir))) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Files.createDirectories(target.resolve(src.relativize(dir)));
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    private Mono<Void> backupExtensions(Path baseDir) {
        var jsonMapper = JsonMapper.builder()
            .defaultPrettyPrinter(new MinimalPrettyPrinter())
            .build();
        try {
            var extensionsPath = Files.createFile(baseDir.resolve("extensions.data"));
            return Mono.using(() -> jsonMapper.createGenerator(extensionsPath.toFile(), UTF8),
                gen -> Mono.create(sink -> repository.findAll()
                    .doFirst(() -> {
                        try {
                            gen.writeStartArray();
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    })
                    .doOnNext(extensionStore -> {
                        try {
                            gen.writeObject(extensionStore);
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    })
                    .doOnComplete(() -> {
                        try {
                            gen.writeEndArray();
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    })
                    .subscribe(null, sink::error, sink::success,
                        Context.of(sink.contextView()))),
                jsonGenerator -> {
                    try {
                        jsonGenerator.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
