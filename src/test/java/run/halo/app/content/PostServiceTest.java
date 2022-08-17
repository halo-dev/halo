package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.content.TestPost.postV1;
import static run.halo.app.content.TestPost.snapshotV1;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import run.halo.app.content.impl.ContentServiceImpl;
import run.halo.app.content.impl.PostServiceImpl;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link PostService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@WithMockUser(username = "guqing")
@ExtendWith(SpringExtension.class)
class PostServiceTest {
    @Mock
    private ExtensionClient client;

    private PostService postService;

    @BeforeEach
    void setUp() {
        ContentService contentService = new ContentServiceImpl(client);
        postService = new PostServiceImpl(contentService, client);
    }

    @Test
    void draftPost() {
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.setSubjectRef(Post.KIND, "post-A");
        snapshotV1.getMetadata().setName("Post-post-A-v1-snapshot");

        when(client.fetch(eq(Post.class), eq("post-A")))
            .thenReturn(Optional.of(postV1()));
        when(client.fetch(eq(Snapshot.class), eq("snapshot-A")))
            .thenReturn(Optional.of(snapshotV1));

        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Optional.of(snapshotV1));
        // base snapshot
        when(client.list(eq(Snapshot.class), any(), eq(null))).thenReturn(List.of(snapshotV1));

        PostRequest postRequest = new PostRequest(postV1(), contentRequest("B", "<p>B</p>"));

        ArgumentCaptor<AbstractExtension> captor = ArgumentCaptor.forClass(AbstractExtension.class);
        StepVerifier.create(postService.draftPost(postRequest))
            //.expectNext()//postV1()
            .consumeNextWith(post -> {
                System.out.println(JsonUtils.objectToJson(post));
            })
            .expectComplete()
            .verify();

        verify(client, times(2)).create(captor.capture());
        List<AbstractExtension> allValues = captor.getAllValues();
        assertThat(allValues.size()).isEqualTo(2);
        assertThat(allValues.get(0)).isInstanceOf(Snapshot.class);
        assertThat(allValues.get(1)).isInstanceOf(Post.class);
        Post expectPost = postV1();
        expectPost.getSpec().setBaseSnapshot("Post-post-A-v1-snapshot");
        expectPost.getSpec().setHeadSnapshot("Post-post-A-v1-snapshot");
        expectPost.getSpec().setOwner("guqing");
        assertThat(allValues.get(1)).isEqualTo(expectPost);

        Snapshot expectSnapshot = snapshotV1();
        expectSnapshot.getMetadata().setName(snapshotV1.getMetadata().getName());
        expectSnapshot.setSubjectRef(Post.KIND, "post-A");
        expectSnapshot.getSpec().setRawPatch("B");
        expectSnapshot.getSpec().setContentPatch("<p>B</p>");
        assertThat(allValues.get(0)).isEqualTo(expectSnapshot);
    }

    /**
     * post 的 head 指向 v1, release-snapshot 指向 null.
     */
    @Test
    void updatePostWhenHeadEqualsToBaseAndNoReleased() {
        // baseSnapshot = v1
        Snapshot oldSnapshot = snapshotV1();
        oldSnapshot.getMetadata().setName("v1");
        when(client.fetch(eq(Snapshot.class), eq(oldSnapshot.getMetadata().getName())))
            .thenReturn(Optional.of(oldSnapshot));
        // base snapshot
        when(client.list(eq(Snapshot.class), any(), eq(null))).thenReturn(List.of(oldSnapshot));

        // 1.post 的 head 指向 v1, release-snapshot 指向 null
        Post post = postV1();
        post.getSpec().setBaseSnapshot("v1");
        post.getSpec().setHeadSnapshot("v1");
        post.getSpec().setReleaseSnapshot(null);

        when(client.fetch(eq(Post.class), eq("post-A"))).thenReturn(Optional.of(post));

        ArgumentCaptor<Snapshot> captor = ArgumentCaptor.forClass(Snapshot.class);
        PostRequest postRequest = new PostRequest(post, contentRequest("B", "<p>B</p>"));

        StepVerifier.create(postService.updatePost(postRequest))
            .expectNext(post)
            .expectComplete()
            .verify();

        verify(client, times(3)).fetch(eq(Snapshot.class), eq("v1"));
        verify(client, times(2)).update(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);

        oldSnapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
        oldSnapshot.getSpec().setRawPatch("B");
        oldSnapshot.getSpec().setContentPatch("<p>B</p>");
        Snapshot updatedSnapshot = captor.getAllValues().get(0);
        assertThat(updatedSnapshot).isEqualTo(oldSnapshot);
    }

    /**
     * post 的 head 指向 v1, release-snapshot 指向 v1.
     */
    @Test
    void updatePostWhenHeadEqualsToBaseAndHasReleased() {
        // baseSnapshot = v1
        Snapshot oldSnapshot = snapshotV1();
        oldSnapshot.getMetadata().setName("v1");
        oldSnapshot.getSpec().setPublishTime(Instant.now());
        when(client.fetch(eq(Snapshot.class), eq(oldSnapshot.getMetadata().getName())))
            .thenReturn(Optional.of(oldSnapshot));

        // 2.post 的 head 指向 v1, release-snapshot 指向 v1
        Post post = postV1();
        post.getSpec().setBaseSnapshot("v1");
        post.getSpec().setHeadSnapshot("v1");
        post.getSpec().setReleaseSnapshot("v1");

        when(client.fetch(eq(Post.class), eq("post-A"))).thenReturn(Optional.of(post));
        when(client.list(eq(Snapshot.class), any(), any()))
            .thenReturn(List.of(oldSnapshot));
        PostRequest postRequest = new PostRequest(post, contentRequest("B", "<p>B</p>"));

        ArgumentCaptor<Snapshot> createCaptor = ArgumentCaptor.forClass(Snapshot.class);
        final ArgumentCaptor<Post> fetchCaptor = ArgumentCaptor.forClass(Post.class);

        StepVerifier.create(postService.updatePost(postRequest))
            .expectNext(post)
            .expectComplete()
            .verify();

        verify(client, times(4)).fetch(eq(Snapshot.class), eq("v1"));

        // will create a snapshot
        verify(client, times(1)).create(createCaptor.capture());
        Snapshot updatedSnapshot = createCaptor.getValue();
        oldSnapshot.getMetadata().setName(updatedSnapshot.getMetadata().getName());
        oldSnapshot.setSubjectRef(Post.KIND, post.getMetadata().getName());
        oldSnapshot.getSpec().setRawPatch("[{\"source\":{\"position\":0,\"lines\":[\"A\"],"
            + "\"changePosition\":null},\"target\":{\"position\":0,\"lines\":[\"B\"],"
            + "\"changePosition\":null},\"type\":\"CHANGE\"}]");
        oldSnapshot.getSpec().setContentPatch("[{\"source\":{\"position\":0,\"lines\":[\"A\"],"
            + "\"changePosition\":null},\"target\":{\"position\":0,\"lines\":[\"<p>B</p>\"],"
            + "\"changePosition\":null},\"type\":\"CHANGE\"}]");
        oldSnapshot.getSpec().setParentSnapshotName("v1");
        oldSnapshot.getSpec().setVersion(2);
        oldSnapshot.getSpec().setDisplayVersion(Snapshot.displayVersionFrom(2));
        oldSnapshot.getSpec().setPublishTime(null);
        assertThat(updatedSnapshot).isEqualTo(oldSnapshot);

        // update post called
        verify(client, times(1)).update(fetchCaptor.capture());
    }

    @Test
    void publishPostWhenNoReleasedBefore() {
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setName("v1");
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Optional.of(snapshotV1));
        // base snapshot
        when(client.list(eq(Snapshot.class), any(), eq(null))).thenReturn(List.of(snapshotV1));

        Post post = postV1();
        post.getSpec().setBaseSnapshot("v1");
        post.getSpec().setHeadSnapshot("v1");
        // there are no release version before
        post.getSpec().setReleaseSnapshot(null);

        String postName = post.getMetadata().getName();
        when(client.fetch(eq(Post.class), eq(postName))).thenReturn(Optional.of(post));

        ArgumentCaptor<AbstractExtension> captor = ArgumentCaptor.forClass(AbstractExtension.class);

        StepVerifier.create(postService.publishPost(postName))
            .expectNext()
            .consumeNextWith(expectPost -> {
                assertThat(expectPost.getSpec().getVersion()).isEqualTo(1);
                assertThat(expectPost.getSpec().getPublishTime()).isNotNull();
                assertThat(expectPost.getStatus().getPhase()).isEqualTo(
                    Post.PostPhase.PUBLISHED.name());
                assertThat(expectPost.getStatus().getConditions()).hasSize(1);
                Condition condition = expectPost.getStatus().getConditions().get(0);
                assertThat(condition.getType()).isEqualTo(Post.PostPhase.PUBLISHED.name());
                assertThat(condition.getStatus()).isEqualTo(ConditionStatus.TRUE);
                assertThat(condition.getLastTransitionTime()).isNotNull();
            })
            .expectComplete()
            .verify();

        verify(client, times(2)).update(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);

        assertThat(captor.getAllValues().get(0)).isInstanceOf(Snapshot.class);
        Snapshot publishedSnapshot = (Snapshot) captor.getAllValues().get(0);
        assertThat(publishedSnapshot.isPublished()).isTrue();
        assertThat(publishedSnapshot.getSpec().getVersion()).isEqualTo(1);
    }

    @Test
    void publishPostWhenHasReleasedBeforeAndNoDraft() {
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setName("v1");
        snapshotV1.getSpec().setPublishTime(Instant.now());
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Optional.of(snapshotV1));

        // base snapshot
        when(client.list(eq(Snapshot.class), any(), eq(null))).thenReturn(List.of(snapshotV1));

        Post post = postV1();
        post.getSpec().setPublished(true);
        post.getSpec().setPublishTime(Instant.now());
        post.getSpec().setBaseSnapshot("v1");
        post.getSpec().setHeadSnapshot("v1");
        post.getSpec().setReleaseSnapshot("v1");

        String postName = post.getMetadata().getName();
        when(client.fetch(eq(Post.class), eq(postName))).thenReturn(Optional.of(post));

        StepVerifier.create(postService.publishPost(postName))
            .expectNext(post)
            .expectComplete()
            .verify();

        verify(client, times(1)).update(any());
    }

    @Test
    void publishPostWhenHasDraft() {
        Snapshot snapshotV1 = snapshotV1();
        snapshotV1.getMetadata().setName("v1");
        snapshotV1.getSpec().setPublishTime(Instant.now());
        when(client.fetch(eq(Snapshot.class), eq(snapshotV1.getMetadata().getName())))
            .thenReturn(Optional.of(snapshotV1));

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getMetadata().setName("v2");
        snapshotV2.getSpec().setPublishTime(null);
        when(client.fetch(eq(Snapshot.class), eq(snapshotV2.getMetadata().getName())))
            .thenReturn(Optional.of(snapshotV2));

        Post post = postV1();
        post.getSpec().setPublished(true);
        post.getSpec().setPublishTime(Instant.now());
        post.getSpec().setBaseSnapshot("v1");
        post.getSpec().setHeadSnapshot("v2");
        post.getSpec().setReleaseSnapshot("v1");

        String postName = post.getMetadata().getName();
        when(client.fetch(eq(Post.class), eq(postName))).thenReturn(Optional.of(post));

        ArgumentCaptor<AbstractExtension> captor = ArgumentCaptor.forClass(AbstractExtension.class);

        StepVerifier.create(postService.publishPost(postName))
            .expectNext()
            .consumeNextWith(expectPost -> {
                assertThat(expectPost.getSpec().getVersion()).isEqualTo(2);
                assertThat(expectPost.getSpec().getPublishTime()).isNotNull();
                assertThat(expectPost.getStatus().getPhase()).isEqualTo(
                    Post.PostPhase.PUBLISHED.name());
                assertThat(expectPost.getStatus().getConditions()).hasSize(1);
                Condition condition = expectPost.getStatus().getConditions().get(0);
                assertThat(condition.getType()).isEqualTo(Post.PostPhase.PUBLISHED.name());
                assertThat(condition.getStatus()).isEqualTo(ConditionStatus.TRUE);
                assertThat(condition.getLastTransitionTime()).isNotNull();
            })
            .expectComplete()
            .verify();

        verify(client, times(1)).update(captor.capture());
    }

    PostRequest.Content contentRequest(String raw, String content) {
        return new PostRequest.Content(raw, content, "MARKDOWN");
    }
}