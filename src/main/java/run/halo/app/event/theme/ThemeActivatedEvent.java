package run.halo.app.event.theme;

import org.springframework.context.ApplicationEvent;

/**
 * Theme activated event.
 *
 * @author johnniang
 * @date 19-4-20
 */
public class ThemeActivatedEvent extends ApplicationEvent {

    /**
     * Creates a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ThemeActivatedEvent(Object source) {
        super(source);
    }

}
