package run.halo.app.content;

import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Snapshot;

public interface SnapshotService {

    Mono<Snapshot> getBy(String snapshotName);

    Mono<Snapshot> getPatchedBy(String snapshotName, String baseSnapshotName);

    Mono<Snapshot> patchAndCreate(Snapshot snapshot, @Nullable Snapshot baseSnapshot, Content content);

    Mono<Snapshot> patchAndUpdate(Snapshot snapshot, Snapshot baseSnapshot, Content content);
}
