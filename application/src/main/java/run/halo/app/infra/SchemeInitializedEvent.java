package run.halo.app.infra;

import org.springframework.context.ApplicationEvent;

public class SchemeInitializedEvent extends ApplicationEvent {

    public SchemeInitializedEvent(Object source) {
        super(source);
    }

}
