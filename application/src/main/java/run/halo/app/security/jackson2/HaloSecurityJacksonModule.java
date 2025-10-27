package run.halo.app.security.jackson2;

import org.springframework.security.jackson.SecurityJacksonModule;
import run.halo.app.security.authentication.login.HaloUser;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;
import tools.jackson.core.Version;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Halo security Jackson2 module.
 *
 * <p>
 * Only registers while serializing and deserializing Halo security
 * related classes.
 *
 * @author johnniang
 */
class HaloSecurityJacksonModule extends SecurityJacksonModule {

    public HaloSecurityJacksonModule() {
        super(HaloSecurityJacksonModule.class.getName(), new Version(1, 0, 0, null, null, null));
    }

    @Override
    public void configurePolymorphicTypeValidator(BasicPolymorphicTypeValidator.Builder builder) {
        builder.allowIfSubType(HaloUser.class)
            .allowIfSubType(TwoFactorAuthentication.class)
            .allowIfSubType(HaloOAuth2AuthenticationToken.class);
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(HaloUser.class, HaloUserMixin.class);
        context.setMixIn(
            TwoFactorAuthentication.class, TwoFactorAuthenticationMixin.class
        );
        context.setMixIn(
            HaloOAuth2AuthenticationToken.class, HaloOAuth2AuthenticationTokenMixin.class
        );
    }

}
