package run.halo.app.theme.finders.vo;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import run.halo.app.core.extension.Reply;

/**
 * A value object for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ReplyVo extends BaseCommentVo {

    String commentName;

    String quoteReply;

    /**
     * Convert {@link Reply} to {@link ReplyVo}.
     *
     * @param reply reply extension
     * @return a value object for {@link Reply}
     */
    public static ReplyVo from(Reply reply) {
        Reply.ReplySpec spec = reply.getSpec();
        return ReplyVo.builder()
            .name(reply.getMetadata().getName())
            .commentName(spec.getCommentName())
            .quoteReply(spec.getQuoteReply())
            .raw(spec.getRaw())
            .content(spec.getContent())
            .owner(spec.getOwner())
            .userAgent(spec.getUserAgent())
            .priority(spec.getPriority())
            .top(spec.getTop())
            .allowNotification(spec.getAllowNotification())
            .annotations(reply.getMetadata().getAnnotations())
            .build();
    }
}
