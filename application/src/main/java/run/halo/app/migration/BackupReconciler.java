package run.halo.app.migration;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.controller.Reconciler.Result.doNotRetry;
import static run.halo.app.migration.Constant.HOUSE_KEEPER_FINALIZER;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.migration.Backup.Phase;

@Slf4j
@Component
public class BackupReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final MigrationService migrationService;

    private Clock clock;

    public BackupReconciler(ExtensionClient client, MigrationService migrationService) {
        this.client = client;
        this.migrationService = migrationService;
        clock = Clock.systemDefaultZone();
    }

    /**
     * Set clock. The method is only for unit test.
     *
     * @param clock is new clock
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Backup.class, request.name())
            .map(backup -> {
                var metadata = backup.getMetadata();
                var status = backup.getStatus();
                var spec = backup.getSpec();
                if (isDeleted(backup)) {
                    if (removeFinalizers(metadata, Set.of(HOUSE_KEEPER_FINALIZER))) {
                        migrationService.cleanup(backup).block();
                        client.update(backup);
                    }
                    return doNotRetry();
                }
                if (addFinalizers(metadata, Set.of(HOUSE_KEEPER_FINALIZER))) {
                    client.update(backup);
                }

                if (Phase.PENDING.equals(status.getPhase())) {
                    // Do backup
                    try {
                        status.setPhase(Phase.RUNNING);
                        status.setStartTimestamp(Instant.now(clock));
                        updateStatus(request.name(), status);
                        // Long period execution when backing up
                        migrationService.backup(backup).block();
                        status.setPhase(Phase.SUCCEEDED);
                        status.setCompletionTimestamp(Instant.now(clock));
                        updateStatus(request.name(), status);
                    } catch (Throwable t) {
                        var unwrapped = Exceptions.unwrap(t);
                        log.error("Failed to backup", unwrapped);
                        // Only happen when shutting down
                        status.setPhase(Phase.FAILED);
                        if (unwrapped instanceof InterruptedException) {
                            status.setFailureReason("Interrupted");
                            status.setFailureMessage("The backup process was interrupted.");
                        } else {
                            status.setFailureReason("SystemError");
                            status.setFailureMessage(
                                "Something went wrong! Error message: " + unwrapped.getMessage());
                        }
                        updateStatus(request.name(), status);
                    }
                }
                // Only happen when failing to update status when interrupted
                if (Phase.RUNNING.equals(status.getPhase())) {
                    status.setPhase(Phase.FAILED);
                    status.setFailureReason("UnexpectedExit");
                    status.setFailureMessage("The backup process may exit abnormally.");
                    updateStatus(request.name(), status);
                }
                // Check the expires at and requeue if necessary
                if (isTerminal(status.getPhase())) {
                    var expiresAt = spec.getExpiresAt();
                    if (expiresAt != null) {
                        var now = Instant.now(clock);
                        if (now.isBefore(expiresAt)) {
                            return new Result(true, Duration.between(now, expiresAt));
                        }
                        client.delete(backup);
                    }
                }
                return doNotRetry();
            }).orElseGet(Result::doNotRetry);
    }

    private void updateStatus(String name, Backup.Status status) {
        client.fetch(Backup.class, name)
            .ifPresent(backup -> {
                backup.setStatus(status);
                client.update(backup);
            });
    }

    private static boolean isTerminal(Phase phase) {
        return Phase.FAILED.equals(phase) || Phase.SUCCEEDED.equals(phase);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Backup())
            .build();
    }
}
