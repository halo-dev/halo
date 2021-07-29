package run.halo.app.event.menu;

import org.springframework.context.ApplicationEvent;

public class MenuUpdateEvent extends ApplicationEvent {
    public MenuUpdateEvent(Object source) {
        super(source);
    }
}
