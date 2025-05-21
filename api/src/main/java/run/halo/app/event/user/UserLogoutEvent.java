package run.halo.app.event.user;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.User;
import run.halo.app.plugin.SharedEvent;

/**
 * User logout event.
 *
 * @author lywq
 **/
@SharedEvent
public class UserLogoutEvent extends ApplicationEvent {

    @Getter
    private final User user;

    @Getter
    private final Boolean logoutStatus;

    public UserLogoutEvent(Object source, User user, Boolean logoutStatus) {
        super(source);
        this.user = user;
        this.logoutStatus = logoutStatus;
    }
}
