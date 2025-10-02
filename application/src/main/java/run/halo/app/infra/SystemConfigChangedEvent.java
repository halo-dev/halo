package run.halo.app.infra;

import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when the system configuration changes.
 *
 * @author johnniang
 * @since 2.21.0
 */
public class SystemConfigChangedEvent extends ApplicationEvent {

    public SystemConfigChangedEvent(Object source) {
        super(source);
    }

}
