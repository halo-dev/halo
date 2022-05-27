package run.halo.app.infra;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Schemes;
import run.halo.app.extension.Unstructured;
import run.halo.app.identity.apitoken.PersonalAccessToken;
import run.halo.app.identity.apitoken.PersonalAccessTokenType;
import run.halo.app.identity.apitoken.PersonalAccessTokenUtils;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authorization.Role;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

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

        // TODO The read location of the configuration file needs to be considered later
        createUnstructured();

        // TODO These test only methods will be removed in the future
        initPersonalAccessTokenForTesting();

    }

    private void initPersonalAccessTokenForTesting() {
        String salt = HaloUtils.readClassPathResourceAsString("apiToken.salt");
        SecretKey secretKey = PersonalAccessTokenUtils.convertStringToSecretKey(salt);
        String tokenValue =
            PersonalAccessTokenUtils.generate(PersonalAccessTokenType.ADMIN_TOKEN, secretKey);

        Set<String> roles =
            Set.of("role-template-view-categories", "role-template-view-nonresources");
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

    private void createUnstructured() {
        try {
            List<Unstructured> unstructuredList = loadClassPathResourcesToUnstructured();
            for (Unstructured unstructured : unstructuredList) {
                extensionClient.create(unstructured);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Unstructured> loadClassPathResourcesToUnstructured() throws IOException {
        PathMatchingResourcePatternResolver resourcePatternResolver =
            new PathMatchingResourcePatternResolver();
        // Gets yaml resources
        Resource[] yamlResources =
            resourcePatternResolver.getResources("classpath*:extensions/*.yaml");
        Resource[] ymlResources =
            resourcePatternResolver.getResources("classpath*:extensions/*.yml");

        YamlUnstructuredLoader yamlUnstructuredLoader =
            new YamlUnstructuredLoader(ArrayUtils.addAll(ymlResources, yamlResources));
        return yamlUnstructuredLoader.load();
    }
}
