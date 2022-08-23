package run.halo.app.theme.finders.vo;

import java.util.Map;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Comment;

/**
 * A value object for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class CommentVo {
    String name;

    String raw;

    String content;

    Comment.CommentOwner owner;

    String userAgent;

    Integer priority;

    Boolean top;

    Boolean allowNotification;

    Boolean approved;

    Map<String, String> annotations;

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
            .raw(spec.getRaw())
            .content(spec.getContent())
            .owner(spec.getOwner())
            .userAgent(spec.getUserAgent())
            .priority(spec.getPriority())
            .top(spec.getTop())
            .allowNotification(spec.getAllowNotification())
            .approved(spec.getApproved())
            .annotations(comment.getMetadata().getAnnotations())
            .build();
    }
}
