package run.halo.app.event.post;

import run.halo.app.core.extension.content.Post;
import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class PostDeletedEvent extends PostEvent {

    private final Post post;

    public PostDeletedEvent(Object source, Post post) {
        super(source, post.getMetadata().getName());
        this.post = post;
    }

    /**
     * Get original post.
     *
     * @return original post.
     */
    public Post getPost() {
        return post;
    }
}
