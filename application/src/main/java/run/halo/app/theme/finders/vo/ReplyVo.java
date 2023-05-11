package run.halo.app.theme.finders.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@ToString
@EqualsAndHashCode
public class ReplyVo implements ExtensionVoOperator {

    @Schema(required = true)
    private MetadataOperator metadata;

    @Schema(required = true)
    private ReplyVo.ReplySpecVo spec;

    @Schema(required = true)
    private CommentOwnerVo owner;

    @Schema(required = true)
    private CommentStatsVo stats;

    @Data
    @Builder
    public static class ReplySpecVo {

        private String commentName;

        private String quoteReply;

        private String raw;

        private String content;

        private CommentOwnerVo owner;

        private String userAgent;

        private Instant approvedTime;

        private Instant creationTime;

        private Integer priority;

        private Boolean top;

        private Boolean allowNotification;

        private Boolean approved;

        private Boolean hidden;

        /**
         * Convert {@link Reply.ReplySpec} to {@link ReplyVo.ReplySpecVo}.
         *
         * @param spec reply spec
         * @return a value object for {@link Reply.ReplySpec}
         */
        public static ReplySpecVo from(Reply.ReplySpec spec) {
            return ReplySpecVo.builder()
                .commentName(spec.getCommentName())
                .quoteReply(spec.getQuoteReply())
                .raw(spec.getRaw())
                .content(spec.getContent())
                .userAgent(spec.getUserAgent())
                .approvedTime(spec.getApprovedTime())
                .creationTime(spec.getCreationTime())
                .priority(spec.getPriority())
                .top(spec.getTop())
                .allowNotification(spec.getAllowNotification())
                .approved(spec.getApproved())
                .hidden(spec.getHidden())
                .build();
        }
    }

    /**
     * Convert {@link Reply} to {@link ReplyVo}.
     *
     * @param reply reply extension
     * @return a value object for {@link Reply}
     */
    public static ReplyVo from(Reply reply) {
        ReplyVo.ReplySpecVo replySpecVo = ReplyVo.ReplySpecVo.from(reply.getSpec());
        return ReplyVo.builder()
            .metadata(reply.getMetadata())
            .spec(replySpecVo)
            .build();
    }
}
