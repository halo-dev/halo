package run.halo.app.event.user;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.plugin.SharedEvent;

/**
 * An event that will be triggered after a user connection is disconnected.
 *
 * @author johnniang
 * @since 2.20.0
 */
@SharedEvent
public class UserConnectionDisconnectedEvent extends ApplicationEvent {

    @Getter
    private final UserConnection userConnection;

    public UserConnectionDisconnectedEvent(Object source, UserConnection userConnection) {
        super(source);
        this.userConnection = userConnection;
    }

}
