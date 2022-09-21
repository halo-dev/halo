package run.halo.app.theme.finders.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import run.halo.app.content.comment.OwnerInfo;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@ToString
@EqualsAndHashCode
public class ReplyVo {

    @Schema(required = true)
    MetadataOperator metadata;

    @Schema(required = true)
    Reply.ReplySpec spec;

    @With
    @Schema(required = true)
    OwnerInfo owner;

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
