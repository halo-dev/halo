package run.halo.app.theme.finders.impl;

import run.halo.app.core.extension.Comment;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.Finder;

/**
 * A default implementation of {@link CommentFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("commentFinder")
public class CommentFinderImpl implements CommentFinder {
    private final ReactiveExtensionClient client;

    public CommentFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Comment getBySubject(String kind, String name) {
        return null;
    }

    @Override
    public ListResult<Comment> list(int page, int size) {
        return null;
    }
}
