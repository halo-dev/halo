package run.halo.app.content.impl;

import java.time.Clock;
import java.util.HashMap;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.content.Content;
import run.halo.app.content.PatchUtils;
import run.halo.app.content.SnapshotService;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.ReactiveExtensionClient;

@Service
public class SnapshotServiceImpl implements SnapshotService {

    private final ReactiveExtensionClient client;

    private final Clock clock;

    public SnapshotServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public Mono<Snapshot> getBy(String snapshotName) {
        return client.get(Snapshot.class, snapshotName);
    }

    @Override
    public Mono<Snapshot> getPatchedBy(String snapshotName, String baseSnapshotName) {
        if (StringUtils.isBlank(snapshotName) || StringUtils.isBlank(baseSnapshotName)) {
            return Mono.empty();
        }

        return client.fetch(Snapshot.class, baseSnapshotName)
            .filter(Snapshot::isBaseSnapshot)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(
                "The snapshot " + baseSnapshotName + " is not a base snapshot.")))
            .flatMap(baseSnapshot ->
                Mono.defer(() -> {
                    if (Objects.equals(snapshotName, baseSnapshotName)) {
                        return Mono.just(baseSnapshot);
                    }
                    return client.fetch(Snapshot.class, snapshotName);
                }).doOnNext(snapshot -> {
                    var baseRaw = baseSnapshot.getSpec().getRawPatch();
                    var baseContent = baseSnapshot.getSpec().getContentPatch();

                    var rawPatch = snapshot.getSpec().getRawPatch();
                    var contentPatch = snapshot.getSpec().getContentPatch();

                    var annotations = snapshot.getMetadata().getAnnotations();
                    if (annotations == null) {
                        annotations = new HashMap<>();
                        snapshot.getMetadata().setAnnotations(annotations);
                    }

                    String patchedContent = baseContent;
                    String patchedRaw = baseRaw;
                    if (!Objects.equals(snapshot, baseSnapshot)) {
                        patchedContent = PatchUtils.applyPatch(baseContent, contentPatch);
                        patchedRaw = PatchUtils.applyPatch(baseRaw, rawPatch);
                    }

                    annotations.put(Snapshot.PATCHED_CONTENT_ANNO, patchedContent);
                    annotations.put(Snapshot.PATCHED_RAW_ANNO, patchedRaw);
                })
            );
    }

    @Override
    public Mono<Snapshot> patchAndCreate(@NonNull Snapshot snapshot,
        @Nullable Snapshot baseSnapshot,
        @NonNull Content content) {
        return Mono.just(snapshot)
            .doOnNext(s -> this.patch(s, baseSnapshot, content))
            .flatMap(client::create);
    }

    @Override
    public Mono<Snapshot> patchAndUpdate(@NonNull Snapshot snapshot,
        @NonNull Snapshot baseSnapshot,
        @NonNull Content content) {
        return Mono.just(snapshot)
            .doOnNext(s -> this.patch(s, baseSnapshot, content))
            .flatMap(client::update);
    }

    private void patch(@NonNull Snapshot snapshot,
        @Nullable Snapshot baseSnapshot,
        @NonNull Content content) {
        var annotations = snapshot.getMetadata().getAnnotations();
        if (annotations != null) {
            annotations.remove(Snapshot.PATCHED_CONTENT_ANNO);
            annotations.remove(Snapshot.PATCHED_RAW_ANNO);
        }
        var spec = snapshot.getSpec();
        if (spec == null) {
            spec = new Snapshot.SnapShotSpec();
        }
        spec.setRawType(content.rawType());
        if (baseSnapshot == null || Objects.equals(snapshot, baseSnapshot)) {
            // indicate the snapshot is a base snapshot
            // update raw and content directly
            spec.setRawPatch(content.raw());
            spec.setContentPatch(content.content());
        } else {
            // apply the patch and set the raw and content
            var baseSpec = baseSnapshot.getSpec();
            var baseContent = baseSpec.getContentPatch();
            var baseRaw = baseSpec.getRawPatch();

            var rawPatch = PatchUtils.diffToJsonPatch(baseRaw, content.raw());
            var contentPatch = PatchUtils.diffToJsonPatch(baseContent, content.content());
            spec.setRawPatch(rawPatch);
            spec.setContentPatch(contentPatch);
        }
        spec.setLastModifyTime(clock.instant());
    }
}
