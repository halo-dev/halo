package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.SchemeManager;
import run.halo.app.security.authentication.pat.PersonalAccessToken;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private final SchemeManager schemeManager;

    public SchemeInitializer(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        schemeManager.register(Role.class);
        schemeManager.register(PersonalAccessToken.class);
        schemeManager.register(Plugin.class);
        schemeManager.register(RoleBinding.class);
        schemeManager.register(User.class);
        schemeManager.register(ReverseProxy.class);
    }
}
