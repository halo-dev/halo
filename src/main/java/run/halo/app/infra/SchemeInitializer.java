package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Schemes;
import run.halo.app.security.authentication.pat.PersonalAccessToken;
import run.halo.app.security.authorization.Role;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Schemes.INSTANCE.register(Role.class);
        Schemes.INSTANCE.register(PersonalAccessToken.class);
    }
}
