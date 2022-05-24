package run.halo.app.identity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import run.halo.app.identity.apitoken.PersonalAccessToken;
import run.halo.app.identity.apitoken.PersonalAccessTokenUtils;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.infra.utils.HaloUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    private OAuth2AuthorizationService oauth2AuthorizationService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        initPersonalAccessTokenForTesting();
    }

    private void initPersonalAccessTokenForTesting() {
        String salt = HaloUtils.readClassPathResourceAsString("apiToken.salt");
        SecretKey secretKey = PersonalAccessTokenUtils.convertStringToSecretKey(salt);
        String tokenValue = PersonalAccessTokenUtils.generate(secretKey);

        OAuth2AccessToken personalAccessToken =
            new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, Instant.now(),
                Instant.now().plus(2, ChronoUnit.HOURS));

        System.out.println(
            "Initializing a personal access token is only for development or testing: ha_"
                + tokenValue);

        OAuth2Authorization authorization = new OAuth2Authorization.Builder()
            .id(HaloUtils.simpleUUID())
            .token(personalAccessToken)
            .principalName(tokenValue)
            .authorizationGrantType(PersonalAccessToken.PERSONAL_ACCESS_TOKEN)
            .build();
        oauth2AuthorizationService.save(authorization);
    }
}
