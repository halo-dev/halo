package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;

/**
 * An abstract class for post events.
 *
 * @author johnniang
 */
public abstract class PostEvent extends ApplicationEvent {

    private final String name;

    public PostEvent(Object source, String name) {
        super(source);
        this.name = name;
    }

    /**
     * Gets post metadata name.
     *
     * @return post metadata name
     */
    public String getName() {
        return name;
    }
}
