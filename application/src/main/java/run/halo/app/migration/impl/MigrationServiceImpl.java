package run.halo.app.migration.impl;

import static java.nio.file.Files.deleteIfExists;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;
import static run.halo.app.infra.utils.FileUtils.copyRecursively;
import static run.halo.app.infra.utils.FileUtils.createTempDir;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.infra.BackupRootGetter;
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

    private final BackupRootGetter backupRoot;

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

    private final Scheduler scheduler = Schedulers.boundedElastic();

    public MigrationServiceImpl(ExtensionStoreRepository repository,
        HaloProperties haloProperties, BackupRootGetter backupRoot) {
        this.repository = repository;
        this.haloProperties = haloProperties;
        this.backupRoot = backupRoot;
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
        return backupRoot.get();
    }

    @Override
    public Mono<Void> backup(Backup backup) {
        return Mono.usingWhen(
            createTempDir("halo-full-backup-", scheduler),
            tempDir -> backupExtensions(tempDir)
                .then(Mono.defer(() -> backupWorkDir(tempDir)))
                .then(Mono.defer(() -> packageBackup(tempDir, backup))),
            tempDir -> deleteRecursivelyAndSilently(tempDir, scheduler)
        );
    }

    @Override
    public Mono<Resource> download(Backup backup) {
        return Mono.create(sink -> {
            var status = backup.getStatus();
            if (!Backup.Phase.SUCCEEDED.equals(status.getPhase()) || status.getFilename() == null) {
                sink.error(new ServerWebInputException("Current backup is not downloadable."));
                return;
            }
            var backupFile = getBackupsRoot().resolve(status.getFilename());
            var resource = new FileSystemResource(backupFile);
            if (!resource.exists()) {
                sink.error(
                    new NotFoundException("problemDetail.migration.backup.notFound",
                        new Object[] {},
                        "Backup file doesn't exist or deleted."));
                return;
            }
            sink.success(resource);
        });
    }

    @Override
    @Transactional
    public Mono<Void> restore(Publisher<DataBuffer> content) {
        return Mono.usingWhen(
            createTempDir("halo-restore-", scheduler),
            tempDir -> unpackBackup(content, tempDir)
                .then(Mono.defer(() -> restoreExtensions(tempDir)))
                .then(Mono.defer(() -> restoreWorkdir(tempDir))),
            tempDir -> deleteRecursivelyAndSilently(tempDir, scheduler)
        );
    }

    @Override
    public Mono<Void> cleanup(Backup backup) {
        return Mono.<Void>create(sink -> {
            var status = backup.getStatus();
            if (status == null || !StringUtils.hasText(status.getFilename())) {
                sink.success();
                return;
            }
            var filename = status.getFilename();
            var backupsRoot = getBackupsRoot();
            var backupFile = backupsRoot.resolve(filename);
            try {
                checkDirectoryTraversal(backupsRoot, backupFile);
                deleteIfExists(backupFile);
                sink.success();
            } catch (IOException e) {
                sink.error(e);
            }
        }).subscribeOn(scheduler);
    }

    private Mono<Void> restoreWorkdir(Path backupRoot) {
        return Mono.<Void>create(sink -> {
            try {
                var workdir = backupRoot.resolve("workdir");
                if (Files.exists(workdir)) {
                    copyRecursively(workdir, haloProperties.getWorkDir());
                }
                sink.success();
            } catch (IOException e) {
                sink.error(e);
            }
        }).subscribeOn(scheduler);
    }

    private Mono<Void> restoreExtensions(Path backupRoot) {
        var extensionsPath = backupRoot.resolve("extensions.data");
        if (Files.notExists(extensionsPath)) {
            return Mono.empty();
        }
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
                    .doOnNext(extensionStore -> extensionStore.setVersion(null)).buffer(100)
                    // We might encounter OptimisticLockingFailureException when saving extension
                    // store,
                    // So we have to delete all extension stores before saving.
                    .flatMap(extensionStores -> repository.deleteAll(extensionStores)
                        .thenMany(repository.saveAll(extensionStores))
                    )
                    .doOnNext(extensionStore -> log.info("Restored extension store: {}",
                        extensionStore.getName()))
                    .then(),
                FileUtils::closeQuietly)
            .subscribeOn(scheduler);
    }

    private Mono<Void> unpackBackup(Publisher<DataBuffer> content, Path target) {
        return unzip(content, target, scheduler);
    }

    private Mono<Void> packageBackup(Path baseDir, Backup backup) {
        return Mono.fromCallable(
                () -> {
                    var backupsFolder = getBackupsRoot();
                    Files.createDirectories(backupsFolder);
                    return backupsFolder;
                })
            .<Void>handle((backupsFolder, sink) -> {
                var backupName = backup.getMetadata().getName();
                var startTimestamp = backup.getStatus().getStartTimestamp();
                var timePart = this.dateTimeFormatter.format(startTimestamp);
                var backupFile = backupsFolder.resolve(timePart + '-' + backupName + ".zip");
                try {
                    FileUtils.zip(baseDir, backupFile);
                    backup.getStatus().setFilename(backupFile.getFileName().toString());
                    backup.getStatus().setSize(Files.size(backupFile));
                    sink.complete();
                } catch (IOException e) {
                    sink.error(e);
                }
            })
            .subscribeOn(scheduler);
    }

    private Mono<Void> backupWorkDir(Path baseDir) {
        return Mono.fromCallable(() -> Files.createDirectory(baseDir.resolve("workdir")))
            .<Void>handle((workdirPath, sink) -> {
                try {
                    copyRecursively(haloProperties.getWorkDir(), workdirPath, excludes);
                    sink.complete();
                } catch (IOException e) {
                    sink.error(e);
                }
            })
            .subscribeOn(scheduler);
    }

    private Mono<Void> backupExtensions(Path baseDir) {
        return Mono.fromCallable(() -> Files.createFile(baseDir.resolve("extensions.data")))
            .flatMap(extensionsPath -> Mono.using(
                () -> objectMapper.writerFor(ExtensionStore.class)
                    .writeValuesAsArray(extensionsPath.toFile()),
                seqWriter -> repository.findAll()
                    .doOnNext(extensionStore -> {
                        try {
                            seqWriter.write(extensionStore);
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }).then(),
                FileUtils::closeQuietly))
            .subscribeOn(scheduler);
    }
}
