package run.halo.app.event.options;

import org.springframework.context.ApplicationEvent;

/**
 * Option updated event.
 *
 * @author johnniang
 * @date 19-4-29
 */
public class OptionUpdatedEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public OptionUpdatedEvent(Object source) {
        super(source);
    }
}
