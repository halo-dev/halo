package run.halo.app.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;

@ExtendWith(MockitoExtension.class)
class BackupReconcilerTest {

    @Mock
    MigrationService migrationService;

    @Mock
    ExtensionClient client;

    @InjectMocks
    BackupReconciler reconciler;

    @Test
    void whenFreshBackupIsComing() {
        var name = "fake-backup";
        var backup = createPureBackup(name);
        backup.getSpec().setFormat("zip");
        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));
        doNothing().when(client).update(backup);
        when(migrationService.backup(backup)).thenReturn(Mono.fromRunnable(() -> {
            var status = backup.getStatus();
            status.setFilename("fake-backup-filename");
            status.setSize(1024L);
        }));

        var result = reconciler.reconcile(new Reconciler.Request(name));

        assertNotNull(result);
        assertFalse(result.reEnqueue());

        var status = backup.getStatus();
        assertEquals(Backup.Phase.SUCCEEDED, status.getPhase());
        assertNotNull(status.getStartTimestamp());
        assertNotNull(status.getCompletionTimestamp());
        assertEquals("fake-backup-filename", status.getFilename());
        assertEquals(1024L, status.getSize());

        // 1. query
        // 2. pending -> running
        // 3. running -> succeeded
        verify(client, times(3)).fetch(Backup.class, name);
        verify(client, times(3)).update(backup);
        verify(migrationService).backup(backup);
    }

    @Test
    void whenBackupDeleted() {
        var name = "fake-deleted-backup";
        var backup = createPureBackup(name);
        backup.getMetadata().setDeletionTimestamp(Instant.now());
        addFinalizers(backup.getMetadata(), Set.of(Constant.HOUSE_KEEPER_FINALIZER));

        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));
        when(migrationService.cleanup(backup)).thenReturn(Mono.empty());
        doNothing().when(client).update(backup);

        var result = reconciler.reconcile(new Reconciler.Request(name));

        assertNotNull(result);
        assertFalse(result.reEnqueue());

        assertFalse(backup.getMetadata().getFinalizers().contains(Constant.HOUSE_KEEPER_FINALIZER));
        verify(client).fetch(Backup.class, name);
        verify(migrationService).cleanup(backup);
        verify(client).update(backup);
    }

    @Test
    void setPhaseToFailedIfPhaseIsRunning() {
        var name = "fake-backup";
        var backup = createPureBackup(name);
        var status = backup.getStatus();
        status.setPhase(Backup.Phase.RUNNING);

        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));
        doNothing().when(client).update(backup);

        var result = reconciler.reconcile(new Reconciler.Request(name));
        assertNotNull(result);
        assertFalse(result.reEnqueue());

        assertEquals(Backup.Phase.FAILED, status.getPhase());
        assertEquals("UnexpectedExit", status.getFailureReason());
        // 1. add finalizer
        // 2. update status
        verify(client, times(2)).fetch(Backup.class, name);
        verify(client, times(2)).update(backup);
    }

    @Test
    void shouldReQueueIfExpiresAtSetAndNotExpired() {
        var now = Instant.now();
        reconciler.setClock(Clock.fixed(now, ZoneId.systemDefault()));
        var name = "fake-backup";
        var backup = createPureBackup(name);
        addFinalizers(backup.getMetadata(), Set.of(Constant.HOUSE_KEEPER_FINALIZER));
        backup.getSpec().setExpiresAt(now.plus(Duration.ofSeconds(3)));
        var status = backup.getStatus();
        status.setPhase(Backup.Phase.SUCCEEDED);

        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));

        var result = reconciler.reconcile(new Reconciler.Request(name));
        assertNotNull(result);
        assertTrue(result.reEnqueue());
        assertEquals(Duration.ofSeconds(3), result.retryAfter());

        verify(client).fetch(Backup.class, name);
        verify(client, never()).update(backup);
        verify(client, never()).delete(backup);
    }

    @Test
    void shouldDeleteIfExpiresAtSetAndExpired() {
        var now = Instant.now();
        reconciler.setClock(Clock.fixed(now, ZoneId.systemDefault()));
        var name = "fake-backup";
        var backup = createPureBackup(name);
        addFinalizers(backup.getMetadata(), Set.of(Constant.HOUSE_KEEPER_FINALIZER));
        backup.getSpec().setExpiresAt(now.minus(Duration.ofSeconds(3)));
        var status = backup.getStatus();
        status.setPhase(Backup.Phase.SUCCEEDED);

        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));
        doNothing().when(client).delete(backup);

        var result = reconciler.reconcile(new Reconciler.Request(name));
        assertNotNull(result);
        assertFalse(result.reEnqueue());

        verify(client).fetch(Backup.class, name);
        verify(client, never()).update(backup);
        verify(client).delete(backup);
    }

    @Test
    void whenBackupInterrupted() {
        var name = "fake-backup";
        var backup = createPureBackup(name);
        backup.getSpec().setFormat("zip");
        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));
        doNothing().when(client).update(backup);
        when(migrationService.backup(backup)).thenReturn(
            Mono.error(Exceptions.propagate(new InterruptedException())));

        var result = reconciler.reconcile(new Reconciler.Request(name));

        assertNotNull(result);
        assertFalse(result.reEnqueue());

        var status = backup.getStatus();
        assertEquals(Backup.Phase.FAILED, status.getPhase());
        assertNotNull(status.getStartTimestamp());
        assertNull(status.getCompletionTimestamp());
        assertEquals("Interrupted", status.getFailureReason());

        // 1. query
        // 2. pending -> running
        // 3. running -> failed
        verify(client, times(3)).fetch(Backup.class, name);
        verify(client, times(3)).update(backup);
        verify(migrationService).backup(backup);
    }

    @Test
    void somethingWentWrongWhenBackup() {
        var name = "fake-backup";
        var backup = createPureBackup(name);
        backup.getSpec().setFormat("zip");
        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));
        doNothing().when(client).update(backup);
        when(migrationService.backup(backup))
            .thenReturn(Mono.error(Exceptions.propagate(new IOException("File not found"))));

        var result = reconciler.reconcile(new Reconciler.Request(name));

        assertNotNull(result);
        assertFalse(result.reEnqueue());

        var status = backup.getStatus();
        assertEquals(Backup.Phase.FAILED, status.getPhase());
        assertNotNull(status.getStartTimestamp());
        assertNull(status.getCompletionTimestamp());
        assertEquals("SystemError", status.getFailureReason());

        // 1. query
        // 2. pending -> running
        // 3. running -> failed
        verify(client, times(3)).fetch(Backup.class, name);
        verify(client, times(3)).update(backup);
        verify(migrationService).backup(backup);
    }

    @Test
    void whenBackupWasFailed() {
        var name = "fake-backup";
        var backup = createPureBackup(name);
        backup.getStatus().setPhase(Backup.Phase.FAILED);

        when(client.fetch(Backup.class, name)).thenReturn(Optional.of(backup));

        var result = reconciler.reconcile(new Reconciler.Request(name));
        assertNotNull(result);
        assertFalse(result.reEnqueue());
        Mockito.verify(migrationService, never()).backup(any(Backup.class));
    }

    Backup createPureBackup(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        var backup = new Backup();
        backup.setMetadata(metadata);
        return backup;
    }

}