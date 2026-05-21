package run.halo.app.content.comment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Comment;

/**
 * Guest/email creator information for a comment or reply.
 *
 * @param email email for comment owner
 * @param avatar avatar for comment owner
 * @param displayName display name for comment owner
 * @param website website for comment owner
 */
@Schema(description = "Guest/email owner information used when a comment is not owned by a Halo user.")
public record CommentEmailOwner(
        @Schema(description = "Email address of the guest commenter. It is used as the owner name when present.")
        String email,

        @Schema(description = "Avatar URL of the guest commenter.")
        String avatar,

        @Schema(description = "Display name of the guest commenter.", requiredMode = REQUIRED)
        String displayName,

        @Schema(description = "Website URL of the guest commenter.")
        String website) {

    public CommentEmailOwner {
        Assert.hasText(displayName, "The 'displayName' must not be empty.");
    }

    /**
     * Converts {@link CommentEmailOwner} to {@link Comment.CommentOwner}.
     *
     * @return a comment owner
     */
    public Comment.CommentOwner toCommentOwner() {
        Comment.CommentOwner commentOwner = new Comment.CommentOwner();
        commentOwner.setKind(Comment.CommentOwner.KIND_EMAIL);
        // email nullable
        commentOwner.setName(StringUtils.defaultString(email));

        commentOwner.setDisplayName(displayName);
        Map<String, String> annotations = new LinkedHashMap<>();
        commentOwner.setAnnotations(annotations);
        annotations.put(Comment.CommentOwner.AVATAR_ANNO, avatar);
        annotations.put(Comment.CommentOwner.WEBSITE_ANNO, website);
        return commentOwner;
    }
}
