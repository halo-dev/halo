package run.halo.app.infra;

import java.util.Map;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when the system configuration changes.
 *
 * @author johnniang
 * @since 2.21.0
 */
public class SystemConfigChangedEvent extends ApplicationEvent {

    /**
     * Old configuration data. Unmodifiable.
     */
    @Getter
    private final Map<String, String> oldData;

    /**
     * New configuration data. Unmodifiable.
     */
    @Getter
    private final Map<String, String> newData;

    public SystemConfigChangedEvent(
        Object source,
        Map<String, String> oldData,
        Map<String, String> newData
    ) {
        super(source);
        this.oldData = Map.copyOf(oldData);
        this.newData = Map.copyOf(newData);
    }

}
