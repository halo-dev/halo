package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.metrics.MeterUtils;

/**
 * Tests for {@link CommentReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CommentReconcilerTest {

    @Mock
    private ExtensionClient client;

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    @Mock
    SchemeManager schemeManager;

    private CommentReconciler commentReconciler;

    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        commentReconciler = new CommentReconciler(client, meterRegistry, schemeManager);
    }

    @Test
    void reconcile() {
        Comment comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName("test");
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setLastReadTime(now.plusSeconds(5));
        comment.setStatus(new Comment.CommentStatus());

        lenient().when(client.fetch(eq(Comment.class), eq("test")))
            .thenReturn(Optional.of(comment));

        lenient().when(client.list(eq(Reply.class), any(), any()))
            .thenReturn(replyList());

        Reconciler.Result result = commentReconciler.reconcile(new Reconciler.Request("test"));
        assertThat(result.reEnqueue()).isTrue();
        assertThat(result.retryAfter()).isEqualTo(Duration.ofMinutes(1));

        verify(client, times(3)).fetch(eq(Comment.class), eq("test"));
        verify(client, times(1)).list(eq(Reply.class), any(), any());

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(client, times(2)).update(captor.capture());
        List<Comment> allValues = captor.getAllValues();
        Comment value = allValues.get(1);

        assertThat(value.getStatus().getReplyCount()).isEqualTo(3);
        assertThat(value.getStatus().getLastReplyTime()).isEqualTo(now.plusSeconds(6));
        assertThat(value.getStatus().getUnreadReplyCount()).isEqualTo(1);
        assertThat(value.getStatus().getHasNewReply()).isTrue();

        assertThat(value.getMetadata().getFinalizers()).contains(CommentReconciler.FINALIZER_NAME);
    }

    @Test
    void reconcileDelete() {
        Comment comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName("test");
        comment.getMetadata().setDeletionTimestamp(Instant.now());
        Set<String> finalizers = new HashSet<>();
        finalizers.add(CommentReconciler.FINALIZER_NAME);
        comment.getMetadata().setFinalizers(finalizers);
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setLastReadTime(now.plusSeconds(5));
        comment.setStatus(new Comment.CommentStatus());

        lenient().when(client.list(eq(Reply.class), any(), any()))
            .thenReturn(replyList());
        lenient().when(client.fetch(eq(Comment.class), eq("test")))
            .thenReturn(Optional.of(comment));

        lenient().when(client.list(eq(Reply.class), any(), any()))
            .thenReturn(replyList());

        Reconciler.Result reconcile = commentReconciler.reconcile(new Reconciler.Request("test"));
        assertThat(reconcile.reEnqueue()).isFalse();
        assertThat(reconcile.retryAfter()).isNull();

        verify(client, times(1)).list(eq(Reply.class), any(), any());
        verify(client, times(3)).delete(any(Reply.class));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(client, times(1)).update(captor.capture());
        Comment value = captor.getValue();
        assertThat(value.getMetadata().getFinalizers()
            .contains(CommentReconciler.FINALIZER_NAME)).isFalse();
    }

    @Test
    void reconcileCommentCount() {
        when(client.list(eq(Comment.class), any(), any()))
            .thenReturn(commentList());
        when(client.fetch(eq(Comment.class), eq("test")))
            .thenReturn(Optional.of(getComment("test")));
        lenient().when(client.list(eq(Reply.class), any(), any()))
            .thenReturn(List.of());

        Ref ref = getRef();
        GroupVersionKind groupVersionKind =
            new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
        Scheme scheme = new Scheme(Post.class, groupVersionKind, "posts", "post",
            Mockito.mock(ObjectNode.class));
        when(schemeManager.fetch(any())).thenReturn(Optional.of(scheme));

        String fakePostCounterName =
            MeterUtils.nameOf(ref.getGroup(), scheme.plural(), "fake-post");
        String testPostCounterName =
            MeterUtils.nameOf(ref.getGroup(), scheme.plural(), "test-post");

        Counter approvedCommentCounter =
            MeterUtils.approvedCommentCounter(meterRegistry, fakePostCounterName);
        approvedCommentCounter.increment(5);

        assertThat(approvedCommentCounter.count()).isEqualTo(5.0);

        Counter testPostCounter =
            MeterUtils.approvedCommentCounter(meterRegistry, testPostCounterName);
        testPostCounter.increment(0);
        assertThat(testPostCounter.count()).isEqualTo(0.0);

        Counter totalCommentCounter =
            MeterUtils.totalCommentCounter(meterRegistry, fakePostCounterName);
        totalCommentCounter.increment(8);

        commentReconciler.reconcile(new Reconciler.Request("test"));
        assertThat(approvedCommentCounter.count()).isEqualTo(3.0);
        assertThat(testPostCounter.count()).isEqualTo(1.0);
        assertThat(totalCommentCounter.count()).isEqualTo(5.0);
    }

    List<Comment> commentList() {
        final List<Comment> comments = new ArrayList<>();
        final Comment commentA = getComment("A");
        final Comment commentB = getComment("B");
        final Comment commentC = getComment("C");

        Comment commentD = getComment("D");
        commentD.getSpec().getSubjectRef().setName("test-post");

        final Comment commentE = getComment("E");
        commentE.getSpec().setApproved(false);

        final Comment commentF = getComment("F");
        commentF.getSpec().setApproved(false);

        comments.add(commentA);
        comments.add(commentB);
        comments.add(commentC);
        comments.add(commentD);
        comments.add(commentE);
        comments.add(commentF);
        return comments;
    }

    private Comment getComment(String name) {
        final Ref ref = getRef();
        Comment comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName(name);
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setSubjectRef(ref);
        comment.getSpec().setApproved(true);
        comment.getSpec().setLastReadTime(now.plusSeconds(5));
        comment.setStatus(new Comment.CommentStatus());
        return comment;
    }

    private static Ref getRef() {
        Ref ref = new Ref();
        ref.setGroup("content.halo.run");
        ref.setVersion("v1alpha1");
        ref.setKind("Post");
        ref.setName("fake-post");
        return ref;
    }

    List<Reply> replyList() {
        Reply replyA = new Reply();
        replyA.setMetadata(new Metadata());
        replyA.getMetadata().setName("reply-A");
        replyA.getMetadata().setCreationTimestamp(now.plusSeconds(6));

        Reply replyB = new Reply();
        replyB.setMetadata(new Metadata());
        replyB.getMetadata().setName("reply-B");
        replyB.getMetadata().setCreationTimestamp(now.plusSeconds(5));

        Reply replyC = new Reply();
        replyC.setMetadata(new Metadata());
        replyC.getMetadata().setName("reply-C");
        replyC.getMetadata().setCreationTimestamp(now.plusSeconds(4));
        return List.of(replyA, replyB, replyC);
    }
}