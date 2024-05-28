package run.halo.app.security.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import run.halo.app.security.authentication.login.HaloUser;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

/**
 * Halo security Jackson2 module.
 *
 * @author johnniang
 */
public class HaloSecurityJackson2Module extends SimpleModule {

    public HaloSecurityJackson2Module() {
        super(HaloSecurityJackson2Module.class.getName(), new Version(1, 0, 0, null, null, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        SecurityJackson2Modules.enableDefaultTyping(context.getOwner());
        context.setMixInAnnotations(HaloUser.class, HaloUserMixin.class);
        context.setMixInAnnotations(TwoFactorAuthentication.class,
            TwoFactorAuthenticationMixin.class);
    }

}
