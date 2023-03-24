package run.halo.app.content;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Abstract Service for {@link Snapshot}.
 *
 * @author guqing
 * @since 2.0.0
 */
@AllArgsConstructor
public abstract class AbstractContentService {

    private final ReactiveExtensionClient client;

    public Mono<ContentWrapper> getContent(String snapshotName, String baseSnapshotName) {
        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .flatMap(baseSnapshot -> {
                if (StringUtils.equals(snapshotName, baseSnapshotName)) {
                    var contentWrapper = ContentWrapper.patchSnapshot(baseSnapshot, baseSnapshot);
                    return Mono.just(contentWrapper);
                }
                return client.fetch(Snapshot.class, snapshotName)
                    .map(snapshot -> ContentWrapper.patchSnapshot(snapshot, baseSnapshot));
            });
    }

    protected void checkBaseSnapshot(Snapshot snapshot) {
        Assert.notNull(snapshot, "The snapshot must not be null.");
        String keepRawAnno =
            MetadataUtil.nullSafeAnnotations(snapshot).get(Snapshot.KEEP_RAW_ANNO);
        if (!org.thymeleaf.util.StringUtils.equals(Boolean.TRUE.toString(), keepRawAnno)) {
            throw new IllegalArgumentException(
                String.format("The snapshot [%s] is not a base snapshot.",
                    snapshot.getMetadata().getName()));
        }
    }

    protected Mono<ContentWrapper> draftContent(@Nullable String baseSnapshotName,
        ContentRequest contentRequest,
        @Nullable String parentSnapshotName) {
        Snapshot snapshot = contentRequest.toSnapshot();
        snapshot.getMetadata().setName(UUID.randomUUID().toString());
        snapshot.getSpec().setParentSnapshotName(parentSnapshotName);

        final String baseSnapshotNameToUse =
            StringUtils.defaultString(baseSnapshotName, snapshot.getMetadata().getName());
        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .defaultIfEmpty(snapshot)
            .map(baseSnapshot -> determineRawAndContentPatch(snapshot, baseSnapshot,
                contentRequest)
            )
            .flatMap(source -> getContextUsername()
                .map(username -> {
                    Snapshot.addContributor(source, username);
                    source.getSpec().setOwner(username);
                    return source;
                })
                .defaultIfEmpty(source)
            )
            .flatMap(snapshotToCreate -> client.create(snapshotToCreate)
                .flatMap(head -> restoredContent(baseSnapshotNameToUse, head)));
    }

    protected Mono<ContentWrapper> draftContent(String baseSnapshotName, ContentRequest content) {
        return this.draftContent(baseSnapshotName, content, content.headSnapshotName());
    }

    protected Mono<ContentWrapper> updateContent(String baseSnapshotName,
        ContentRequest contentRequest) {
        Assert.notNull(contentRequest, "The contentRequest must not be null");
        Assert.notNull(baseSnapshotName, "The baseSnapshotName must not be null");
        Assert.notNull(contentRequest.headSnapshotName(), "The headSnapshotName must not be null");
        return client.fetch(Snapshot.class, contentRequest.headSnapshotName())
            .flatMap(headSnapshot -> client.fetch(Snapshot.class, baseSnapshotName)
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
            .flatMap(head -> restoredContent(baseSnapshotName, head));
    }

    protected Mono<ContentWrapper> restoredContent(String baseSnapshotName, Snapshot headSnapshot) {
        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .map(baseSnapshot -> ContentWrapper.patchSnapshot(headSnapshot, baseSnapshot));
    }

    protected Snapshot determineRawAndContentPatch(Snapshot snapshotToUse, Snapshot baseSnapshot,
        ContentRequest contentRequest) {
        Assert.notNull(baseSnapshot, "The baseSnapshot must not be null.");
        Assert.notNull(contentRequest, "The contentRequest must not be null.");
        Assert.notNull(snapshotToUse, "The snapshotToUse not be null.");
        String originalRaw = baseSnapshot.getSpec().getRawPatch();
        String originalContent = baseSnapshot.getSpec().getContentPatch();
        String baseSnapshotName = baseSnapshot.getMetadata().getName();

        snapshotToUse.getSpec().setLastModifyTime(Instant.now());
        // it is the v1 snapshot, set the content directly
        if (org.thymeleaf.util.StringUtils.equals(baseSnapshotName,
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
