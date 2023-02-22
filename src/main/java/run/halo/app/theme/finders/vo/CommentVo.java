package run.halo.app.theme.finders.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.With;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.MetadataOperator;

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
    private Comment.CommentSpec spec;

    private Comment.CommentStatus status;

    @With
    @Schema(required = true)
    private OwnerInfo owner;

    @Schema(required = true)
    private CommentStatsVo stats;

    /**
     * Convert {@link Comment} to {@link CommentVo}.
     *
     * @param comment comment extension
     * @return a value object for {@link Comment}
     */
    public static CommentVo from(Comment comment) {
        return CommentVo.builder()
            .metadata(comment.getMetadata())
            .spec(comment.getSpec())
            .status(comment.getStatus())
            .build();
    }
}
