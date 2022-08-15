package run.halo.app.content.impl;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostServiceImpl implements PostService {

    private final ExtensionClient client;

    public PostServiceImpl(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Post> draftPost(PostRequest postRequest) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(principal -> {
                String username = principal.getName();
                Post post = postRequest.post();
                post.getSpec().setOwner(username);
                return create(post)
                    .map(v -> {
                        ContentRequest contentRequest = postRequest.content();
                        Snapshot snapshot = contentRequest.toSnapshot();
                        snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
                        snapshot.addContributor(username);
                        return snapshot;
                    })
                    .flatMap(this::create)
                    .flatMap(v -> fetch(Post.class, post.getMetadata().getName()));
            });
    }

    @Override
    public Mono<Post> updatePost(PostRequest postRequest) {
        Post post = postRequest.post();
        String headSnapshotName = post.getSpec().getHeadSnapshot();
        return update(post)
            .flatMap(v -> getContextUsername())
            .flatMap(username -> fetch(Snapshot.class, headSnapshotName)
                .map(headSnapShot -> Tuples.of(username, headSnapShot)))
            .map(tuple -> {
                String username = tuple.getT1();
                Snapshot headSnapShot = tuple.getT2();
                ContentRequest contentRequest = postRequest.content();
                return handleSnapshot(headSnapShot, contentRequest, post, username);
            })
            .flatMap(snapshot -> fetch(Post.class, postRequest.post().getMetadata().getName()));

        //
        //     Post.PostSpec postSpec = post.getSpec();
        //     String baseSnapshotName = postSpec.getBaseSnapshot();
        //     String headSnapshotName = postSpec.getHeadSnapshot();
        //     ContentRequest contentRequest = postRequest.content();
        //     Snapshot headSnapshot = client.fetch(Snapshot.class, headSnapshotName)
        //         .orElseThrow();
        //     // head is published, then create a new draft snapshot
        //     if (headSnapshot.isPublished()) {
        //         Snapshot snapshot = contentRequest.toSnapshot();
        //         snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
        //         snapshot.addContributor(username);
        //         // v2 or above
        //         if (StringUtils.isNotBlank(postSpec.getReleaseSnapshot())) {
        //             // latest released snapshot version
        //             Snapshot releasedSnapshot =
        //                 client.fetch(Snapshot.class, postSpec.getReleaseSnapshot())
        //                     .orElseThrow();
        //             snapshot.getSpec().setVersion(releasedSnapshot.getSpec().getVersion() + 1);
        //             snapshot.getSpec().setDisplayVersion(
        //                 Snapshot.displayVersionFrom(snapshot.getSpec().getVersion()));
        //             snapshot.getSpec().setParentSnapshotName(headSnapshotName);
        //         }
        //     }
        //
        //     if (StringUtils.equals(headSnapshotName, baseSnapshotName)) {
        //         // v1
        //         // obtain snapshot from content request
        //         Snapshot snapshot = contentRequest.toSnapshot();
        //         snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
        //
        //         client.update(snapshot);
        //     } else {
        //         // v2 or above
        //         updateRawAndContentToHeadSnapshot(baseSnapshotName, headSnapshotName,
        //             contentRequest);
        //     }
        //     return client.fetch(Post.class, post.getMetadata().getName())
        //         .orElseThrow();
        // });
    }

    private Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }

    private Mono<Snapshot> handleSnapshot(Snapshot headSnapshot, ContentRequest contentRequest,
        Post post,
        String username) {
        Post.PostSpec postSpec = post.getSpec();
        String headSnapshotName = postSpec.getHeadSnapshot();
        String baseSnapshotName = postSpec.getBaseSnapshot();

        Mono<Snapshot> snapshotToUse;
        if (headSnapshot.isPublished()) {
            snapshotToUse = headSnapshotPublished(headSnapshot, contentRequest, post, username);
        }
        if (StringUtils.equals(headSnapshotName, baseSnapshotName)) {
            // v1
            // obtain snapshot from content request
            Snapshot snapshot = contentRequest.toSnapshot();
            snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());

            return Mono.just(snapshot);
        }
        // v2 or above
        return updateRawAndContentToHeadSnapshot(baseSnapshotName, headSnapshotName,
            contentRequest);
    }

    private Mono<Snapshot> headSnapshotPublished(Snapshot headSnapshot, ContentRequest contentRequest, Post post,
        String username) {
        String headSnapshotName = post.getSpec().getHeadSnapshot();
        Post.PostSpec postSpec = post.getSpec();
        Snapshot newSnapshot = contentRequest.toSnapshot();
        newSnapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
        newSnapshot.addContributor(username);

        if (StringUtils.isBlank(postSpec.getReleaseSnapshot())) {
            // no released snapshot, indicating v1 now, just update the content directly
            return updateRawAndContentToHeadSnapshot(newSnapshot, headSnapshotName, contentRequest);
        }

        // has released snapshot, there are 3 assumptions:
        // if headPtr != releasePtr && head is not published, then update its content directly
        // if headPtr != releasePtr && head is published, then create a new snapshot
        // if headPtr == releasePtr, then create a new snapshot too
        return fetch(Snapshot.class, postSpec.getReleaseSnapshot())
            .flatMap(releasedSnapshot -> {
                newSnapshot.getSpec()
                    .setVersion(releasedSnapshot.getSpec().getVersion() + 1);
                newSnapshot.getSpec().setDisplayVersion(
                    Snapshot.displayVersionFrom(newSnapshot.getSpec().getVersion()));
                newSnapshot.getSpec()
                    .setParentSnapshotName(headSnapshotName);

                // head is published or headPtr == releasePtr
                if(headSnapshot.isPublished() || StringUtils.equals(headSnapshotName, postSpec.getReleaseSnapshot())) {
                    // create a new snapshot,done
                    return updateRawAndContentToHeadSnapshot(newSnapshot, headSnapshotName, contentRequest);
                }

                // else,done
                // update its content directly
                return updateRawAndContentToHeadSnapshot(headSnapshot, headSnapshotName, contentRequest);
            });
    }

    @Override
    public Mono<Post> publishPost(String postName) {
        return fetch(Post.class, postName)
            .flatMap(post -> {
                Post.PostSpec spec = post.getSpec();
                // publish snapshot
                return Mono.zip(Mono.just(post), fetch(Snapshot.class, spec.getHeadSnapshot()));
            })
            .flatMap(tuple -> {
                Post post = tuple.getT1();
                Snapshot snapshot = tuple.getT2();

                Snapshot.SnapShotSpec snapshotSpec = snapshot.getSpec();
                snapshotSpec.setVersion(snapshotSpec.getVersion() + 1);
                snapshotSpec.setPublishTime(Instant.now());
                snapshotSpec.setDisplayVersion(
                    Snapshot.displayVersionFrom(snapshotSpec.getVersion()));

                Post.PostSpec postSpec = post.getSpec();
                postSpec.setPublished(true);
                postSpec.setVersion(postSpec.getVersion() + 1);
                postSpec.setPublishTime(Instant.now());

                // update release snapshot name and condition
                postSpec.setReleaseSnapshot(snapshot.getMetadata().getName());
                appendPublishedCondition(post);
                return Mono.zip(update(snapshot), update(post))
                    .then(fetch(Post.class, postName));
            });
    }

    private static void appendPublishedCondition(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostStatus status = post.getStatusOrDefault();
        status.setPhase(Post.PostPhase.PUBLISHED.name());
        List<Condition> conditions = status.getConditionsOrDefault();
        Condition condition = new Condition();
        conditions.add(condition);

        condition.setType(Post.PostPhase.PUBLISHED.name());
        condition.setReason("");
        condition.setMessage("");
        condition.setStatus(ConditionStatus.TRUE);
        condition.setLastTransitionTime(Instant.now());
    }

    private Mono<Snapshot> updateRawAndContentToHeadSnapshot(Snapshot snapshotToUpdate,
        String baseSnapshotName,
        ContentRequest contentRequest) {
        return fetch(Snapshot.class, baseSnapshotName)
            .flatMap(baseSnapshot -> {
                String originalRaw = baseSnapshot.getSpec().getRawPatch();
                String originalContent = baseSnapshot.getSpec().getRawPatch();

                String revisedRaw = contentRequest.rawPatchFrom(originalRaw);
                String revisedContent = contentRequest.contentPatchFrom(originalContent);
                snapshotToUpdate.getSpec().setRawPatch(revisedRaw);
                snapshotToUpdate.getSpec().setContentPatch(revisedContent);
                return update(snapshotToUpdate)
                    .thenReturn(snapshotToUpdate);
            });
    }

    <E extends Extension> Mono<E> fetch(Class<E> type, String name) {
        return Mono.just(client.fetch(type, name).orElseThrow());
    }

    <E extends Extension> Mono<Void> update(E extension) {
        client.update(extension);
        return Mono.empty();
    }

    <E extends Extension> Mono<Void> create(E extension) {
        client.create(extension);
        return Mono.empty();
    }
}
