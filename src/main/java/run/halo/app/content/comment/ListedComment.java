package run.halo.app.content.comment;

import org.springframework.util.Assert;
import run.halo.app.core.extension.Comment;
import run.halo.app.extension.Extension;

public record ListedComment(Comment comment, OwnerInfo owner, Extension subject) {

    public ListedComment {
        Assert.notNull(comment, "Comment must not be null.");
        Assert.notNull(owner, "Owner must not be null.");
        Assert.notNull(subject, "Subject must not be null.");
    }
}
