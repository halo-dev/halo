package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Schemes;
import run.halo.app.identity.authorization.Role;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Schemes.INSTANCE.register(Role.class);
    }
}
