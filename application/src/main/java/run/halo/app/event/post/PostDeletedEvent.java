package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.content.Post;

@Getter
public class PostDeletedEvent extends ApplicationEvent implements PostEvent {

    private final Post post;

    public PostDeletedEvent(Object source, Post post) {
        super(source);
        this.post = post;
    }

    @Override
    public String getName() {
        return post.getMetadata().getName();
    }
}
