package run.halo.app.content.comment;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Comment;

/**
 * <p>The creator info of the comment.</p>
 * This {@link CommentEmailOwner} is only applicable to the user who is allowed to comment
 * without logging in.
 *
 * @param email email for comment owner
 * @param avatar avatar for comment owner
 * @param displayName display name for comment owner
 * @param website website for comment owner
 */
public record CommentEmailOwner(String email, String avatar, String displayName, String website) {

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
        commentOwner.setName(StringUtils.defaultString(email, StringUtils.EMPTY));

        commentOwner.setDisplayName(displayName);
        Map<String, String> annotations = new LinkedHashMap<>();
        commentOwner.setAnnotations(annotations);
        annotations.put(Comment.CommentOwner.AVATAR_ANNO, avatar);
        annotations.put(Comment.CommentOwner.WEBSITE_ANNO, website);
        return commentOwner;
    }
}
