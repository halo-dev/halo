package run.halo.app.event.logger;

import org.springframework.context.ApplicationEvent;

/**
 * @author Holldean
 * @date 2020-05-17
 */
public class VisitorLogEvent extends ApplicationEvent {

    private final String ipAddress;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source   the object on which the event initially occurred (never {@code null})
     * @param ipAddress visitor's ip address
     */
    public VisitorLogEvent(Object source, String ipAddress) {
        super(source);

        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
