package run.halo.app.content.impl;

import java.security.Principal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;

/**
 * A default implementation of {@link PostService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostServiceImpl implements PostService {
    private static final Comparator<Snapshot> SNAPSHOT_COMPARATOR =
        Comparator.comparing(snapshot -> snapshot.getSpec().getVersion());
    public static Comparator<Snapshot> LATEST_SNAPSHOT_COMPARATOR = SNAPSHOT_COMPARATOR.reversed();
    private final ContentService contentService;
    private final ReactiveExtensionClient client;

    public PostServiceImpl(ContentService contentService, ReactiveExtensionClient client) {
        this.contentService = contentService;
        this.client = client;
    }

    @Override
    public Mono<Post> draftPost(PostRequest postRequest) {
        return contentService.draftContent(postRequest.contentRequest())
            .flatMap(contentWrapper -> getContextUsername()
                .flatMap(username -> {
                    Post post = postRequest.post();
                    post.getSpec().setBaseSnapshot(contentWrapper.snapshotName());
                    post.getSpec().setHeadSnapshot(contentWrapper.snapshotName());
                    post.getSpec().setOwner(username);
                    return client.create(post)
                        .then(Mono.defer(() ->
                            client.fetch(Post.class, postRequest.post().getMetadata().getName())));
                }));
    }

    @Override
    public Mono<Post> updatePost(PostRequest postRequest) {
        Post post = postRequest.post();
        return contentService.updateContent(postRequest.contentRequest())
            .flatMap(contentWrapper -> {
                post.getSpec().setHeadSnapshot(contentWrapper.snapshotName());
                return client.update(post);
            })
            .then(Mono.defer(() -> client.fetch(Post.class, post.getMetadata().getName())));
    }

    private Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }

    @Override
    public Mono<Post> publishPost(String postName) {
        return client.fetch(Post.class, postName)
            .flatMap(post -> {
                Post.PostSpec spec = post.getSpec();
                // publish snapshot
                return Mono.zip(Mono.just(post),
                    client.fetch(Snapshot.class, spec.getHeadSnapshot()));
            })
            .flatMap(tuple -> {
                Post post = tuple.getT1();
                Snapshot snapshot = tuple.getT2();

                Post.PostSpec postSpec = post.getSpec();
                if (Objects.equals(true, postSpec.getPublished())) {
                    // has been published before
                    postSpec.setVersion(postSpec.getVersion() + 1);
                } else {
                    postSpec.setPublished(true);
                }

                if (postSpec.getPublishTime() == null) {
                    postSpec.setPublishTime(Instant.now());
                }

                // update release snapshot name and condition
                postSpec.setReleaseSnapshot(snapshot.getMetadata().getName());
                appendPublishedCondition(post);

                Snapshot.SubjectRef subjectRef =
                    Snapshot.SubjectRef.of(Post.KIND, post.getMetadata().getName());
                return contentService.publish(snapshot.getMetadata().getName(), subjectRef)
                    .flatMap(contentWrapper -> {
                        post.getSpec().setReleaseSnapshot(contentWrapper.snapshotName());
                        return client.update(post);
                    })
                    .then(Mono.defer(() -> client.fetch(Post.class, postName)));
            });
    }

    void appendPublishedCondition(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostStatus status = post.getStatusOrDefault();
        status.setPhase(Post.PostPhase.PUBLISHED.name());
        List<Condition> conditions = status.getConditionsOrDefault();
        Condition condition = new Condition();
        conditions.add(condition);

        condition.setType(Post.PostPhase.PUBLISHED.name());
        condition.setReason(Post.PostPhase.PUBLISHED.name());
        condition.setMessage("");
        condition.setStatus(ConditionStatus.TRUE);
        condition.setLastTransitionTime(Instant.now());
    }
}
