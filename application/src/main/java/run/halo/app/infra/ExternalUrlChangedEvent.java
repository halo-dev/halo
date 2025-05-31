package run.halo.app.infra;

import java.net.URL;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when the external URL of the application changes.
 *
 * @author johnniang
 * @since 2.21.0
 */
public class ExternalUrlChangedEvent extends ApplicationEvent {

    @Getter
    private final URL externalUrl;

    public ExternalUrlChangedEvent(Object source, URL externalUrl) {
        super(source);
        this.externalUrl = externalUrl;
    }

}
