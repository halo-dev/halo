package run.halo.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import run.halo.app.identity.authentication.InMemoryOAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2TokenType;

/**
 * An {@link OAuth2AuthorizationService} that stores {@link OAuth2Authorization}'s in-memory.<p>
 * <b>NOTE:</b> This implementation should ONLY be used during development/testing.
 *
 * @author guqing
 * @see OAuth2AuthorizationService
 * @since 2.0.0
 */
public class InMemoryOAuth2AuthorizationServiceTest {
    private static final String ID = "id";
    private static final String PRINCIPAL_NAME = "principal";
    private static final AuthorizationGrantType AUTHORIZATION_GRANT_TYPE =
        AuthorizationGrantType.PASSWORD;
    private static final OAuth2AccessToken TOKEN =
        new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access_token",
            Instant.now(), Instant.now().plusSeconds(256));
    private InMemoryOAuth2AuthorizationService authorizationService;

    @BeforeEach
    public void setup() {
        this.authorizationService = new InMemoryOAuth2AuthorizationService();
    }

    @Test
    public void constructorVarargsWhenAuthorizationNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> new InMemoryOAuth2AuthorizationService((OAuth2Authorization) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("authorization cannot be null");
    }

    @Test
    public void constructorListWhenAuthorizationsNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(
            () -> new InMemoryOAuth2AuthorizationService((List<OAuth2Authorization>) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("authorizations cannot be null");
    }

    @Test
    public void constructorWhenDuplicateAuthorizationsThenThrowIllegalArgumentException() {
        OAuth2Authorization authorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .token(TOKEN)
            .build();

        assertThatThrownBy(
            () -> new InMemoryOAuth2AuthorizationService(authorization, authorization))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The authorization must be unique. Found duplicate identifier: id");
    }

    @Test
    public void saveWhenAuthorizationNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> this.authorizationService.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("authorization cannot be null");
    }

    @Test
    public void saveWhenAuthorizationNewThenSaved() {
        OAuth2Authorization expectedAuthorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .token(TOKEN)
            .build();
        this.authorizationService.save(expectedAuthorization);

        OAuth2Authorization authorization = this.authorizationService.findByToken(
            TOKEN.getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);
        assertThat(authorization).isEqualTo(expectedAuthorization);
    }

    @Test
    public void saveWhenAuthorizationExistsThenUpdated() {
        OAuth2Authorization originalAuthorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .token(TOKEN)
            .build();
        this.authorizationService.save(originalAuthorization);

        OAuth2Authorization authorization = this.authorizationService.findById(
            originalAuthorization.getId());
        assertThat(authorization).isEqualTo(originalAuthorization);

        OAuth2Authorization updatedAuthorization = OAuth2Authorization.from(authorization)
            .attribute("custom-name-1", "custom-value-1")
            .build();
        this.authorizationService.save(updatedAuthorization);

        authorization = this.authorizationService.findById(
            updatedAuthorization.getId());
        assertThat(authorization).isEqualTo(updatedAuthorization);
        assertThat(authorization).isNotEqualTo(originalAuthorization);
    }

    @Test
    public void saveWhenInitializedAuthorizationsReachMaxThenOldestRemoved() {
        int maxInitializedAuthorizations = 5;
        InMemoryOAuth2AuthorizationService authorizationService =
            new InMemoryOAuth2AuthorizationService(maxInitializedAuthorizations);

        OAuth2Authorization initialAuthorization = new OAuth2Authorization.Builder()
            .id(ID + "-initial")
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .attribute(OAuth2ParameterNames.STATE, "state-initial")
            .build();
        authorizationService.save(initialAuthorization);

        OAuth2Authorization authorization =
            authorizationService.findById(initialAuthorization.getId());
        assertThat(authorization).isEqualTo(initialAuthorization);
    }

    @Test
    public void removeWhenAuthorizationNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> this.authorizationService.remove(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("authorization cannot be null");
    }

    @Test
    public void removeWhenAuthorizationProvidedThenRemoved() {
        OAuth2Authorization expectedAuthorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .token(TOKEN)
            .build();

        this.authorizationService.save(expectedAuthorization);
        OAuth2Authorization authorization = this.authorizationService.findByToken(
            TOKEN.getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);
        assertThat(authorization).isEqualTo(expectedAuthorization);

        this.authorizationService.remove(expectedAuthorization);
        authorization = this.authorizationService.findByToken(
            TOKEN.getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);
        assertThat(authorization).isNull();
    }

    @Test
    public void findByIdWhenIdNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> this.authorizationService.findById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("id cannot be empty");
    }

    @Test
    public void findByTokenWhenTokenNullThenThrowIllegalArgumentException() {
        assertThatThrownBy(
            () -> this.authorizationService.findByToken(null, OAuth2TokenType.ACCESS_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("token cannot be empty");
    }

    @Test
    public void findByTokenWhenAccessTokenExistsThenFound() {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
            "access-token", Instant.now().minusSeconds(60), Instant.now());
        OAuth2Authorization authorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .token(TOKEN)
            .accessToken(accessToken)
            .build();
        this.authorizationService.save(authorization);

        OAuth2Authorization result = this.authorizationService.findByToken(
            accessToken.getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);
        assertThat(authorization).isEqualTo(result);
        result = this.authorizationService.findByToken(accessToken.getTokenValue(), null);
        assertThat(authorization).isEqualTo(result);
    }

    @Test
    public void findByTokenWhenRefreshTokenExistsThenFound() {
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken("refresh-token", Instant.now());
        OAuth2Authorization authorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .refreshToken(refreshToken)
            .build();
        this.authorizationService.save(authorization);

        OAuth2Authorization result = this.authorizationService.findByToken(
            refreshToken.getTokenValue(), OAuth2TokenType.REFRESH_TOKEN);
        assertThat(authorization).isEqualTo(result);
        result = this.authorizationService.findByToken(refreshToken.getTokenValue(), null);
        assertThat(authorization).isEqualTo(result);
    }

    @Test
    public void findByTokenWhenWrongTokenTypeThenNotFound() {
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken("refresh-token", Instant.now());
        OAuth2Authorization authorization = new OAuth2Authorization.Builder()
            .id(ID)
            .principalName(PRINCIPAL_NAME)
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE)
            .refreshToken(refreshToken)
            .build();
        this.authorizationService.save(authorization);

        OAuth2Authorization result = this.authorizationService.findByToken(
            refreshToken.getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);
        assertThat(result).isNull();
    }

    @Test
    public void findByTokenWhenTokenDoesNotExistThenNull() {
        OAuth2Authorization result = this.authorizationService.findByToken(
            "access-token", OAuth2TokenType.ACCESS_TOKEN);
        assertThat(result).isNull();
    }
}
