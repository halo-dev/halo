package run.halo.app.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author johnniang
 * @date 19-4-20
 */
public class LogEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LogEvent(Object source) {
        super(source);
    }
}
