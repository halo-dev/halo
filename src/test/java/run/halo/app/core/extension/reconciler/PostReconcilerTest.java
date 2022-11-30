package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.content.TestPost;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;

/**
 * Tests for {@link PostReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostReconcilerTest {

    @Mock
    private ExtensionClient client;
    @Mock
    private ContentService contentService;
    @Mock
    private PostPermalinkPolicy postPermalinkPolicy;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostReconciler postReconciler;

    @Test
    void reconcile() {
        String name = "post-A";
        Post post = TestPost.postV1();
        post.getSpec().setPublish(false);
        post.getSpec().setHeadSnapshot("post-A-head-snapshot");
        when(client.fetch(eq(Post.class), eq(name)))
            .thenReturn(Optional.of(post));
        when(contentService.getContent(eq(post.getSpec().getReleaseSnapshot())))
            .thenReturn(Mono.empty());

        Snapshot snapshotV1 = TestPost.snapshotV1();
        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV1.getSpec().setContributors(Set.of("guqing"));
        snapshotV2.getSpec().setContributors(Set.of("guqing", "zhangsan"));
        when(contentService.listSnapshots(any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        postReconciler.reconcile(new Reconciler.Request(name));

        verify(client, times(3)).update(captor.capture());

        verify(postPermalinkPolicy, times(1)).permalink(any());
        verify(postPermalinkPolicy, times(1)).onPermalinkAdd(any());
        verify(postPermalinkPolicy, times(1)).onPermalinkDelete(any());
        verify(postPermalinkPolicy, times(0)).onPermalinkUpdate(any());

        Post value = captor.getValue();
        assertThat(value.getStatus().getExcerpt()).isNull();
        assertThat(value.getStatus().getContributors()).isEqualTo(List.of("guqing", "zhangsan"));
    }

    @Test
    void reconcileExcerpt() {
        // https://github.com/halo-dev/halo/issues/2452
        String name = "post-A";
        Post post = TestPost.postV1();
        post.getSpec().setPublish(true);
        post.getSpec().setHeadSnapshot("post-A-head-snapshot");
        post.getSpec().setReleaseSnapshot("post-fake-released-snapshot");
        when(client.fetch(eq(Post.class), eq(name)))
            .thenReturn(Optional.of(post));
        when(contentService.getContent(eq(post.getSpec().getReleaseSnapshot())))
            .thenReturn(Mono.just(ContentWrapper.builder()
                .snapshotName(post.getSpec().getHeadSnapshot())
                .raw("hello world")
                .content("<p>hello world</p>")
                .rawType("markdown")
                .build()));

        Snapshot snapshotV2 = TestPost.snapshotV2();
        snapshotV2.getMetadata().setLabels(new HashMap<>());
        snapshotV2.getSpec().setContributors(Set.of("guqing", "zhangsan"));

        Snapshot snapshotV1 = TestPost.snapshotV1();
        snapshotV1.getSpec().setContributors(Set.of("guqing"));

        when(contentService.listSnapshots(any()))
            .thenReturn(Flux.just(snapshotV1, snapshotV2));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        postReconciler.reconcile(new Reconciler.Request(name));

        verify(client, times(4)).update(captor.capture());
        Post value = captor.getValue();
        assertThat(value.getStatus().getExcerpt()).isEqualTo("hello world");
    }
}