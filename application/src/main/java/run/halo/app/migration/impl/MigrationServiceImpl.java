package run.halo.app.migration.impl;

import static org.springframework.core.io.buffer.DataBufferUtils.releaseConsumer;
import static run.halo.app.infra.utils.FileUtils.closeQuietly;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.migration.Backup;
import run.halo.app.migration.MigrationService;

@Slf4j
@Service
public class MigrationServiceImpl implements MigrationService {

    private final ExtensionStoreRepository repository;

    private final HaloProperties haloProperties;

    private final ObjectMapper objectMapper;

    private final Set<String> excludes = Set.of(
        "**/.git/**",
        "**/node_modules/**",
        "backups/**",
        "db/**",
        "logs/**",
        "docker-compose.yaml",
        "docker-compose.yml",
        "mysql/**",
        "mysqlBackup/**",
        "**/.idea/**",
        "**/.vscode/**"
    );

    private final DateTimeFormatter dateTimeFormatter;

    public MigrationServiceImpl(ExtensionStoreRepository repository,
        HaloProperties haloProperties) {
        this.repository = repository;
        this.haloProperties = haloProperties;
        this.objectMapper = JsonMapper.builder()
            .defaultPrettyPrinter(new MinimalPrettyPrinter())
            .build();
        this.dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());
    }

    DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    Path getBackupsRoot() {
        return haloProperties.getWorkDir().resolve("backups");
    }

    @Override
    public Mono<Void> backup(Backup backup) {
        try {
            // create temporary folder to store all backup files into single files.
            var tempDir = Files.createTempDirectory("halo-full-backup-");
            return backupExtensions(tempDir)
                .and(backupWorkDir(tempDir))
                .and(packageBackup(tempDir, backup))
                .doFinally(signalType -> deleteRecursivelyAndSilently(tempDir))
                .subscribeOn(Schedulers.boundedElastic());
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public Mono<Resource> download(Backup backup) {
        var status = backup.getStatus();
        if (!Backup.Phase.SUCCEEDED.equals(status.getPhase()) || status.getFilename() == null) {
            return Mono.error(new ServerWebInputException("Current backup is not downloadable."));
        }

        var backupFile = getBackupsRoot().resolve(status.getFilename());
        var resource = new FileSystemResource(backupFile);
        if (!resource.exists()) {
            return Mono.error(new NotFoundException("problemDetail.migration.backup.notFound",
                new Object[] {}, "Backup file doesn't exist or deleted."));
        }
        return Mono.just(resource);
    }

    @Override
    @Transactional
    public Mono<Void> restore(Publisher<DataBuffer> content) {
        return Mono.defer(() -> {
            try {
                var tempDir = Files.createTempDirectory("halo-restore-");
                return unpackBackup(content, tempDir)
                    .and(restoreExtensions(tempDir))
                    .and(restoreWorkdir(tempDir))
                    .doFinally(signalType -> deleteRecursivelyAndSilently(tempDir))
                    .subscribeOn(Schedulers.boundedElastic());
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> cleanup(Backup backup) {
        return Mono.<Void>fromRunnable(() -> {
            var status = backup.getStatus();
            if (status == null || status.getFilename() == null) {
                return;
            }
            var filename = status.getFilename();
            var backupsRoot = getBackupsRoot();
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
        return Mono.fromRunnable(() -> {
            try {
                var workdir = backupRoot.resolve("workdir");
                if (Files.exists(workdir)) {
                    FileSystemUtils.copyRecursively(workdir, haloProperties.getWorkDir());
                }
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private Mono<Void> restoreExtensions(Path backupRoot) {
        var extensionsPath = backupRoot.resolve("extensions.data");
        var reader = objectMapper.readerFor(ExtensionStore.class);
        return Mono.<Void, MappingIterator<ExtensionStore>>using(
            () -> reader.readValues(extensionsPath.toFile()),
            itr -> Flux.<ExtensionStore>create(
                    sink -> {
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
                        () -> closeQuietly(pipedOs),
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
                var backupsFolder = getBackupsRoot();
                Files.createDirectories(backupsFolder);
                var backupName = backup.getMetadata().getName();
                var startTimestamp = backup.getStatus().getStartTimestamp();
                var timePart = this.dateTimeFormatter.format(startTimestamp);
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
                FileUtils.copyRecursively(haloProperties.getWorkDir(), workdirPath, excludes);
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private Mono<Void> backupExtensions(Path baseDir) {
        try {
            var extensionsPath = Files.createFile(baseDir.resolve("extensions.data"));
            return Mono.using(() -> objectMapper.writerFor(ExtensionStore.class)
                    .writeValuesAsArray(extensionsPath.toFile()),
                seqWriter -> repository.findAll()
                    .doOnNext(extensionStore -> {
                        try {
                            seqWriter.write(extensionStore);
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    })
                    .then(),
                seqWriter -> {
                    try {
                        seqWriter.close();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
