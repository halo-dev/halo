package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.Plugin;
import run.halo.app.security.authentication.pat.PersonalAccessToken;
import run.halo.app.security.authorization.Role;
import run.halo.app.security.authorization.RoleBinding;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private final SchemeManager schemeManager;

    public SchemeInitializer(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        schemeManager.register(Role.class);
        schemeManager.register(RoleBinding.class);
        schemeManager.register(PersonalAccessToken.class);
        schemeManager.register(Plugin.class);
    }
}
