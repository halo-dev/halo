package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;

/**
 * @author ZJamss
 */
public class PostChangeEvent extends ApplicationEvent implements PostEvent{
    private final String name;

    public PostChangeEvent(Object source, String post) {
        super(source);
        this.name = post;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
