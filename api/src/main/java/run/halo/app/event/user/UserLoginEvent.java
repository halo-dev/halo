package run.halo.app.event.user;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.User;
import run.halo.app.plugin.SharedEvent;

/**
 * User login event.
 *
 * @author lywq
 **/
@SharedEvent
public class UserLoginEvent extends ApplicationEvent {

    @Getter
    private final User user;

    @Getter
    private final Boolean loginStatus;

    public UserLoginEvent(Object source, User user, Boolean loginStatus) {
        super(source);
        this.user = user;
        this.loginStatus = loginStatus;
    }
}
