package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;

public class PostUnpublishedEvent extends ApplicationEvent implements PostEvent {

    private final String postName;

    public PostUnpublishedEvent(Object source, String postName) {
        super(source);
        this.postName = postName;
    }

    @Override
    public String getName() {
        return postName;
    }

}
