package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;

public class PostRecycledEvent extends ApplicationEvent {

    private final String postName;

    public PostRecycledEvent(Object source, String postName) {
        super(source);
        this.postName = postName;
    }

    public String getPostName() {
        return postName;
    }
}
