package run.halo.app.content.impl;

import java.security.Principal;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

/**
 * A default implementation of {@link ContentService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ContentServiceImpl implements ContentService {
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
    public Mono<ContentWrapper> draftContent(ContentRequest content) {
        return this.draftContent(content, content.headSnapshotName());
    }

    @Override
    public Mono<ContentWrapper> draftContent(ContentRequest contentRequest, String parentName) {
        return Mono.defer(
                () -> {
                    Snapshot snapshot = contentRequest.toSnapshot();
                    snapshot.getMetadata().setName(UUID.randomUUID().toString());
                    snapshot.getSpec().setParentSnapshotName(parentName);
                    return getBaseSnapshot(contentRequest.subjectRef())
                        .defaultIfEmpty(snapshot)
                        .map(baseSnapshot -> determineRawAndContentPatch(snapshot, baseSnapshot,
                            contentRequest))
                        .flatMap(source -> getContextUsername()
                            .map(username -> {
                                Snapshot.addContributor(source, username);
                                source.getSpec().setOwner(username);
                                return source;
                            })
                            .defaultIfEmpty(source)
                        );
                })
            .flatMap(snapshot -> client.create(snapshot)
                .flatMap(this::restoredContent));
    }

    @Override
    public Mono<ContentWrapper> updateContent(ContentRequest contentRequest) {
        Assert.notNull(contentRequest, "The contentRequest must not be null");
        Assert.notNull(contentRequest.headSnapshotName(), "The headSnapshotName must not be null");
        Ref subjectRef = contentRequest.subjectRef();
        return client.fetch(Snapshot.class, contentRequest.headSnapshotName())
            .flatMap(headSnapshot -> getBaseSnapshot(subjectRef)
                .map(baseSnapshot -> determineRawAndContentPatch(headSnapshot, baseSnapshot,
                    contentRequest)
                )
            )
            .flatMap(headSnapshot -> getContextUsername()
                .map(username -> {
                    Snapshot.addContributor(headSnapshot, username);
                    return headSnapshot;
                })
                .defaultIfEmpty(headSnapshot)
            )
            .flatMap(client::update)
            .flatMap(this::restoredContent);
    }

    private Mono<ContentWrapper> restoredContent(Snapshot headSnapshot) {
        return getBaseSnapshot(headSnapshot.getSpec().getSubjectRef())
            .map(headSnapshot::applyPatch);
    }

    @Override
    public Mono<Snapshot> getBaseSnapshot(Ref subjectRef) {
        return listSnapshots(subjectRef)
            .sort(createTimeReversedComparator().reversed())
            .filter(p -> StringUtils.equals(Boolean.TRUE.toString(),
                ExtensionUtil.nullSafeAnnotations(p).get(Snapshot.KEEP_RAW_ANNO)))
            .next();
    }

    @Override
    public Mono<Snapshot> latestSnapshotVersion(Ref subjectRef) {
        Assert.notNull(subjectRef, "The subjectRef must not be null.");
        return listSnapshots(subjectRef)
            .sort(createTimeReversedComparator())
            .next();
    }

    @Override
    public Flux<Snapshot> listSnapshots(Ref subjectRef) {
        Assert.notNull(subjectRef, "The subjectRef must not be null.");
        return client.list(Snapshot.class, snapshot -> subjectRef.equals(snapshot.getSpec()
            .getSubjectRef()), null);
    }

    private Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }

    private Snapshot determineRawAndContentPatch(Snapshot snapshotToUse, Snapshot baseSnapshot,
        ContentRequest contentRequest) {
        Assert.notNull(baseSnapshot, "The baseSnapshot must not be null.");
        Assert.notNull(contentRequest, "The contentRequest must not be null.");
        Assert.notNull(snapshotToUse, "The snapshotToUse not be null.");
        String originalRaw = baseSnapshot.getSpec().getRawPatch();
        String originalContent = baseSnapshot.getSpec().getContentPatch();
        String baseSnapshotName = baseSnapshot.getMetadata().getName();

        snapshotToUse.getSpec().setLastModifyTime(Instant.now());
        // it is the v1 snapshot, set the content directly
        if (StringUtils.equals(baseSnapshotName, snapshotToUse.getMetadata().getName())) {
            snapshotToUse.getSpec().setRawPatch(contentRequest.raw());
            snapshotToUse.getSpec().setContentPatch(contentRequest.content());
            ExtensionUtil.nullSafeAnnotations(snapshotToUse)
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

    Comparator<Snapshot> createTimeReversedComparator() {
        Function<Snapshot, String> name = snapshot -> snapshot.getMetadata().getName();
        Function<Snapshot, Instant> createTime = snapshot -> snapshot.getMetadata()
            .getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(name)
            .reversed();
    }
}
