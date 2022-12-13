package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;

public class PostRecycledEvent extends ApplicationEvent implements PostEvent {

    private final String postName;

    public PostRecycledEvent(Object source, String postName) {
        super(source);
        this.postName = postName;
    }

    public String getName() {
        return postName;
    }
}
