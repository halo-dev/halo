package run.halo.app.theme.finders.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.Ref;

/**
 * A value object for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@EqualsAndHashCode
public class CommentVo implements ExtensionVoOperator {

    @Schema(required = true)
    private MetadataOperator metadata;

    @Schema(required = true)
    private CommentVo.CommentSpecVo spec;

    private Comment.CommentStatus status;

    @Schema(required = true)
    private CommentOwnerVo owner;

    @Schema(required = true)
    private CommentStatsVo stats;

    /**
     * Convert {@link Comment} to {@link CommentVo}.
     *
     * @param comment comment extension
     * @return a value object for {@link Comment}
     */
    public static CommentVo from(Comment comment) {
        CommentVo.CommentSpecVo commentSpecVo = CommentVo.CommentSpecVo.from(comment.getSpec());
        return CommentVo.builder()
            .metadata(comment.getMetadata())
            .spec(commentSpecVo)
            .status(comment.getStatus())
            .build();
    }

    @Data
    @Builder
    public static class CommentSpecVo {

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

        private Ref subjectRef;

        private Instant lastReadTime;

        /**
         * Convert {@link Comment.CommentSpec} to {@link CommentVo.CommentSpecVo}.
         *
         * @param spec comment spec
         * @return a value object for {@link Comment.CommentSpec}
         */
        public static CommentSpecVo from(Comment.CommentSpec spec) {
            return CommentSpecVo.builder()
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
                .subjectRef(spec.getSubjectRef())
                .lastReadTime(spec.getLastReadTime())
                .build();
        }
    }
}
