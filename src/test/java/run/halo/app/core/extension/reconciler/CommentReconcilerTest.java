package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Reconciler;

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

    @Mock
    SchemeManager schemeManager;

    private CommentReconciler commentReconciler;

    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        commentReconciler = new CommentReconciler(client, schemeManager);
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
    void compatibleCreationTime() {
        Comment comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName("fake-comment");
        comment.setSpec(new Comment.CommentSpec());
        comment.getSpec().setApprovedTime(Instant.now());
        comment.getSpec().setCreationTime(null);
        when(client.fetch(eq(Comment.class), eq("fake-comment")))
            .thenReturn(Optional.of(comment));

        commentReconciler.compatibleCreationTime("fake-comment");

        verify(client, times(1)).fetch(eq(Comment.class), eq("fake-comment"));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(client, times(1)).update(captor.capture());
        Comment updated = captor.getValue();
        assertThat(updated.getSpec().getCreationTime()).isNotNull();
        assertThat(updated.getSpec().getCreationTime())
            .isEqualTo(updated.getSpec().getApprovedTime());
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