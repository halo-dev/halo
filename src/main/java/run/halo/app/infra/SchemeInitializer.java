package run.halo.app.infra;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Schemes;
import run.halo.app.identity.apitoken.PersonalAccessToken;
import run.halo.app.identity.apitoken.PersonalAccessTokenType;
import run.halo.app.identity.apitoken.PersonalAccessTokenUtils;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authorization.PolicyRule;
import run.halo.app.identity.authorization.Role;
import run.halo.app.infra.utils.HaloUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private final OAuth2AuthorizationService oauth2AuthorizationService;

    private final ExtensionClient extensionClient;

    public SchemeInitializer(OAuth2AuthorizationService oauth2AuthorizationService,
        ExtensionClient extensionClient) {
        this.oauth2AuthorizationService = oauth2AuthorizationService;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Schemes.INSTANCE.register(Role.class);

        // TODO These test only methods will be removed in the future
        initRoleForTesting();
        initPersonalAccessTokenForTesting();
    }


    private void initRoleForTesting() {
        Role role = new Role();
        role.setApiVersion("v1alpha1");
        role.setKind("Role");
        Metadata metadata = new Metadata();
        metadata.setName("readPostRole");
        role.setMetadata(metadata);
        List<PolicyRule> rules = List.of(
            new PolicyRule.Builder().apiGroups("").resources("posts").verbs("list", "get")
                .build(),
            new PolicyRule.Builder().apiGroups("").resources("categories").verbs("*")
                .build(),
            new PolicyRule.Builder().nonResourceURLs("/healthy").verbs("get", "post", "head")
                .build()
        );
        role.setRules(rules);

        extensionClient.create(role);
    }

    private void initPersonalAccessTokenForTesting() {
        String salt = HaloUtils.readClassPathResourceAsString("apiToken.salt");
        SecretKey secretKey = PersonalAccessTokenUtils.convertStringToSecretKey(salt);
        String tokenValue =
            PersonalAccessTokenUtils.generate(PersonalAccessTokenType.ADMIN_TOKEN, secretKey);

        Set<String> roles = Set.of("readPostRole");
        OAuth2AccessToken personalAccessToken =
            new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, Instant.now(),
                Instant.now().plus(2, ChronoUnit.HOURS), roles);

        System.out.println(
            "Initializing a personal access token is only for development or testing: "
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
