package run.halo.app.event.post;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author guqing
 * @since 2.0.0
 */
@Getter
public abstract class VotedEvent extends ApplicationEvent {
    private final String group;
    private final String name;
    private final String plural;

    public VotedEvent(Object source, String group, String name, String plural) {
        super(source);
        this.group = group;
        this.name = name;
        this.plural = plural;
    }
}
