package run.halo.app.content.impl;

import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
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
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(principal -> {
                String username = principal.getName();
                Post post = postRequest.post();
                String headSnapshotName = post.getSpec().getHeadSnapshot();
                return Mono.zip(Mono.just(username), Mono.just(post), update(post),
                    fetch(Snapshot.class, headSnapshotName));
            })
            .map(tuple -> {
                Post post = tuple.getT2();
                String username = tuple.getT1();
                Post.PostSpec postSpec = post.getSpec();
                Snapshot headSnapshot = tuple.getT4();
                String headSnapshotName = headSnapshot.getMetadata().getName();
                String baseSnapshotName = postSpec.getBaseSnapshot();
                ContentRequest contentRequest = postRequest.content();
                if (headSnapshot.isPublished()) {
                    Snapshot snapshot = contentRequest.toSnapshot();
                    snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
                    snapshot.addContributor(username);
                    // v2 or above
                    if (StringUtils.isNotBlank(postSpec.getReleaseSnapshot())) {
                        // latest released snapshot version
                        return fetch(Snapshot.class, postSpec.getReleaseSnapshot())
                            .map(releasedSnapshot -> {
                                snapshot.getSpec()
                                    .setVersion(releasedSnapshot.getSpec().getVersion() + 1);
                                snapshot.getSpec().setDisplayVersion(
                                    Snapshot.displayVersionFrom(snapshot.getSpec().getVersion()));
                                snapshot.getSpec()
                                    .setParentSnapshotName(headSnapshotName);
                                return snapshot;
                            });

                    }
                    return snapshot;
                }
                if (StringUtils.equals(headSnapshotName, baseSnapshotName)) {
                    // v1
                    // obtain snapshot from content request
                    Snapshot snapshot = contentRequest.toSnapshot();
                    snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());

                    return update(snapshot);
                }
                // v2 or above
                return updateRawAndContentToHeadSnapshot(baseSnapshotName, headSnapshotName,
                    contentRequest);
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

    private Mono<Snapshot> updateRawAndContentToHeadSnapshot(String baseSnapshotName,
        String headSnapshotName,
        ContentRequest contentRequest) {
        return Mono.zip(fetch(Snapshot.class, baseSnapshotName),
                fetch(Snapshot.class, headSnapshotName))
            .flatMap(tuple -> {
                Snapshot baseSnapshot = tuple.getT1();
                String originalRaw = baseSnapshot.getSpec().getRawPatch();
                String originalContent = baseSnapshot.getSpec().getRawPatch();

                Snapshot headSnapshot = tuple.getT2();
                String revisedRaw = contentRequest.rawPatchFrom(originalRaw);
                String revisedContent = contentRequest.contentPatchFrom(originalContent);
                headSnapshot.getSpec().setRawPatch(revisedRaw);
                headSnapshot.getSpec().setContentPatch(revisedContent);
                return update(headSnapshot)
                    .thenReturn(headSnapshot);
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
