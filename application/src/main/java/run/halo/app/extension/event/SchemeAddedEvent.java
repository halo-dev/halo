package run.halo.app.extension.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.extension.Scheme;

/**
 * Event published when a scheme is added.
 *
 * @author johnniang
 */
public class SchemeAddedEvent extends ApplicationEvent {

    @Getter
    private final Scheme scheme;

    public SchemeAddedEvent(Object source, Scheme scheme) {
        super(source);
        this.scheme = scheme;
    }

}
