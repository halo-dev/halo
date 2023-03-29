package run.halo.app.infra;

import org.springframework.context.ApplicationEvent;

/**
 * ExtensionInitializedEvent is fired after extensions have been initialized completely.
 *
 * @author johnniang
 */
public class ExtensionInitializedEvent extends ApplicationEvent {

    public ExtensionInitializedEvent(Object source) {
        super(source);
    }

}
