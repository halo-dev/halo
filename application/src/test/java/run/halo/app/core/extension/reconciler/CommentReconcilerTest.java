package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
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

    @Mock
    ReplyService replyService;

    @InjectMocks
    private CommentReconciler commentReconciler;

    private final Instant now = Instant.now();

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
        comment.getSpec().setSubjectRef(getRef());
        comment.getSpec().setLastReadTime(now.plusSeconds(5));
        comment.setStatus(new Comment.CommentStatus());

        when(client.fetch(eq(Comment.class), eq("test")))
            .thenReturn(Optional.of(comment));

        when(replyService.removeAllByComment(eq(comment.getMetadata().getName())))
            .thenReturn(Mono.empty());
        when(client.listBy(eq(Comment.class), any(ListOptions.class), isA(PageRequest.class)))
            .thenReturn(ListResult.emptyResult());

        Reconciler.Result reconcile = commentReconciler.reconcile(new Reconciler.Request("test"));
        assertThat(reconcile.reEnqueue()).isFalse();
        assertThat(reconcile.retryAfter()).isNull();

        verify(replyService).removeAllByComment(eq(comment.getMetadata().getName()));

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

        commentReconciler.compatibleCreationTime(comment);

        assertThat(comment.getSpec().getCreationTime())
            .isEqualTo(comment.getSpec().getApprovedTime());
    }

    private static Ref getRef() {
        Ref ref = new Ref();
        ref.setGroup("content.halo.run");
        ref.setVersion("v1alpha1");
        ref.setKind("Post");
        ref.setName("fake-post");
        return ref;
    }
}
