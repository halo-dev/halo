package run.halo.app.migration;

import static run.halo.app.migration.Constant.HOUSE_KEEPER_FINALIZER;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.controller.Reconciler.Result.doNotRetry;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.migration.Backup.Phase;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

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
                if (status == null) {
                    status = new Backup.Status();
                    backup.setStatus(status);
                }
                var spec = backup.getSpec();
                if (spec == null) {
                    spec = new Backup.Spec();
                    backup.setSpec(spec);
                }
                if (isDeleted(backup)) {
                    if (metadata.getFinalizers().contains(HOUSE_KEEPER_FINALIZER)) {
                        if (isTerminal(status.getPhase())) {
                            // TODO cleanup resources
                        }
                        removeFinalizers(metadata, Set.of(HOUSE_KEEPER_FINALIZER));
                    }
                    client.update(backup);
                    return doNotRetry();
                }
                addFinalizers(metadata, Set.of(HOUSE_KEEPER_FINALIZER));
                if (isTerminal(status.getPhase())) {
                    var autoDeleteWhen = spec.getAutoDeleteWhen();
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

                if (Phase.PENDING.equals(status.getPhase())) {
                    // Do backup
                    try {
                        migrationService.backup(backup)
                            .doFirst(() -> {
                                backup.getStatus().setPhase(Phase.RUNNING);
                                backup.getStatus().setStartTimestamp(Instant.now());
                            })
                            .doOnError(t -> {
                                backup.getStatus().setPhase(Phase.FAILED);
                                backup.getStatus().setFailureReason("SystemError");
                                backup.getStatus().setFailureMessage(t.getMessage());
                            })
                            .doOnSuccess(v -> backup.getStatus().setPhase(Phase.SUCCEEDED))
                            .doFinally(
                                s -> backup.getStatus().setCompletionTimestamp(Instant.now()))
                            .block();
                    } catch (Throwable t) {
                        log.error("Failed to backup", t);
                    }
                }

                client.update(backup);
                return doNotRetry();
            }).orElseGet(Result::doNotRetry);
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
