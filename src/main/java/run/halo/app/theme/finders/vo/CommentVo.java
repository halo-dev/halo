package run.halo.app.theme.finders.vo;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import run.halo.app.core.extension.Comment;

/**
 * A value object for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CommentVo extends BaseCommentVo {

    Comment.CommentSubjectRef subjectRef;

    /**
     * Convert {@link Comment} to {@link CommentVo}.
     *
     * @param comment comment extension
     * @return a value object for {@link Comment}
     */
    public static CommentVo from(Comment comment) {
        Comment.CommentSpec spec = comment.getSpec();
        return CommentVo.builder()
            .name(comment.getMetadata().getName())
            .subjectRef(spec.getSubjectRef())
            .raw(spec.getRaw())
            .content(spec.getContent())
            .owner(spec.getOwner())
            .userAgent(spec.getUserAgent())
            .priority(spec.getPriority())
            .top(spec.getTop())
            .allowNotification(spec.getAllowNotification())
            .annotations(comment.getMetadata().getAnnotations())
            .build();
    }
}
