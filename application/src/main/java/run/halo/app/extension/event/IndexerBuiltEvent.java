package run.halo.app.extension.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.extension.Scheme;

/**
 * IndexBuildEvent is fired when index build of a scheme is triggered and completed.
 */
public class IndexerBuiltEvent extends ApplicationEvent {

    @Getter
    private final Scheme scheme;

    public IndexerBuiltEvent(Object source, Scheme scheme) {
        super(source);
        this.scheme = scheme;
    }

}
