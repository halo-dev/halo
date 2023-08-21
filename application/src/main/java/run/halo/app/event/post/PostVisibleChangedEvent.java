package run.halo.app.event.post;

import lombok.Data;
import org.springframework.lang.Nullable;
import run.halo.app.core.extension.content.Post;

@Data
public class PostVisibleChangedEvent implements PostEvent {

    private final String postName;

    @Nullable
    private final Post.VisibleEnum oldVisible;

    private final Post.VisibleEnum newVisible;

    public PostVisibleChangedEvent(String postName, Post.VisibleEnum oldVisible,
        Post.VisibleEnum newVisible) {
        this.postName = postName;
        this.oldVisible = oldVisible;
        this.newVisible = newVisible;
    }

    @Override
    public String getName() {
        return postName;
    }

}
