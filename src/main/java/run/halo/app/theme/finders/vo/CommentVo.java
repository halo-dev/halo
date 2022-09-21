package run.halo.app.theme.finders.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.extension.Comment;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@EqualsAndHashCode
public class CommentVo {

    @Schema(required = true)
    MetadataOperator metadata;

    @Schema(required = true)
    Comment.CommentSpec spec;

    Comment.CommentStatus status;

    @With
    @Schema(required = true)
    OwnerInfo owner;

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
