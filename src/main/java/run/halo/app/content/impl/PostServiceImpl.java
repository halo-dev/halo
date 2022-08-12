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
import run.halo.app.content.ContentRequest;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
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

    public Mono<Post> draftPost(PostRequest postRequest) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .map(username -> {
                Post post = postRequest.post();
                post.getSpec().setOwner(username);
                client.create(post);

                ContentRequest contentRequest = postRequest.content();
                Snapshot snapshot = contentRequest.toSnapshot();
                snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
                snapshot.addContributor(username);

                client.create(snapshot);
                return client.fetch(Post.class, post.getMetadata().getName())
                    .orElseThrow();
            });
    }

    public Mono<Post> updatePost(PostRequest postRequest) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .map(username -> {
                Post post = postRequest.post();
                client.update(post);

                Post.PostSpec postSpec = post.getSpec();
                String baseSnapshotName = postSpec.getBaseSnapshot();
                String headSnapshotName = postSpec.getHeadSnapshot();
                ContentRequest contentRequest = postRequest.content();
                Snapshot headSnapshot = client.fetch(Snapshot.class, headSnapshotName)
                    .orElseThrow();
                // head is published, then create a new draft snapshot
                if (headSnapshot.isPublished()) {
                    Snapshot snapshot = contentRequest.toSnapshot();
                    snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
                    snapshot.addContributor(username);
                    // v2 or above
                    if (StringUtils.isNotBlank(postSpec.getReleaseSnapshot())) {
                        // latest released snapshot version
                        Snapshot releasedSnapshot =
                            client.fetch(Snapshot.class, postSpec.getReleaseSnapshot())
                                .orElseThrow();
                        snapshot.getSpec().setVersion(releasedSnapshot.getSpec().getVersion() + 1);
                        snapshot.getSpec().setDisplayVersion(
                            Snapshot.displayVersionFrom(snapshot.getSpec().getVersion()));
                        snapshot.getSpec().setParentSnapshotName(headSnapshotName);
                    }
                }

                if (StringUtils.equals(headSnapshotName, baseSnapshotName)) {
                    // v1
                    // obtain snapshot from content request
                    Snapshot snapshot = contentRequest.toSnapshot();
                    snapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());

                    client.update(snapshot);
                } else {
                    // v2 or above
                    updateRawAndContentToHeadSnapshot(baseSnapshotName, headSnapshotName,
                        contentRequest);
                }
                return client.fetch(Post.class, post.getMetadata().getName())
                    .orElseThrow();
            });
    }

    public Mono<Post> publishPost(String postName) {
        return Mono.just(client.fetch(Post.class, postName).orElseThrow())
            .map(post -> {
                Post.PostSpec spec = post.getSpec();
                // publish snapshot
                Snapshot snapshot = client.fetch(Snapshot.class, spec.getHeadSnapshot())
                    .orElseThrow();
                Snapshot.SnapShotSpec snapshotSpec = snapshot.getSpec();
                snapshotSpec.setVersion(snapshotSpec.getVersion() + 1);
                snapshotSpec.setPublishTime(Instant.now());
                snapshotSpec.setDisplayVersion(
                    Snapshot.displayVersionFrom(snapshotSpec.getVersion()));
                spec.setPublished(true);
                spec.setVersion(spec.getVersion() + 1);
                spec.setPublishTime(Instant.now());
                client.update(snapshot);

                // update release snapshot name and condition
                spec.setReleaseSnapshot(snapshot.getMetadata().getName());
                appendPublishedCondition(post);
                client.update(post);
                return client.fetch(Post.class, postName).orElseThrow();
            });
    }

    private static void appendPublishedCondition(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostStatus status = post.getStatusOrDefault();
        status.setPhase(Post.PostPhase.PUBLISHED.name());
        List<Condition> conditions = status.getConditionsOrDefault();
        Condition condition = new Condition();
        condition.setType(Post.PostPhase.PUBLISHED.name());
        condition.setReason("");
        condition.setMessage("");
        condition.setStatus(ConditionStatus.TRUE);
        condition.setLastTransitionTime(Instant.now());
        conditions.add(condition);
    }

    private void updateRawAndContentToHeadSnapshot(String baseSnapshotName,
        String headSnapshotName,
        ContentRequest contentRequest) {
        Snapshot baseSnapShot = client.fetch(Snapshot.class, baseSnapshotName)
            .orElseThrow();
        String originalRaw = baseSnapShot.getSpec().getRawPatch();
        String originalContent = baseSnapShot.getSpec().getRawPatch();
        Snapshot headSnapshot = client.fetch(Snapshot.class, headSnapshotName)
            .orElseThrow();

        String revisedRaw = contentRequest.rawPatchFrom(originalRaw);
        String revisedContent = contentRequest.contentPatchFrom(originalContent);
        headSnapshot.getSpec().setRawPatch(revisedRaw);
        headSnapshot.getSpec().setContentPatch(revisedContent);
    }
}
