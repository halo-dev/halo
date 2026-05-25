package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;

/**
 * Request body for creating a {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Comment creation payload for console comment APIs.")
@Data
public class CommentRequest {

    @Schema(description = "Reference to the subject being commented on.", requiredMode = REQUIRED)
    private Ref subjectRef;

    /** Guest/email owner information. If omitted, Halo uses the current authenticated user. */
    private CommentEmailOwner owner;

    @Schema(description = "Original comment text submitted by the editor.", requiredMode = REQUIRED, minLength = 1)
    private String raw;

    @Schema(
            description = "Rendered HTML content for the comment. Unsafe HTML is rejected.",
            requiredMode = REQUIRED,
            minLength = 1)
    private String content;

    @Schema(description = "Whether to subscribe the owner to notifications for future replies.", defaultValue = "false")
    private Boolean allowNotification;

    @Schema(description = "Whether the comment should be hidden from normal display.", defaultValue = "false")
    private Boolean hidden;

    /**
     * Converts {@link CommentRequest} to {@link Comment}.
     *
     * @return a comment
     */
    public Comment toComment() {
        Comment comment = new Comment();
        comment.setMetadata(new Metadata());
        comment.getMetadata().setName(UUID.randomUUID().toString());

        Comment.CommentSpec spec = new Comment.CommentSpec();
        comment.setSpec(spec);
        spec.setSubjectRef(subjectRef);
        spec.setRaw(raw);
        spec.setContent(content);
        spec.setAllowNotification(allowNotification);
        spec.setHidden(hidden);

        if (owner != null) {
            spec.setOwner(owner.toCommentOwner());
        }
        return comment;
    }
}
