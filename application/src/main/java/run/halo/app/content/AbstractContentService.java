package run.halo.app.content;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Abstract Service for {@link Snapshot}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractContentService {

    private final ReactiveExtensionClient client;

    public Mono<ContentWrapper> getContent(String snapshotName, String baseSnapshotName) {
        if (StringUtils.isBlank(snapshotName) || StringUtils.isBlank(baseSnapshotName)) {
            return Mono.empty();
        }
        // TODO: refactor this method to use client.get instead of fetch but please be careful
        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .flatMap(baseSnapshot -> {
                if (StringUtils.equals(snapshotName, baseSnapshotName)) {
                    var contentWrapper = ContentWrapper.patchSnapshot(baseSnapshot, baseSnapshot);
                    return Mono.just(contentWrapper);
                }
                return client.fetch(Snapshot.class, snapshotName)
                    .map(snapshot -> ContentWrapper.patchSnapshot(snapshot, baseSnapshot));
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.error("The content snapshot [{}] or base snapshot [{}] not found.",
                    snapshotName, baseSnapshotName);
                return Mono.empty();
            }));
    }

    protected void checkBaseSnapshot(Snapshot snapshot) {
        Assert.notNull(snapshot, "The snapshot must not be null.");
        if (!Snapshot.isBaseSnapshot(snapshot)) {
            throw new IllegalArgumentException(
                String.format("The snapshot [%s] is not a base snapshot.",
                    snapshot.getMetadata().getName()));
        }
    }

    protected Mono<ContentWrapper> draftContent(@Nullable String baseSnapshotName,
        ContentRequest contentRequest,
        @Nullable String parentSnapshotName) {
        return create(baseSnapshotName, contentRequest, parentSnapshotName)
            .flatMap(head -> {
                String baseSnapshotNameToUse =
                    StringUtils.defaultIfBlank(baseSnapshotName, head.getMetadata().getName());
                return restoredContent(baseSnapshotNameToUse, head);
            });
    }

    protected Mono<ContentWrapper> draftContent(String baseSnapshotName, ContentRequest content) {
        return this.draftContent(baseSnapshotName, content, content.headSnapshotName());
    }

    private Mono<Snapshot> create(@Nullable String baseSnapshotName,
        ContentRequest contentRequest,
        @Nullable String parentSnapshotName) {
        Snapshot snapshot = contentRequest.toSnapshot();
        snapshot.getMetadata().setName(UUID.randomUUID().toString());
        snapshot.getSpec().setParentSnapshotName(parentSnapshotName);

        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .defaultIfEmpty(snapshot)
            .map(baseSnapshot -> determineRawAndContentPatch(snapshot, baseSnapshot,
                contentRequest)
            )
            .flatMap(source -> getContextUsername()
                .doOnNext(username -> {
                    Snapshot.addContributor(source, username);
                    source.getSpec().setOwner(username);
                })
                .thenReturn(source)
            )
            .flatMap(client::create);
    }

    protected Mono<ContentWrapper> updateContent(String baseSnapshotName,
        ContentRequest contentRequest) {
        Assert.notNull(contentRequest, "The contentRequest must not be null");
        Assert.notNull(baseSnapshotName, "The baseSnapshotName must not be null");
        Assert.notNull(contentRequest.headSnapshotName(), "The headSnapshotName must not be null");
        return Mono.defer(() -> client.fetch(Snapshot.class, contentRequest.headSnapshotName())
                .flatMap(headSnapshot -> {
                    var oldVersion = contentRequest.version();
                    var version = headSnapshot.getMetadata().getVersion();
                    if (hasConflict(oldVersion, version)) {
                        // draft a new snapshot as the head snapshot
                        return create(baseSnapshotName, contentRequest,
                            contentRequest.headSnapshotName());
                    }
                    return Mono.just(headSnapshot);
                })
                .flatMap(headSnapshot -> client.fetch(Snapshot.class, baseSnapshotName)
                    .map(baseSnapshot -> determineRawAndContentPatch(headSnapshot,
                        baseSnapshot, contentRequest))
                )
                .flatMap(headSnapshot -> getContextUsername()
                    .doOnNext(username -> Snapshot.addContributor(headSnapshot, username))
                    .thenReturn(headSnapshot)
                )
                .flatMap(client::update)
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            .flatMap(head -> restoredContent(baseSnapshotName, head));
    }

    protected Flux<Snapshot> listSnapshotsBy(Ref ref) {
        var snapshotListOptions = new ListOptions();
        var query = and(isNull("metadata.deletionTimestamp"),
            equal("spec.subjectRef", Snapshot.toSubjectRefKey(ref)));
        snapshotListOptions.setFieldSelector(FieldSelector.of(query));
        var sort = Sort.by("metadata.creationTimestamp", "metadata.name").descending();
        return client.listAll(Snapshot.class, snapshotListOptions, sort);
    }

    boolean hasConflict(Long oldVersion, Long newVersion) {
        return oldVersion != null && !newVersion.equals(oldVersion);
    }

    protected Mono<ContentWrapper> restoredContent(String baseSnapshotName, Snapshot headSnapshot) {
        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .map(baseSnapshot -> ContentWrapper.patchSnapshot(headSnapshot, baseSnapshot));
    }

    protected Snapshot determineRawAndContentPatch(Snapshot snapshotToUse,
        Snapshot baseSnapshot,
        ContentRequest contentRequest) {
        Assert.notNull(baseSnapshot, "The baseSnapshot must not be null.");
        Assert.notNull(contentRequest, "The contentRequest must not be null.");
        Assert.notNull(snapshotToUse, "The snapshotToUse not be null.");
        String originalRaw = baseSnapshot.getSpec().getRawPatch();
        String originalContent = baseSnapshot.getSpec().getContentPatch();
        String baseSnapshotName = baseSnapshot.getMetadata().getName();

        snapshotToUse.getSpec().setLastModifyTime(Instant.now());
        // it is the v1 snapshot, set the content directly
        if (StringUtils.equals(baseSnapshotName,
            snapshotToUse.getMetadata().getName())) {
            snapshotToUse.getSpec().setRawPatch(contentRequest.raw());
            snapshotToUse.getSpec().setContentPatch(contentRequest.content());
            MetadataUtil.nullSafeAnnotations(snapshotToUse)
                .put(Snapshot.KEEP_RAW_ANNO, Boolean.TRUE.toString());
        } else {
            // otherwise diff a patch based on the v1 snapshot
            String revisedRaw = contentRequest.rawPatchFrom(originalRaw);
            String revisedContent = contentRequest.contentPatchFrom(originalContent);
            snapshotToUse.getSpec().setRawPatch(revisedRaw);
            snapshotToUse.getSpec().setContentPatch(revisedContent);
        }
        return snapshotToUse;
    }

    protected Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }
}
