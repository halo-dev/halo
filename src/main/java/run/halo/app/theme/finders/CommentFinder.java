package run.halo.app.theme.finders;

import run.halo.app.core.extension.Comment;
import run.halo.app.extension.ListResult;

/**
 * A finder for finding {@link Comment comments} in template.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CommentFinder {

    Comment getBySubject(String kind, String name);

    ListResult<Comment> list(int page, int size);
}
