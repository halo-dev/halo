package run.halo.app.extension.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.extension.Scheme;

/**
 * Event published when a scheme is removed.
 *
 * @author johnniang
 */
public class SchemeRemovedEvent extends ApplicationEvent {

    @Getter
    private final Scheme scheme;

    public SchemeRemovedEvent(Object source, Scheme scheme) {
        super(source);
        this.scheme = scheme;
    }
}
