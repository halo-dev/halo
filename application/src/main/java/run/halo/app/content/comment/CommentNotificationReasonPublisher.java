package run.halo.app.content.comment;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static run.halo.app.content.comment.ReplyNotificationSubscriptionHelper.identityFrom;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.event.post.CommentCreatedEvent;
import run.halo.app.event.post.ReplyCreatedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Ref;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.plugin.ExtensionComponentsFinder;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Notification reason publisher for {@link Comment} and {@link Reply}.
 *
 * @author guqing
 * @since 2.9.0
 */
@Component
@RequiredArgsConstructor
public class CommentNotificationReasonPublisher {
    private static final GroupVersionKind POST_GVK = GroupVersionKind.fromExtension(Post.class);
    private static final GroupVersionKind PAGE_GVK =
        GroupVersionKind.fromExtension(SinglePage.class);

    private final ExtensionClient client;
    private final NewCommentOnPostReasonPublisher newCommentOnPostReasonPublisher;
    private final NewCommentOnPageReasonPublisher newCommentOnPageReasonPublisher;
    private final NewReplyReasonPublisher newReplyReasonPublisher;

    /**
     * On new comment.
     */
    @Async
    @EventListener(CommentCreatedEvent.class)
    public void onNewComment(CommentCreatedEvent event) {
        Comment comment = event.getComment();
        if (isPostComment(comment)) {
            newCommentOnPostReasonPublisher.publishReasonBy(comment);
        } else if (isPageComment(comment)) {
            newCommentOnPageReasonPublisher.publishReasonBy(comment);
        }
    }

    /**
     * On new reply.
     */
    @Async
    @EventListener(ReplyCreatedEvent.class)
    public void onNewReply(ReplyCreatedEvent event) {
        Reply reply = event.getReply();
        var commentName = reply.getSpec().getCommentName();
        client.fetch(Comment.class, commentName)
            .ifPresent(comment -> newReplyReasonPublisher.publishReasonBy(reply, comment));
    }

    boolean isPostComment(Comment comment) {
        return Ref.groupKindEquals(comment.getSpec().getSubjectRef(), POST_GVK);
    }

    boolean isPageComment(Comment comment) {
        return Ref.groupKindEquals(comment.getSpec().getSubjectRef(), PAGE_GVK);
    }

    @Component
    @RequiredArgsConstructor
    static class NewCommentOnPostReasonPublisher {

        private final ExtensionClient client;
        private final NotificationReasonEmitter notificationReasonEmitter;
        private final ExternalLinkProcessor externalLinkProcessor;

        public void publishReasonBy(Comment comment) {
            Ref subjectRef = comment.getSpec().getSubjectRef();
            Post post = client.fetch(Post.class, subjectRef.getName()).orElseThrow();
            if (doNotEmitReason(comment, post)) {
                return;
            }

            String postUrl =
                externalLinkProcessor.processLink(post.getStatusOrDefault().getPermalink());
            var reasonSubject = Reason.Subject.builder()
                .apiVersion(post.getApiVersion())
                .kind(post.getKind())
                .name(subjectRef.getName())
                .title(post.getSpec().getTitle())
                .url(postUrl)
                .build();
            Comment.CommentOwner owner = comment.getSpec().getOwner();
            notificationReasonEmitter.emit(NotificationReasonConst.NEW_COMMENT_ON_POST,
                builder -> {
                    var attributes = CommentOnPostReasonData.builder()
                        .postName(subjectRef.getName())
                        .postOwner(post.getSpec().getOwner())
                        .postTitle(post.getSpec().getTitle())
                        .postUrl(postUrl)
                        .commenter(owner.getDisplayName())
                        .content(comment.getSpec().getContent())
                        .commentName(comment.getMetadata().getName())
                        .build();
                    builder.attributes(ReasonDataConverter.toAttributeMap(attributes))
                        .author(identityFrom(owner))
                        .subject(reasonSubject);
                }).block();
        }

        boolean doNotEmitReason(Comment comment, Post post) {
            Comment.CommentOwner commentOwner = comment.getSpec().getOwner();
            return isPostOwner(post, commentOwner);
        }

        boolean isPostOwner(Post post, Comment.CommentOwner commentOwner) {
            String kind = commentOwner.getKind();
            String name = commentOwner.getName();
            var postOwner = post.getSpec().getOwner();
            if (Comment.CommentOwner.KIND_EMAIL.equals(kind)) {
                return client.fetch(User.class, postOwner)
                    .filter(user -> name.equals(user.getSpec().getEmail()))
                    .isPresent();
            }
            return name.equals(postOwner);
        }

        @Builder
        record CommentOnPostReasonData(String postName, String postOwner, String postTitle,
                                       String postUrl, String commenter, String content,
                                       String commentName) {
        }
    }

    @Component
    @RequiredArgsConstructor
    static class NewCommentOnPageReasonPublisher {
        private final ExtensionClient client;
        private final NotificationReasonEmitter notificationReasonEmitter;
        private final ExternalLinkProcessor externalLinkProcessor;

        public void publishReasonBy(Comment comment) {
            Ref subjectRef = comment.getSpec().getSubjectRef();
            var singlePage = client.fetch(SinglePage.class, subjectRef.getName()).orElseThrow();

            if (doNotEmitReason(comment, singlePage)) {
                return;
            }

            var pageUrl = externalLinkProcessor
                .processLink(singlePage.getStatusOrDefault().getPermalink());

            var reasonSubject = Reason.Subject.builder()
                .apiVersion(singlePage.getApiVersion())
                .kind(singlePage.getKind())
                .name(subjectRef.getName())
                .title(singlePage.getSpec().getTitle())
                .url(pageUrl)
                .build();

            Comment.CommentOwner owner = comment.getSpec().getOwner();
            notificationReasonEmitter.emit(NotificationReasonConst.NEW_COMMENT_ON_PAGE,
                builder -> {
                    var attributes = CommentOnPageReasonData.builder()
                        .pageName(subjectRef.getName())
                        .pageOwner(singlePage.getSpec().getOwner())
                        .pageTitle(singlePage.getSpec().getTitle())
                        .pageUrl(pageUrl)
                        .commenter(defaultIfBlank(owner.getDisplayName(), owner.getName()))
                        .content(comment.getSpec().getContent())
                        .commentName(comment.getMetadata().getName())
                        .build();
                    builder.attributes(ReasonDataConverter.toAttributeMap(attributes))
                        .author(identityFrom(owner))
                        .subject(reasonSubject);
                }).block();
        }

        public boolean doNotEmitReason(Comment comment, SinglePage page) {
            Comment.CommentOwner commentOwner = comment.getSpec().getOwner();
            return isPageOwner(page, commentOwner);
        }

        boolean isPageOwner(SinglePage page, Comment.CommentOwner commentOwner) {
            String kind = commentOwner.getKind();
            String name = commentOwner.getName();
            var pageOwner = page.getSpec().getOwner();
            if (Comment.CommentOwner.KIND_EMAIL.equals(kind)) {
                return client.fetch(User.class, pageOwner)
                    .filter(user -> name.equals(user.getSpec().getEmail()))
                    .isPresent();
            }
            return name.equals(pageOwner);
        }

        @Builder
        record CommentOnPageReasonData(String pageName, String pageOwner, String pageTitle,
                                       String pageUrl, String commenter, String content,
                                       String commentName) {
        }
    }

    @UtilityClass
    static class ReasonDataConverter {
        public static <T> Map<String, Object> toAttributeMap(T data) {
            Assert.notNull(data, "Reason attributes must not be null");
            return JsonUtils.mapper().convertValue(data, new TypeReference<>() {
            });
        }
    }

    @Component
    @RequiredArgsConstructor
    static class NewReplyReasonPublisher {
        private final ExtensionClient client;
        private final NotificationReasonEmitter notificationReasonEmitter;
        private final ExtensionComponentsFinder extensionComponentsFinder;

        public void publishReasonBy(Reply reply, Comment comment) {
            boolean isQuoteReply = StringUtils.isNotBlank(reply.getSpec().getQuoteReply());

            Optional<Reply> quoteReplyOptional = Optional.of(isQuoteReply)
                .filter(Boolean::booleanValue)
                .flatMap(isQuote -> client.fetch(Reply.class, reply.getSpec().getQuoteReply()));

            if (doNotEmitReason(reply, quoteReplyOptional.orElse(null), comment)) {
                return;
            }

            var reasonSubject = quoteReplyOptional
                .map(quoteReply -> Subscription.ReasonSubject.builder()
                    .apiVersion(quoteReply.getApiVersion())
                    .kind(quoteReply.getKind())
                    .name(quoteReply.getMetadata().getName())
                    .build()
                )
                .orElseGet(() -> Subscription.ReasonSubject.builder()
                    .apiVersion(comment.getApiVersion())
                    .kind(comment.getKind())
                    .name(comment.getMetadata().getName())
                    .build()
                );

            var reasonSubjectTitle = quoteReplyOptional
                .map(quoteReply -> quoteReply.getSpec().getContent())
                .orElse(comment.getSpec().getContent());

            var quoteReplyContent = quoteReplyOptional
                .map(quoteReply -> quoteReply.getSpec().getContent())
                .orElse(null);
            var replyOwner = reply.getSpec().getOwner();

            var repliedOwner = quoteReplyOptional
                .map(quoteReply -> quoteReply.getSpec().getOwner())
                .orElseGet(() -> comment.getSpec().getOwner());

            var reasonAttributesBuilder = NewReplyReasonData.builder()
                .commentContent(comment.getSpec().getContent())
                .isQuoteReply(isQuoteReply)
                .quoteContent(quoteReplyContent)
                .commentName(comment.getMetadata().getName())
                .replier(defaultIfBlank(replyOwner.getDisplayName(), replyOwner.getName()))
                .content(reply.getSpec().getContent())
                .replyName(reply.getMetadata().getName())
                .replyOwner(identityFrom(replyOwner).name())
                .repliedOwner(identityFrom(repliedOwner).name());

            getCommentSubjectDisplay(comment.getSpec().getSubjectRef())
                .ifPresent(subject -> {
                    reasonAttributesBuilder.commentSubjectTitle(subject.title());
                    reasonAttributesBuilder.commentSubjectUrl(subject.url());
                });

            notificationReasonEmitter.emit(NotificationReasonConst.SOMEONE_REPLIED_TO_YOU,
                builder -> {
                    var data = ReasonDataConverter.toAttributeMap(reasonAttributesBuilder.build());
                    builder.attributes(data)
                        .author(identityFrom(replyOwner))
                        .subject(Reason.Subject.builder()
                            .apiVersion(reasonSubject.getApiVersion())
                            .kind(reasonSubject.getKind())
                            .name(reasonSubject.getName())
                            .title(reasonSubjectTitle)
                            .build());
                }).block();
        }

        /**
         * To be compatible with older versions, it may be empty, so use optional.
         * TODO use {@link ExtensionGetter} instead of {@code extensionComponentsFinder}
         */
        @SuppressWarnings("unchecked")
        Optional<CommentSubject.SubjectDisplay> getCommentSubjectDisplay(Ref ref) {
            return extensionComponentsFinder.getExtensions(CommentSubject.class)
                .stream()
                .filter(commentSubject -> commentSubject.supports(ref))
                .findFirst()
                .flatMap(commentSubject
                    -> commentSubject.getSubjectDisplay(ref.getName()).blockOptional());
        }

        boolean doNotEmitReason(Reply currentReply, Reply quoteReply, Comment comment) {
            boolean isQuoteReply = StringUtils.isNotBlank(currentReply.getSpec().getQuoteReply());

            if (isQuoteReply && quoteReply == null) {
                throw new IllegalArgumentException(
                    "quoteReply can not be null when currentReply is reply to quote");
            }

            Comment.CommentOwner commentOwner = isQuoteReply ? quoteReply.getSpec().getOwner()
                : comment.getSpec().getOwner();

            var currentReplyOwner = currentReply.getSpec().getOwner();
            // reply to oneself do not emit reason
            return currentReplyOwner.getKind().equals(commentOwner.getKind())
                && currentReplyOwner.getName().equals(commentOwner.getName());
        }

        @Builder
        record NewReplyReasonData(String commentContent, String commentSubjectTitle,
                                  String commentSubjectUrl, boolean isQuoteReply,
                                  String quoteContent,
                                  String commentName, String replier, String content,
                                  String replyName, String replyOwner, String repliedOwner) {
        }
    }
}
