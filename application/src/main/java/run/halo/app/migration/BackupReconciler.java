package run.halo.app.migration;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.controller.Reconciler.Result.doNotRetry;
import static run.halo.app.migration.Constant.HOUSE_KEEPER_FINALIZER;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Consumer;
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
                        migrationService.cleanup(backup).block();
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

                client.update(backup);

                if (Phase.PENDING.equals(status.getPhase())) {
                    // Do backup
                    try {
                        migrationService.backup(backup)
                            .doFirst(() -> updateStatus(request.name(), s -> {
                                s.setPhase(Phase.RUNNING);
                                s.setStartTimestamp(Instant.now());
                            }))
                            .doOnError(t -> updateStatus(request.name(), s -> {
                                s.setPhase(Phase.FAILED);
                                s.setFailureReason("SystemError");
                                s.setFailureMessage(t.getMessage());
                            }))
                            .doOnSuccess(v -> updateStatus(request.name(), s -> {
                                var latestStatus = backup.getStatus();
                                s.setPhase(Phase.SUCCEEDED);
                                s.setFilename(latestStatus.getFilename());
                                s.setSize(latestStatus.getSize());
                                s.setCompletionTimestamp(Instant.now());
                            }))
                            .block();
                    } catch (Throwable t) {
                        log.error("Failed to backup", t);
                    }
                }

                client.update(backup);
                return doNotRetry();
            }).orElseGet(Result::doNotRetry);
    }

    private void updateStatus(String name, Consumer<Backup.Status> statusConsumer) {
        client.fetch(Backup.class, name)
            .ifPresent(backup -> {
                statusConsumer.accept(backup.getStatus());
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
