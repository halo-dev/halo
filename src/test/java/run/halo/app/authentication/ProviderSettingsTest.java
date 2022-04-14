package run.halo.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import run.halo.app.identity.authentication.ProviderSettings;

/**
 * Tests for {@link ProviderSettings}.
 *
 * @author guqing
 * @date 2022-04-14
 */
public class ProviderSettingsTest {

    @Test
    public void buildWhenDefaultThenDefaultsAreSet() {
        ProviderSettings providerSettings = ProviderSettings.builder().build();

        assertThat(providerSettings.getIssuer()).isNull();
        assertThat(providerSettings.getAuthorizationEndpoint()).isEqualTo("/oauth2/authorize");
        assertThat(providerSettings.getTokenEndpoint()).isEqualTo("/oauth2/token");
        assertThat(providerSettings.getJwkSetEndpoint()).isEqualTo("/oauth2/jwks");
    }

    @Test
    public void buildWhenSettingsProvidedThenSet() {
        String authorizationEndpoint = "/oauth2/v1/authorize";
        String tokenEndpoint = "/oauth2/v1/token";
        String jwkSetEndpoint = "/oauth2/v1/jwks";
        String issuer = "https://example.com:9000";

        ProviderSettings providerSettings = ProviderSettings.builder()
            .issuer(issuer)
            .authorizationEndpoint(authorizationEndpoint)
            .tokenEndpoint(tokenEndpoint)
            .jwkSetEndpoint(jwkSetEndpoint)
            .build();

        assertThat(providerSettings.getIssuer()).isEqualTo(issuer);
        assertThat(providerSettings.getAuthorizationEndpoint()).isEqualTo(authorizationEndpoint);
        assertThat(providerSettings.getTokenEndpoint()).isEqualTo(tokenEndpoint);
        assertThat(providerSettings.getJwkSetEndpoint()).isEqualTo(jwkSetEndpoint);
    }

    @Test
    public void settingWhenCustomThenSet() {
        ProviderSettings providerSettings = ProviderSettings.builder()
            .setting("name1", "value1")
            .settings(settings -> settings.put("name2", "value2"))
            .build();

        assertThat(providerSettings.getSettings()).hasSize(5);
        assertThat(providerSettings.<String>getSetting("name1")).isEqualTo("value1");
        assertThat(providerSettings.<String>getSetting("name2")).isEqualTo("value2");
    }

    @Test
    public void issuerWhenNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().issuer(null))
            .withMessage("value cannot be null");
    }

    @Test
    public void authorizationEndpointWhenNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().authorizationEndpoint(null))
            .withMessage("value cannot be null");
    }

    @Test
    public void tokenEndpointWhenNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().tokenEndpoint(null))
            .withMessage("value cannot be null");
    }

    @Test
    public void jwksEndpointWhenNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().jwkSetEndpoint(null))
            .withMessage("value cannot be null");
    }
}
