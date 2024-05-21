package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PostScheduledToPublish extends ApplicationEvent implements PostEvent {
    private final String name;

    public PostScheduledToPublish(Object source, String name) {
        super(source);
        this.name = name;
    }
}
