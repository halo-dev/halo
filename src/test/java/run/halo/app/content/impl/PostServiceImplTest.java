package run.halo.app.content.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.ContentService;
import run.halo.app.content.PostQuery;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;

/**
 * Tests for {@link PostServiceImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {
    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private ContentService contentService;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void listPredicate() {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.put("category", List.of("category1", "category2"));
        PostQuery postQuery = new PostQuery(multiValueMap);

        Post post = TestPost.postV1();
        post.getSpec().setTags(null);
        post.getStatusOrDefault().setContributors(null);
        post.getSpec().setCategories(List.of("category1"));
        boolean test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        post.getSpec().setTags(List.of("tag1"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        // Do not include tags
        multiValueMap.put("tag", List.of("tag2"));
        post.getSpec().setTags(List.of("tag1"));
        post.getSpec().setCategories(null);
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isFalse();

        multiValueMap.put("tag", List.of());
        multiValueMap.remove("category");
        postQuery = new PostQuery(multiValueMap);
        post.getSpec().setTags(List.of());
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        multiValueMap.put("labelSelector", List.of("hello"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isFalse();

        post.getMetadata().setLabels(Map.of("hello", "world"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();
    }

    @Test
    void publishWhenPostIsNonePublishedState() {
        String postName = "fake-post";
        String snapV1name = "fake-post-snapshot-v1";
        Post post = TestPost.postV1();
        post.getMetadata().setName(postName);

        // v1 not published
        Snapshot snapshotV1 = TestPost.snapshotV1();
        snapshotV1.getMetadata().setName(snapV1name);
        snapshotV1.getSpec().setPublishTime(null);
        post.getSpec().setBaseSnapshot(snapshotV1.getMetadata().getName());

        post.getSpec().setHeadSnapshot(null);
        post.getSpec().setReleaseSnapshot(null);
        when(client.fetch(eq(Post.class), eq(postName))).thenReturn(Mono.just(post));
        verify(client, times(0)).fetch(eq(Snapshot.class), eq(snapV1name));

        postService.publishPost(postName)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void publishWhenPostIsPublishedStateAndNotPublishedBefore() {
        String postName = "fake-post";
        String snapV1name = "fake-post-snapshot-v1";
        Post post = TestPost.postV1();
        post.getSpec().setPublish(true);
        post.getSpec().setPublishTime(null);
        post.getMetadata().setName(postName);
        post.getSpec().setBaseSnapshot(snapV1name);
        post.getSpec().setHeadSnapshot(null);
        post.getSpec().setReleaseSnapshot(null);
        when(client.fetch(eq(Post.class), eq(postName))).thenReturn(Mono.just(post));

        // v1 not published
        Snapshot snapshotV1 = TestPost.snapshotV1();
        snapshotV1.getMetadata().setName(snapV1name);
        snapshotV1.getSpec().setPublishTime(null);
        when(client.fetch(eq(Snapshot.class), eq(snapV1name))).thenReturn(Mono.just(snapshotV1));

        when(contentService.publish(eq(snapV1name), eq(Ref.of(post))))
            .thenReturn(Mono.just(snapshotV1.applyPatch(snapshotV1)));

        when(client.update(any(Post.class))).thenAnswer((Answer<Mono<Post>>) invocation -> {
            Post updated = invocation.getArgument(0);
            return Mono.just(updated);
        });

        postService.publishPost(postName)
            .as(StepVerifier::create)
            .consumeNextWith(expected -> {
                ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
                verify(client, times(1)).update(captor.capture());
                Post updated = captor.getValue();
                assertThat(updated.getSpec().getReleaseSnapshot()).isEqualTo(snapV1name);
                assertThat(updated.getSpec().getHeadSnapshot()).isEqualTo(snapV1name);
                assertThat(updated.getSpec().getPublishTime()).isNotNull();
                assertThat(updated.getSpec().getVersion()).isEqualTo(1);
                List<Condition> conditions = updated.getStatus().getConditions();
                assertThat(conditions).hasSize(1);
                assertThat(conditions.get(0).getType()).isEqualTo("PUBLISHED");
                assertThat(conditions.get(0).getStatus()).isEqualTo(ConditionStatus.TRUE);
                assertThat(expected).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    void publishWhenPostIsPublishedStateAndPublishedBefore() {
        String postName = "fake-post";
        String snapV1name = "fake-post-snapshot-v1";
        String snapV2name = "fake-post-snapshot-v2";
        Post post = TestPost.postV1();
        post.getSpec().setPublish(true);
        post.getSpec().setPublishTime(null);
        post.getMetadata().setName(postName);
        post.getSpec().setBaseSnapshot(snapV1name);
        post.getSpec().setHeadSnapshot(snapV2name);
        post.getSpec().setReleaseSnapshot(snapV2name);
        ExtensionUtil.nullSafeLabels(post).put(Post.PUBLISHED_LABEL, "true");
        when(client.fetch(eq(Post.class), eq(postName))).thenReturn(Mono.just(post));

        // v1 has been published
        Snapshot snapshotV1 = TestPost.snapshotV1();
        snapshotV1.getMetadata().setName(snapV1name);
        snapshotV1.getMetadata().setLabels(new HashMap<>());
        Snapshot.putPublishedLabel(snapshotV1.getMetadata().getLabels());
        snapshotV1.getSpec().setPublishTime(Instant.now());

        // v1 not published
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getMetadata().setName(snapV2name);
        snapshotV2.getSpec().setPublishTime(null);
        when(client.fetch(eq(Snapshot.class), eq(snapV2name))).thenReturn(Mono.just(snapshotV2));

        when(contentService.publish(eq(snapV2name), eq(Ref.of(post))))
            .thenReturn(Mono.just(snapshotV2.applyPatch(snapshotV1)));

        when(client.update(any(Post.class))).thenAnswer((Answer<Mono<Post>>) invocation -> {
            Post updated = invocation.getArgument(0);
            return Mono.just(updated);
        });

        postService.publishPost(postName)
            .as(StepVerifier::create)
            .consumeNextWith(expected -> {
                assertThat(expected).isNotNull();
                ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
                verify(client, times(1)).update(captor.capture());
                Post updated = captor.getValue();
                assertThat(updated.getSpec().getReleaseSnapshot()).isEqualTo(snapV2name);
                assertThat(updated.getSpec().getHeadSnapshot()).isEqualTo(snapV2name);
                assertThat(updated.getSpec().getPublishTime()).isNotNull();
                assertThat(updated.getSpec().getVersion()).isEqualTo(2);
            })
            .verifyComplete();
    }
}