package run.halo.app.theme.finders.vo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.content.comment.OwnerInfo;
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

    @Schema(requiredMode = REQUIRED)
    private MetadataOperator metadata;

    @Schema(requiredMode = REQUIRED)
    private Reply.ReplySpec spec;

    @Schema(requiredMode = REQUIRED)
    private OwnerInfo owner;

    @Schema(requiredMode = REQUIRED)
    private CommentStatsVo stats;

    /**
     * Convert {@link Reply} to {@link ReplyVo}.
     *
     * @param reply reply extension
     * @return a value object for {@link Reply}
     */
    public static ReplyVo from(Reply reply) {
        Reply.ReplySpec spec = reply.getSpec();
        return ReplyVo.builder()
            .metadata(reply.getMetadata())
            .spec(spec)
            .build();
    }
}
