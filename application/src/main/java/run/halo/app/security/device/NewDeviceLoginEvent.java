package run.halo.app.security.device;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.Device;

@Getter
public class NewDeviceLoginEvent extends ApplicationEvent {
    private final Device device;

    public NewDeviceLoginEvent(Object source, Device device) {
        super(source);
        this.device = device;
    }
}
