package run.halo.app.content.impl;

import java.security.Principal;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * A default implementation of {@link ContentService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ContentServiceImpl implements ContentService {
    private static final Comparator<Snapshot> SNAPSHOT_COMPARATOR =
        Comparator.comparing(snapshot -> snapshot.getSpec().getVersion());
    public static Comparator<Snapshot> LATEST_SNAPSHOT_COMPARATOR = SNAPSHOT_COMPARATOR.reversed();

    private final ReactiveExtensionClient client;

    public ContentServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<ContentWrapper> getContent(String name) {
        return client.fetch(Snapshot.class, name)
            .flatMap(snapshot -> getBaseSnapshot(snapshot.getSpec().getSubjectRef())
                .map(snapshot::applyPatch));
    }

    @Override
    public Mono<ContentWrapper> draftContent(ContentRequest contentRequest) {
        return getContextUsername()
            .flatMap(username -> {
                // create snapshot
                Snapshot snapshot = contentRequest.toSnapshot();
                snapshot.addContributor(username);
                return client.create(snapshot)
                    .flatMap(this::restoredContent);
            });
    }

    @Override
    public Mono<ContentWrapper> updateContent(ContentRequest contentRequest) {
        Assert.notNull(contentRequest, "The contentRequest must not be null");
        Assert.notNull(contentRequest.headSnapshotName(), "The headSnapshotName must not be null");
        return Mono.zip(getContextUsername(),
                client.fetch(Snapshot.class, contentRequest.headSnapshotName()))
            .flatMap(tuple -> {
                String username = tuple.getT1();
                Snapshot headSnapShot = tuple.getT2();
                return handleSnapshot(headSnapShot, contentRequest, username);
            })
            .flatMap(snapshot -> restoredContent(snapshot)
                .map(content -> new ContentWrapper(snapshot.getMetadata().getName(),
                    content.raw(), content.content(), content.rawType())
                )
            );
    }

    @Override
    public Mono<ContentWrapper> publish(String headSnapshotName, Snapshot.SubjectRef subjectRef) {
        Assert.notNull(headSnapshotName, "The headSnapshotName must not be null");
        return client.fetch(Snapshot.class, headSnapshotName)
            .flatMap(snapshot -> {
                if (snapshot.isPublished()) {
                    // there is nothing to publish
                    return restoredContent(snapshot.getMetadata().getName(),
                        subjectRef);
                }

                Snapshot.SnapShotSpec snapshotSpec = snapshot.getSpec();
                snapshotSpec.setPublishTime(Instant.now());
                snapshotSpec.setDisplayVersion(
                    Snapshot.displayVersionFrom(snapshotSpec.getVersion()));
                return client.update(snapshot)
                    .then(Mono.defer(
                        () -> restoredContent(snapshot.getMetadata().getName(), subjectRef))
                    );
            });
    }

    private Mono<ContentWrapper> restoredContent(String snapshotName,
        Snapshot.SubjectRef subjectRef) {
        return getBaseSnapshot(subjectRef)
            .flatMap(baseSnapshot -> client.fetch(Snapshot.class, snapshotName)
                .map(snapshot -> snapshot.applyPatch(baseSnapshot)));
    }

    private Mono<ContentWrapper> restoredContent(Snapshot headSnapshot) {
        return getBaseSnapshot(headSnapshot.getSpec().getSubjectRef())
            .map(headSnapshot::applyPatch);
    }

    @Override
    public Mono<Snapshot> getBaseSnapshot(Snapshot.SubjectRef subjectRef) {
        return listSnapshots(subjectRef)
            .filter(snapshot -> snapshot.getSpec().getVersion() == 1)
            .next();
    }

    private Mono<Snapshot> handleSnapshot(Snapshot headSnapshot, ContentRequest contentRequest,
        String username) {
        Snapshot.SubjectRef subjectRef = contentRequest.subjectRef();
        return getBaseSnapshot(subjectRef).flatMap(baseSnapshot -> {
            String baseSnapshotName = baseSnapshot.getMetadata().getName();
            return latestPublishedSnapshot(subjectRef)
                .flatMap(latestReleasedSnapshot -> {
                    Snapshot newSnapshot = contentRequest.toSnapshot();
                    newSnapshot.getSpec().setSubjectRef(subjectRef);
                    newSnapshot.addContributor(username);
                    // has released snapshot, there are 3 assumptions:
                    // if headPtr != releasePtr && head is not published, then update its content
                    // directly
                    // if headPtr != releasePtr && head is published, then create a new snapshot
                    // if headPtr == releasePtr, then create a new snapshot too
                    return latestSnapshotVersion(subjectRef)
                        .flatMap(latestSnapshot -> {
                            String headSnapshotName = contentRequest.headSnapshotName();
                            newSnapshot.getSpec()
                                .setVersion(latestSnapshot.getSpec().getVersion() + 1);
                            newSnapshot.getSpec().setDisplayVersion(
                                Snapshot.displayVersionFrom(newSnapshot.getSpec().getVersion()));
                            newSnapshot.getSpec()
                                .setParentSnapshotName(headSnapshotName);
                            // head is published or headPtr == releasePtr
                            String releasedSnapshotName =
                                latestReleasedSnapshot.getMetadata().getName();
                            if (headSnapshot.isPublished() || StringUtils.equals(headSnapshotName,
                                releasedSnapshotName)) {
                                // create a new snapshot,done
                                return createNewSnapshot(newSnapshot, baseSnapshotName,
                                    contentRequest);
                            }

                            // otherwise update its content directly
                            return updateRawAndContentToHeadSnapshot(headSnapshot, baseSnapshotName,
                                contentRequest);
                        });
                })
                // no released snapshot, indicating v1 now, just update the content directly
                .switchIfEmpty(Mono.defer(
                    () -> updateRawAndContentToHeadSnapshot(headSnapshot, baseSnapshotName,
                        contentRequest)));
        });
    }

    @Override
    public Mono<Snapshot> latestSnapshotVersion(Snapshot.SubjectRef subjectRef) {
        Assert.notNull(subjectRef, "The subjectRef must not be null.");
        return listSnapshots(subjectRef)
            .sort(LATEST_SNAPSHOT_COMPARATOR)
            .next();
    }

    @Override
    public Mono<Snapshot> latestPublishedSnapshot(Snapshot.SubjectRef subjectRef) {
        Assert.notNull(subjectRef, "The subjectRef must not be null.");
        return listSnapshots(subjectRef)
            .filter(Snapshot::isPublished)
            .sort(LATEST_SNAPSHOT_COMPARATOR)
            .next();
    }

    @Override
    public Flux<Snapshot> listSnapshots(Snapshot.SubjectRef subjectRef) {
        Assert.notNull(subjectRef, "The subjectRef must not be null.");
        return client.list(Snapshot.class, snapshot -> subjectRef.equals(snapshot.getSpec()
            .getSubjectRef()), null);
    }

    private Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }

    private Mono<Snapshot> updateRawAndContentToHeadSnapshot(Snapshot snapshotToUpdate,
        String baseSnapshotName,
        ContentRequest contentRequest) {
        return client.fetch(Snapshot.class, baseSnapshotName)
            .flatMap(baseSnapshot -> {
                determineRawAndContentPatch(snapshotToUpdate,
                    baseSnapshot, contentRequest);
                return client.update(snapshotToUpdate)
                    .thenReturn(snapshotToUpdate);
            });
    }

    private Mono<Snapshot> createNewSnapshot(Snapshot snapshotToCreate,
        String baseSnapshotName,
        ContentRequest contentRequest) {
        return client.fetch(Snapshot.class, baseSnapshotName)
            .flatMap(baseSnapshot -> {
                determineRawAndContentPatch(snapshotToCreate,
                    baseSnapshot, contentRequest);
                snapshotToCreate.getMetadata().setName(UUID.randomUUID().toString());
                snapshotToCreate.getSpec().setSubjectRef(contentRequest.subjectRef());
                return client.create(snapshotToCreate)
                    .thenReturn(snapshotToCreate);
            });
    }

    private void determineRawAndContentPatch(Snapshot snapshotToUse, Snapshot baseSnapshot,
        ContentRequest contentRequest) {
        String originalRaw = baseSnapshot.getSpec().getRawPatch();
        String originalContent = baseSnapshot.getSpec().getContentPatch();

        // it is the v1 snapshot, set the content directly
        if (snapshotToUse.getSpec().getVersion() == 1) {
            snapshotToUse.getSpec().setRawPatch(contentRequest.raw());
            snapshotToUse.getSpec().setContentPatch(contentRequest.content());
        } else {
            // otherwise diff a patch based on the v1 snapshot
            String revisedRaw = contentRequest.rawPatchFrom(originalRaw);
            String revisedContent = contentRequest.contentPatchFrom(originalContent);
            snapshotToUse.getSpec().setRawPatch(revisedRaw);
            snapshotToUse.getSpec().setContentPatch(revisedContent);
        }
    }
}
