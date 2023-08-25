package run.halo.app.migration.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.infra.BackupRootGetter;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.migration.Backup;

@ExtendWith(MockitoExtension.class)
class MigrationServiceImplTest {

    @Mock
    ExtensionStoreRepository repository;

    @Mock
    HaloProperties haloProperties;

    @Mock
    BackupRootGetter backupRoot;

    @InjectMocks
    MigrationServiceImpl migrationService;

    @TempDir
    Path tempDir;

    @Test
    void backupTest() throws IOException {
        Files.writeString(tempDir.resolve("fake-file"), "halo", StandardOpenOption.CREATE_NEW);
        var extensionStores = List.of(
            createExtensionStore("fake-extension-store", "fake-data")
        );
        when(repository.findAll()).thenReturn(Flux.fromIterable(extensionStores));
        when(haloProperties.getWorkDir()).thenReturn(tempDir);
        when(backupRoot.get()).thenReturn(tempDir.resolve("backups"));
        var startTimestamp = Instant.now();
        var backup = createRunningBackup("fake-backup", startTimestamp);
        StepVerifier.create(migrationService.backup(backup))
            .verifyComplete();

        verify(repository).findAll();
        // 1. backup workdir
        // 2. package backup
        verify(haloProperties).getWorkDir();
        verify(backupRoot).get();

        var status = backup.getStatus();
        var datetimePart = migrationService.getDateTimeFormatter().format(startTimestamp);
        assertEquals(datetimePart + "-fake-backup.zip", status.getFilename());
        var backupFile = migrationService.getBackupsRoot()
            .resolve(status.getFilename());
        assertTrue(Files.exists(backupFile));
        assertEquals(Files.size(backupFile), status.getSize());

        var target = tempDir.resolve("target");
        try (var zis = new ZipInputStream(
            Files.newInputStream(backupFile, StandardOpenOption.READ))) {
            FileUtils.unzip(zis, tempDir.resolve("target"));
        }

        var extensionsFile = target.resolve("extensions.data");
        var workdir = target.resolve("workdir");
        assertTrue(Files.exists(extensionsFile));
        assertTrue(Files.exists(workdir));

        var objectMapper = migrationService.getObjectMapper();
        var gotExtensionStores = objectMapper.readValue(extensionsFile.toFile(),
            new TypeReference<List<ExtensionStore>>() {
            });
        assertEquals(gotExtensionStores, extensionStores);
        assertEquals("halo", Files.readString(workdir.resolve("fake-file")));
    }

    @Test
    void restoreTest() throws IOException, URISyntaxException {
        var unpackedBackup =
            getClass().getClassLoader().getResource("backups/backup-for-restoration");
        assertNotNull(unpackedBackup);
        var backupFile = tempDir.resolve("backups").resolve("fake-backup.zip");
        Files.createDirectories(backupFile.getParent());
        FileUtils.zip(Path.of(unpackedBackup.toURI()), backupFile);
        var workdir = tempDir.resolve("workdir-for-restoration");
        Files.createDirectory(workdir);


        var expectStore = createExtensionStore("fake-extension-store", "fake-data");
        expectStore.setVersion(null);

        when(haloProperties.getWorkDir()).thenReturn(workdir);
        when(repository.deleteAll(List.of(expectStore))).thenReturn(Mono.empty());
        when(repository.saveAll(List.of(expectStore))).thenReturn(Flux.empty());

        var content = DataBufferUtils.read(backupFile,
            DefaultDataBufferFactory.sharedInstance,
            2048,
            StandardOpenOption.READ);
        StepVerifier.create(migrationService.restore(content))
            .verifyComplete();


        verify(haloProperties).getWorkDir();
        verify(repository).deleteAll(List.of(expectStore));
        verify(repository).saveAll(List.of(expectStore));

        // make sure the workdir is recovered.
        var fakeFile = workdir.resolve("fake-file");
        assertEquals("halo", Files.readString(fakeFile));
    }

    @Test
    void cleanupBackupTest() throws IOException {
        var backupFile = tempDir.resolve("workdir").resolve("backups").resolve("backup.zip");
        Files.createDirectories(backupFile.getParent());
        Files.createFile(backupFile);

        when(backupRoot.get()).thenReturn(tempDir.resolve("workdir").resolve("backups"));
        var backup = createSucceededBackup("fake-backup", "backup.zip");
        StepVerifier.create(migrationService.cleanup(backup))
            .verifyComplete();
        verify(haloProperties, never()).getWorkDir();
        verify(backupRoot).get();
        assertTrue(Files.notExists(backupFile));
    }

    @Test
    void cleanupBackupWithNoFilename() {
        var backup = createSucceededBackup("fake-backup", null);
        StepVerifier.create(migrationService.cleanup(backup))
            .verifyComplete();
        verify(haloProperties, never()).getWorkDir();
        verify(backupRoot, never()).get();
    }

    @Test
    void downloadBackupTest() throws IOException {
        var backupFile = tempDir.resolve("workdir").resolve("backups").resolve("backup.zip");
        Files.createDirectories(backupFile.getParent());
        Files.writeString(backupFile, "this is a backup file.", StandardOpenOption.CREATE_NEW);
        when(backupRoot.get()).thenReturn(tempDir.resolve("workdir").resolve("backups"));
        var backup = createSucceededBackup("fake-backup", "backup.zip");

        StepVerifier.create(migrationService.download(backup))
            .assertNext(resource -> {
                assertEquals("backup.zip", resource.getFilename());
                try {
                    var content = resource.getContentAsString(UTF_8);
                    assertEquals("this is a backup file.", content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();

        verify(haloProperties, never()).getWorkDir();
        verify(backupRoot).get();
    }

    @Test
    void downloadBackupWhichDoesNotExist() {
        var backup = createSucceededBackup("fake-backup", "backup.zip");
        when(backupRoot.get()).thenReturn(tempDir.resolve("workdir").resolve("backups"));

        StepVerifier.create(migrationService.download(backup))
            .expectError(NotFoundException.class)
            .verify();
        verify(haloProperties, never()).getWorkDir();
        verify(backupRoot).get();
    }

    Backup createSucceededBackup(String name, String filename) {
        var metadata = new Metadata();
        metadata.setName(name);
        var backup = new Backup();
        backup.setMetadata(metadata);
        var status = backup.getStatus();
        status.setPhase(Backup.Phase.SUCCEEDED);
        status.setCompletionTimestamp(Instant.now());
        status.setFilename(filename);
        status.setSize(1024L);
        return backup;
    }

    Backup createRunningBackup(String name, Instant startTimestamp) {
        var metadata = new Metadata();
        metadata.setName(name);
        var backup = new Backup();
        backup.setMetadata(metadata);
        var status = backup.getStatus();
        status.setPhase(Backup.Phase.RUNNING);
        status.setStartTimestamp(startTimestamp);
        return backup;
    }

    ExtensionStore createExtensionStore(String name, String data) {
        var store = new ExtensionStore();
        store.setName(name);
        store.setData(data.getBytes(UTF_8));
        store.setVersion(1024L);
        return store;
    }
}