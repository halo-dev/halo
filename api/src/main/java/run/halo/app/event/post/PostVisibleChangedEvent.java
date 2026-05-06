package run.halo.app.event.post;

import org.jspecify.annotations.Nullable;
import run.halo.app.core.extension.content.Post;
import run.halo.app.plugin.SharedEvent;

@SharedEvent
public class PostVisibleChangedEvent extends PostEvent {

    private final Post.@Nullable VisibleEnum oldVisible;

    private final Post.VisibleEnum newVisible;

    public PostVisibleChangedEvent(
            Object source,
            String postName,
            Post.@Nullable VisibleEnum oldVisible,
            Post.VisibleEnum newVisible) {
        super(source, postName);
        this.oldVisible = oldVisible;
        this.newVisible = newVisible;
    }

    public Post.@Nullable VisibleEnum getOldVisible() {
        return oldVisible;
    }

    public Post.VisibleEnum getNewVisible() {
        return newVisible;
    }
}
