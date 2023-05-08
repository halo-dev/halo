package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;

public class PostUpdatedEvent extends ApplicationEvent implements PostEvent {

    private final String postName;

    public PostUpdatedEvent(Object source, String postName) {
        super(source);
        this.postName = postName;
    }

    @Override
    public String getName() {
        return postName;
    }
}
