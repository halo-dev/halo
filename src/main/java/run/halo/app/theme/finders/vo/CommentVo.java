package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
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

    MetadataOperator metadata;

    Comment.CommentSpec spec;

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
            .build();
    }
}
