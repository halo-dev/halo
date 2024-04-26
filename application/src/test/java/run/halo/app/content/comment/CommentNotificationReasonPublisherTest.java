package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.event.post.CommentCreatedEvent;
import run.halo.app.event.post.ReplyCreatedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.ReasonPayload;
import run.halo.app.notification.UserIdentity;
import run.halo.app.plugin.ExtensionComponentsFinder;

/**
 * Tests for {@link CommentNotificationReasonPublisher}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class CommentNotificationReasonPublisherTest {

    @Mock
    private ExtensionClient client;

    @Mock
    CommentNotificationReasonPublisher.NewCommentOnPostReasonPublisher
        newCommentOnPostReasonPublisher;

    @Mock
    CommentNotificationReasonPublisher.NewCommentOnPageReasonPublisher
        newCommentOnPageReasonPublisher;

    @Mock
    CommentNotificationReasonPublisher.NewReplyReasonPublisher newReplyReasonPublisher;

    @InjectMocks
    private CommentNotificationReasonPublisher reasonPublisher;


    @Test
    void onNewCommentTest() {
        var comment = mock(Comment.class);
        var spyReasonPublisher = spy(reasonPublisher);

        doReturn(true).when(spyReasonPublisher).isPostComment(eq(comment));

        var event = new CommentCreatedEvent(this, comment);
        spyReasonPublisher.onNewComment(event);

        verify(newCommentOnPostReasonPublisher).publishReasonBy(eq(comment));

        doReturn(false).when(spyReasonPublisher).isPostComment(eq(comment));
        doReturn(true).when(spyReasonPublisher).isPageComment(eq(comment));

        spyReasonPublisher.onNewComment(event);
        verify(newCommentOnPageReasonPublisher).publishReasonBy(eq(comment));
    }

    @Test
    void onNewReplyTest() {
        var reply = mock(Reply.class);
        var spec = mock(Reply.ReplySpec.class);
        when(reply.getSpec()).thenReturn(spec);
        when(spec.getCommentName()).thenReturn("fake-comment");

        var spyReasonPublisher = spy(reasonPublisher);
        var comment = mock(Comment.class);

        when(client.fetch(eq(Comment.class), eq("fake-comment")))
            .thenReturn(Optional.of(comment));

        var event = new ReplyCreatedEvent(this, reply);
        spyReasonPublisher.onNewReply(event);

        verify(newReplyReasonPublisher).publishReasonBy(eq(reply), eq(comment));
        verify(spec).getCommentName();
        verify(client).fetch(eq(Comment.class), eq("fake-comment"));
    }

    @Test
    void isPostCommentTest() {
        var comment = createComment();
        comment.getSpec()
            .setSubjectRef(Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class)));

        assertThat(reasonPublisher.isPostComment(comment)).isTrue();

        comment.getSpec()
            .setSubjectRef(Ref.of("fake-post", GroupVersionKind.fromExtension(SinglePage.class)));

        assertThat(reasonPublisher.isPostComment(comment)).isFalse();
    }

    @Test
    void isPageComment() {
        var comment = createComment();
        comment.getSpec()
            .setSubjectRef(Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class)));

        assertThat(reasonPublisher.isPageComment(comment)).isFalse();

        comment.getSpec()
            .setSubjectRef(Ref.of("fake-post", GroupVersionKind.fromExtension(SinglePage.class)));

        assertThat(reasonPublisher.isPageComment(comment)).isTrue();
    }


    @Nested
    class NewCommentOnPostReasonPublisherTest {
        @Mock
        ExtensionClient client;

        @Mock
        NotificationReasonEmitter emitter;

        @Mock
        ExtensionComponentsFinder extensionComponentsFinder;

        @Mock
        ExternalLinkProcessor externalLinkProcessor;

        @InjectMocks
        CommentNotificationReasonPublisher.NewCommentOnPostReasonPublisher
            newCommentOnPostReasonPublisher;

        @Test
        void publishReasonByTest() {
            final var comment = createComment();
            comment.getSpec().getOwner().setDisplayName("fake-display-name");
            comment.getSpec().setContent("fake-comment-content");

            var post = mock(Post.class);
            final var spec = mock(Post.PostSpec.class);
            var metadata = new Metadata();
            metadata.setName("fake-post");
            when(post.getMetadata()).thenReturn(metadata);
            when(post.getStatusOrDefault()).thenReturn(new Post.PostStatus());
            when(post.getSpec()).thenReturn(spec);
            when(spec.getTitle()).thenReturn("fake-title");

            when(client.fetch(eq(Post.class), eq(metadata.getName())))
                .thenReturn(Optional.of(post));

            when(emitter.emit(eq("new-comment-on-post"), any()))
                .thenReturn(Mono.empty());

            newCommentOnPostReasonPublisher.publishReasonBy(comment);

            verify(client).fetch(eq(Post.class), eq(metadata.getName()));
            verify(emitter).emit(eq("new-comment-on-post"), assertArg(consumer -> {
                var builder = ReasonPayload.builder();
                consumer.accept(builder);
                var reasonPayload = builder.build();
                var reasonSubject = Reason.Subject.builder()
                    .apiVersion(post.getApiVersion())
                    .kind(post.getKind())
                    .name(post.getMetadata().getName())
                    .title(post.getSpec().getTitle())
                    .build();
                assertThat(reasonPayload.getSubject()).isEqualTo(reasonSubject);

                assertThat(reasonPayload.getAuthor())
                    .isEqualTo(
                        UserIdentity.anonymousWithEmail(comment.getSpec().getOwner().getName()));

                assertThat(reasonPayload.getAttributes()).containsAllEntriesOf(Map.of(
                    "postName", post.getMetadata().getName(),
                    "postTitle", post.getSpec().getTitle(),
                    "commenter", comment.getSpec().getOwner().getDisplayName(),
                    "content", comment.getSpec().getContent(),
                    "commentName", comment.getMetadata().getName()
                ));
            }));
        }

        @Test
        void doNotEmitReasonTest() {
            final var comment = createComment();
            var commentOwner = new Comment.CommentOwner();
            commentOwner.setKind(User.KIND);
            commentOwner.setName("fake-user");
            comment.getSpec().setOwner(commentOwner);

            var post = new Post();
            post.setMetadata(new Metadata());
            post.getMetadata().setName("fake-post");
            post.setSpec(new Post.PostSpec());
            post.getSpec().setOwner("fake-user");

            // the username is the same as the comment owner
            assertThat(newCommentOnPostReasonPublisher.doNotEmitReason(comment, post)).isTrue();

            // not the same username
            commentOwner.setName("other");
            assertThat(newCommentOnPostReasonPublisher.doNotEmitReason(comment, post)).isFalse();

            // the comment owner is email and the same as the post-owner user email
            commentOwner.setKind(Comment.CommentOwner.KIND_EMAIL);
            commentOwner.setName("example@example.com");
            var user = new User();
            user.setSpec(new User.UserSpec());
            user.getSpec().setEmail("example@example.com");
            when(client.fetch(eq(User.class), eq("fake-user")))
                .thenReturn(Optional.of(user));

            assertThat(newCommentOnPostReasonPublisher.doNotEmitReason(comment, post)).isTrue();

            // the comment owner is email and not the same as the post-owner user email
            user.getSpec().setEmail("fake@example.com");
            assertThat(newCommentOnPostReasonPublisher.doNotEmitReason(comment, post)).isFalse();
        }
    }

    @Nested
    class NewCommentOnPageReasonPublisherTest {
        @Mock
        ExtensionClient client;

        @Mock
        NotificationReasonEmitter emitter;

        @Mock
        ExtensionComponentsFinder extensionComponentsFinder;

        @Mock
        ExternalLinkProcessor externalLinkProcessor;

        @InjectMocks
        CommentNotificationReasonPublisher.NewCommentOnPageReasonPublisher
            newCommentOnPageReasonPublisher;

        @Test
        void publishReasonByTest() {
            final var comment = createComment();
            comment.getSpec().getOwner().setDisplayName("fake-display-name");
            comment.getSpec().setContent("fake-comment-content");
            comment.getSpec().setSubjectRef(
                Ref.of("fake-page", GroupVersionKind.fromExtension(SinglePage.class)));

            var page = mock(SinglePage.class);
            final var spec = mock(SinglePage.SinglePageSpec.class);
            var metadata = new Metadata();
            metadata.setName("fake-page");
            when(page.getMetadata()).thenReturn(metadata);
            when(page.getStatusOrDefault()).thenReturn(new SinglePage.SinglePageStatus());
            when(page.getSpec()).thenReturn(spec);
            when(spec.getTitle()).thenReturn("fake-title");

            when(client.fetch(eq(SinglePage.class), eq(metadata.getName())))
                .thenReturn(Optional.of(page));

            when(emitter.emit(eq("new-comment-on-single-page"), any()))
                .thenReturn(Mono.empty());

            newCommentOnPageReasonPublisher.publishReasonBy(comment);

            verify(client).fetch(eq(SinglePage.class), eq(metadata.getName()));
            verify(emitter).emit(eq("new-comment-on-single-page"), assertArg(consumer -> {
                var builder = ReasonPayload.builder();
                consumer.accept(builder);
                var reasonPayload = builder.build();
                var reasonSubject = Reason.Subject.builder()
                    .apiVersion(page.getApiVersion())
                    .kind(page.getKind())
                    .name(page.getMetadata().getName())
                    .title(page.getSpec().getTitle())
                    .build();
                assertThat(reasonPayload.getSubject()).isEqualTo(reasonSubject);

                assertThat(reasonPayload.getAuthor())
                    .isEqualTo(
                        UserIdentity.anonymousWithEmail(comment.getSpec().getOwner().getName()));

                assertThat(reasonPayload.getAttributes()).containsAllEntriesOf(Map.of(
                    "pageName", page.getMetadata().getName(),
                    "pageTitle", page.getSpec().getTitle(),
                    "commenter", comment.getSpec().getOwner().getDisplayName(),
                    "content", comment.getSpec().getContent(),
                    "commentName", comment.getMetadata().getName()
                ));
            }));
        }

        @Test
        void doNotEmitReasonTest() {
            final var comment = createComment();
            var commentOwner = new Comment.CommentOwner();
            commentOwner.setKind(User.KIND);
            commentOwner.setName("fake-user");
            comment.getSpec().setOwner(commentOwner);

            var page = new SinglePage();
            page.setMetadata(new Metadata());
            page.getMetadata().setName("fake-page");
            page.setSpec(new SinglePage.SinglePageSpec());
            page.getSpec().setOwner("fake-user");

            // the username is the same as the comment owner
            assertThat(newCommentOnPageReasonPublisher.doNotEmitReason(comment, page)).isTrue();

            // not the same username
            commentOwner.setName("other");
            assertThat(newCommentOnPageReasonPublisher.doNotEmitReason(comment, page)).isFalse();

            // the comment owner is email and the same as the page-owner user email
            commentOwner.setKind(Comment.CommentOwner.KIND_EMAIL);
            commentOwner.setName("example@example.com");
            var user = new User();
            user.setSpec(new User.UserSpec());
            user.getSpec().setEmail("example@example.com");
            when(client.fetch(eq(User.class), eq("fake-user")))
                .thenReturn(Optional.of(user));

            assertThat(newCommentOnPageReasonPublisher.doNotEmitReason(comment, page)).isTrue();

            // the comment owner is email and not the same as the post-owner user email
            user.getSpec().setEmail("fake@example.com");
            assertThat(newCommentOnPageReasonPublisher.doNotEmitReason(comment, page)).isFalse();
        }
    }

    @Nested
    class NewReplyReasonPublisherTest {

        @Mock
        ExtensionClient client;

        @Mock
        NotificationReasonEmitter notificationReasonEmitter;

        @Mock
        ExtensionComponentsFinder extensionComponentsFinder;

        @InjectMocks
        CommentNotificationReasonPublisher.NewReplyReasonPublisher newReplyReasonPublisher;

        @Test
        void publishReasonByTest() {
            var reply = createReply("fake-reply");

            reply.getSpec().setQuoteReply("fake-quote-reply");
            var quoteReply = createReply("fake-quote-reply");

            when(client.fetch(eq(Reply.class), eq("fake-quote-reply")))
                .thenReturn(Optional.of(quoteReply));

            var spyNewReplyReasonPublisher = spy(newReplyReasonPublisher);

            var comment = createComment();
            comment.getSpec().setContent("fake-comment-content");

            doReturn(false).when(spyNewReplyReasonPublisher)
                .doNotEmitReason(any(), any(), any());
            when(notificationReasonEmitter.emit(any(), any()))
                .thenReturn(Mono.empty());

            // execute target method
            spyNewReplyReasonPublisher.publishReasonBy(reply, comment);

            verify(notificationReasonEmitter)
                .emit(eq(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU), assertArg(consumer -> {
                    var builder = ReasonPayload.builder();
                    consumer.accept(builder);
                    var reasonPayload = builder.build();
                    var reasonSubject = Reason.Subject.builder()
                        .apiVersion(quoteReply.getApiVersion())
                        .kind(quoteReply.getKind())
                        .name(quoteReply.getMetadata().getName())
                        .title(quoteReply.getSpec().getContent())
                        .build();
                    assertThat(reasonPayload.getSubject()).isEqualTo(reasonSubject);

                    assertThat(reasonPayload.getAuthor())
                        .isEqualTo(
                            UserIdentity.of(reply.getSpec().getOwner().getName()));

                    assertThat(reasonPayload.getAttributes()).containsAllEntriesOf(Map.of(
                        "commentContent", comment.getSpec().getContent(),
                        "isQuoteReply", true,
                        "quoteContent", quoteReply.getSpec().getContent(),
                        "commentName", comment.getMetadata().getName(),
                        "replier", reply.getSpec().getOwner().getDisplayName(),
                        "content", reply.getSpec().getContent(),
                        "replyName", reply.getMetadata().getName()
                    ));
                }));
        }

        @Test
        void doNotEmitReasonTest() {
            final var currentReply = createReply("current");
            currentReply.getSpec().setQuoteReply("quote");
            final var quoteReply = createReply("quote");
            final var comment = createComment();

            assertThat(newReplyReasonPublisher
                .doNotEmitReason(currentReply, quoteReply, comment)).isTrue();

            currentReply.getSpec().getOwner().setName("other");
            assertThat(newReplyReasonPublisher
                .doNotEmitReason(currentReply, quoteReply, comment)).isFalse();

            currentReply.getSpec().setQuoteReply(null);
            assertThat(newReplyReasonPublisher
                .doNotEmitReason(currentReply, quoteReply, comment)).isFalse();

            currentReply.getSpec().setOwner(comment.getSpec().getOwner());
            assertThat(newReplyReasonPublisher
                .doNotEmitReason(currentReply, quoteReply, comment)).isTrue();
        }

        static Reply createReply(String name) {
            var reply = new Reply();
            reply.setMetadata(new Metadata());
            reply.getMetadata().setName(name);
            reply.setSpec(new Reply.ReplySpec());
            reply.getSpec().setCommentName("fake-comment");
            var owner = new Comment.CommentOwner();
            owner.setKind(User.KIND);
            owner.setName("fake-user");
            owner.setDisplayName("fake-display-name");
            reply.getSpec().setOwner(owner);
            reply.getSpec().setContent("fake-reply-content");
            return reply;
        }
    }

    static Comment createComment() {
        var comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName("fake-comment");
        comment.setSpec(new Comment.CommentSpec());
        var commentOwner = new Comment.CommentOwner();
        commentOwner.setKind(Comment.CommentOwner.KIND_EMAIL);
        commentOwner.setName("example@example.com");
        comment.getSpec().setOwner(commentOwner);
        comment.getSpec().setSubjectRef(
            Ref.of("fake-post", GroupVersionKind.fromExtension(Post.class)));
        return comment;
    }
}
