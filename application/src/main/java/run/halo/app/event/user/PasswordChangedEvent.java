package run.halo.app.event.user;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordChangedEvent extends ApplicationEvent {
    private final String username;

    public PasswordChangedEvent(Object source, String username) {
        super(source);
        this.username = username;
    }
}
