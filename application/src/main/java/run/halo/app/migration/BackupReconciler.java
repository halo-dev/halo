package run.halo.app.migration;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.controller.Reconciler.Result.doNotRetry;
import static run.halo.app.migration.Constant.HOUSE_KEEPER_FINALIZER;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

    public BackupReconciler(ExtensionClient client, MigrationService migrationService) {
        this.client = client;
        this.migrationService = migrationService;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Backup.class, request.name())
            .map(backup -> {
                var metadata = backup.getMetadata();
                var status = backup.getStatus();
                var spec = backup.getSpec();
                if (isDeleted(backup)) {
                    if (metadata.getFinalizers().contains(HOUSE_KEEPER_FINALIZER)) {
                        migrationService.cleanup(backup).block();
                        removeFinalizers(metadata, Set.of(HOUSE_KEEPER_FINALIZER));
                    }
                    client.update(backup);
                    return doNotRetry();
                }
                addFinalizers(metadata, Set.of(HOUSE_KEEPER_FINALIZER));
                if (isTerminal(status.getPhase())) {
                    var autoDeleteWhen = spec.getExpiresAt();
                    if (autoDeleteWhen != null) {
                        var now = Instant.now();
                        if (now.isBefore(autoDeleteWhen)) {
                            return new Result(true, Duration.between(autoDeleteWhen, now));
                        }
                        client.delete(backup);
                        return null;
                    }
                    return doNotRetry();
                }

                client.update(backup);

                if (Phase.PENDING.equals(status.getPhase())) {
                    // Do backup
                    try {
                        migrationService.backup(backup)
                            .doFirst(() -> {
                                status.setPhase(Phase.RUNNING);
                                status.setStartTimestamp(Instant.now());
                                updateStatus(request.name(), status);
                            })
                            .doOnError(t -> {
                                status.setPhase(Phase.FAILED);
                                status.setFailureReason("SystemError");
                                status.setFailureMessage(t.getMessage());
                                updateStatus(request.name(), status);
                            })
                            .doOnSuccess(v -> {
                                status.setPhase(Phase.SUCCEEDED);
                                status.setCompletionTimestamp(Instant.now());
                                updateStatus(request.name(), status);
                            })
                            .block();
                    } catch (Throwable t) {
                        log.error("Failed to backup", t);
                    }
                }
                if (isTerminal(status.getPhase())) {
                    // requeue for applying autoDeleteWhen
                    return new Result(true, Duration.ofSeconds(1));
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
