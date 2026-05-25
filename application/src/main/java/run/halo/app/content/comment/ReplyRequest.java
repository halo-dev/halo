package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Metadata;

/**
 * Request body for creating a {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Reply creation payload for console comment APIs.")
@Data
public class ReplyRequest {

    @Schema(description = "Original reply text submitted by the editor.", requiredMode = REQUIRED, minLength = 1)
    private String raw;

    @Schema(
            description = "Rendered HTML content for the reply. Unsafe HTML is rejected.",
            requiredMode = REQUIRED,
            minLength = 1)
    private String content;

    @Schema(description = "Whether to subscribe the owner to notifications for future replies.", defaultValue = "false")
    private Boolean allowNotification;

    @Schema(description = "Whether the reply should be hidden from normal display.", defaultValue = "false")
    private Boolean hidden;

    /** Guest/email owner information. If omitted, Halo uses the current authenticated user. */
    private CommentEmailOwner owner;

    /** Metadata name of the reply being quoted, if this reply quotes another reply. */
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
        spec.setHidden(hidden);
        spec.setQuoteReply(quoteReply);

        if (owner != null) {
            spec.setOwner(owner.toCommentOwner());
        }
        return reply;
    }
}
