package run.halo.app.content.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;

/**
 * Request parameter object for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class CommentRequest {

    @Schema(required = true)
    private Ref subjectRef;

    private CommentEmailOwner owner;

    @Schema(required = true, minLength = 1)
    private String raw;

    @Schema(required = true, minLength = 1)
    private String content;

    @Schema(defaultValue = "false")
    private Boolean allowNotification;

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

        if (owner != null) {
            spec.setOwner(owner.toCommentOwner());
        }
        return comment;
    }
}
