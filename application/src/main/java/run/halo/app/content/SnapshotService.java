package run.halo.app.content;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Snapshot;

public interface SnapshotService {

    Mono<Snapshot> getBy(String snapshotName);

    Mono<Snapshot> getPatchedBy(String snapshotName, String baseSnapshotName);

    Mono<Snapshot> patchAndCreate(@NonNull Snapshot snapshot,
        @Nullable Snapshot baseSnapshot,
        @NonNull Content content);

    Mono<Snapshot> patchAndUpdate(@NonNull Snapshot snapshot,
        @NonNull Snapshot baseSnapshot,
        @NonNull Content content);

}
