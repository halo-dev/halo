package run.halo.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
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
        assertThat(providerSettings.getAuthorizationEndpoint()).isEqualTo(
            "/api/v1/oauth2/authorize");
        assertThat(providerSettings.getTokenEndpoint()).isEqualTo("/api/v1/oauth2/token");
        assertThat(providerSettings.getJwkSetEndpoint()).isEqualTo("/api/v1/oauth2/jwks");
        assertThat(providerSettings.isReuseRefreshTokens()).isEqualTo(false);
        assertThat(providerSettings.getAccessTokenTimeToLive()).isEqualTo(Duration.ofMinutes(5));
        assertThat(providerSettings.getRefreshTokenTimeToLive()).isEqualTo(Duration.ofMinutes(60));
    }

    @Test
    public void buildWhenSettingsProvidedThenSet() {
        String authorizationEndpoint = "/oauth2/v1/authorize";
        String tokenEndpoint = "/oauth2/v1/token";
        String jwkSetEndpoint = "/oauth2/v1/jwks";
        String issuer = "https://example.com:9000";
        boolean isReuseRefreshTokens = true;
        Duration accessTokenTimeToLive = Duration.ofMinutes(30);
        Duration refreshTokenTimeToLive = Duration.ofMinutes(120);

        ProviderSettings providerSettings = ProviderSettings.builder()
            .issuer(issuer)
            .authorizationEndpoint(authorizationEndpoint)
            .tokenEndpoint(tokenEndpoint)
            .jwkSetEndpoint(jwkSetEndpoint)
            .reuseRefreshTokens(isReuseRefreshTokens)
            .accessTokenTimeToLive(accessTokenTimeToLive)
            .refreshTokenTimeToLive(refreshTokenTimeToLive)
            .build();

        assertThat(providerSettings.getIssuer()).isEqualTo(issuer);
        assertThat(providerSettings.getAuthorizationEndpoint()).isEqualTo(authorizationEndpoint);
        assertThat(providerSettings.getTokenEndpoint()).isEqualTo(tokenEndpoint);
        assertThat(providerSettings.getJwkSetEndpoint()).isEqualTo(jwkSetEndpoint);
        assertThat(providerSettings.isReuseRefreshTokens()).isEqualTo(isReuseRefreshTokens);
        assertThat(providerSettings.getAccessTokenTimeToLive()).isEqualTo(accessTokenTimeToLive);
        assertThat(providerSettings.getRefreshTokenTimeToLive()).isEqualTo(refreshTokenTimeToLive);
    }

    @Test
    public void settingWhenCustomThenSet() {
        ProviderSettings providerSettings = ProviderSettings.builder()
            .setting("name1", "value1")
            .settings(settings -> settings.put("name2", "value2"))
            .build();

        assertThat(providerSettings.getSettings()).hasSize(8);
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

    @Test
    public void refreshTokenTimeToLiveWhenNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().refreshTokenTimeToLive(null))
            .withMessage("refreshTokenTimeToLive cannot be null");
    }

    @Test
    public void refreshTokenTimeToLiveWhenLessThanZeroThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().refreshTokenTimeToLive(Duration.ZERO))
            .withMessage("refreshTokenTimeToLive must be greater than Duration.ZERO");
    }

    @Test
    public void accessTokenTimeToLiveToLiveWhenNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().accessTokenTimeToLive(null))
            .withMessage("accessTokenTimeToLive cannot be null");
    }

    @Test
    public void accessTokenTimeToLiveToLiveWhenLessThanZeroThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ProviderSettings.builder().accessTokenTimeToLive(Duration.ZERO))
            .withMessage("accessTokenTimeToLive must be greater than Duration.ZERO");
    }
}
