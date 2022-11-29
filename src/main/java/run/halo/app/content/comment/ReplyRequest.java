package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Metadata;

/**
 * A request parameter object for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class ReplyRequest {

    @Schema(required = true, minLength = 1)
    private String raw;

    @Schema(required = true, minLength = 1)
    private String content;

    @Schema(defaultValue = "false")
    private Boolean allowNotification;

    private CommentEmailOwner owner;

    private String quoteReply;

    /**
     * Converts {@link ReplyRequest} to {@link Reply}.
     *
     * @return a reply
     */
    public Reply toReply() {
        Reply reply = new Reply();
        reply.setMetadata(new Metadata());
        reply.getMetadata().setName(UUID.randomUUID().toString());

        Reply.ReplySpec spec = new Reply.ReplySpec();
        reply.setSpec(spec);
        spec.setRaw(raw);
        spec.setContent(content);
        spec.setAllowNotification(allowNotification);
        spec.setQuoteReply(quoteReply);

        if (owner != null) {
            spec.setOwner(owner.toCommentOwner());
        }
        return reply;
    }
}
