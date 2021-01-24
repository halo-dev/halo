package run.halo.app.event;

import java.nio.file.Path;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author ryanwang
 * @date 2020-03-24
 */
public class StaticStorageChangedEvent extends ApplicationEvent {

    @Getter
    private final Path staticPath;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     * which the event is associated (never {@code null})
     */
    public StaticStorageChangedEvent(Object source, Path staticPath) {
        super(source);
        this.staticPath = staticPath;
    }
}
