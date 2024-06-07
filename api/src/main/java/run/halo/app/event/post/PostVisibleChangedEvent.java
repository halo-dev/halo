package run.halo.app.event.post;

import org.springframework.lang.Nullable;
import run.halo.app.core.extension.content.Post;
import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class PostVisibleChangedEvent extends PostEvent {

    @Nullable
    private final Post.VisibleEnum oldVisible;

    private final Post.VisibleEnum newVisible;

    public PostVisibleChangedEvent(Object source, String postName,
        @Nullable Post.VisibleEnum oldVisible, Post.VisibleEnum newVisible) {
        super(source, postName);
        this.oldVisible = oldVisible;
        this.newVisible = newVisible;
    }

    @Nullable
    public Post.VisibleEnum getOldVisible() {
        return oldVisible;
    }

    public Post.VisibleEnum getNewVisible() {
        return newVisible;
    }
}
