package run.halo.app.identity.apitoken;

import java.util.Collection;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import run.halo.app.identity.authentication.OAuth2Authorization;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2TokenType;

/**
 * A default implementation of personal access token authentication.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultPersonalAccessTokenDecoder implements PersonalAccessTokenDecoder {

    private static final String DECODING_ERROR_MESSAGE_TEMPLATE =
        "An error occurred while attempting to decode the personal access token: %s";

    private OAuth2TokenValidator<PersonalAccessToken> personalAccessTokenValidator =
        createDefault();

    private final OAuth2AuthorizationService oauth2AuthorizationService;

    private final SecretKey secretKey;

    public DefaultPersonalAccessTokenDecoder(
        OAuth2AuthorizationService oauth2AuthorizationService, SecretKey secretKey) {
        this.oauth2AuthorizationService = oauth2AuthorizationService;
        this.secretKey = secretKey;
    }

    /**
     * Use this {@link PersonalAccessToken} Validator.
     *
     * @param personalAccessTokenValidator - the PersonalAccessToken Validator to use
     */
    public void setTokenValidator(
        OAuth2TokenValidator<PersonalAccessToken> personalAccessTokenValidator) {
        Assert.notNull(personalAccessTokenValidator, "personalAccessTokenValidator cannot be null");
        this.personalAccessTokenValidator = personalAccessTokenValidator;
    }

    @Override
    public PersonalAccessToken decode(String token) throws PersonalAccessTokenException {
        preValidate(token);
        PersonalAccessToken personalAccessToken = createPersonalAccessToken(token);
        return validate(personalAccessToken);
    }

    private void preValidate(String token) {
        if (secretKey == null) {
            return;
        }
        boolean matches = PersonalAccessTokenUtils.verifyChecksum(token, secretKey);
        if (matches) {
            return;
        }
        throw new PersonalAccessTokenException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE,
            "Failed to verify the personal access token"));
    }

    private PersonalAccessToken createPersonalAccessToken(String token) {
        OAuth2Authorization oauth2Authorization = oauth2AuthorizationService.findByToken(token,
            OAuth2TokenType.ACCESS_TOKEN);
        if (oauth2Authorization == null) {
            throw new PersonalAccessTokenException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE,
                "Failed to retrieve personal access token"));
        }
        return createPersonalAccessToken(oauth2Authorization);
    }

    private PersonalAccessToken createPersonalAccessToken(OAuth2Authorization oauth2Authorization) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessTokenToken =
            oauth2Authorization.getToken(OAuth2AccessToken.class);
        if (accessTokenToken == null) {
            throw new PersonalAccessTokenException(String.format(DECODING_ERROR_MESSAGE_TEMPLATE,
                "Failed to retrieve personal access token"));
        }
        return PersonalAccessToken.builder()
            .principalName(oauth2Authorization.getPrincipalName())
            .token(accessTokenToken.getToken())
            .attributes(accessTokenToken.getClaims())
            .build();
    }

    private PersonalAccessToken validate(PersonalAccessToken token) {
        OAuth2TokenValidatorResult result = this.personalAccessTokenValidator.validate(token);
        if (result.hasErrors()) {
            Collection<OAuth2Error> errors = result.getErrors();
            String validationErrorString = getValidationExceptionMessage(errors);
            throw new JwtValidationException(validationErrorString, errors);
        }
        return token;
    }

    private String getValidationExceptionMessage(Collection<OAuth2Error> errors) {
        for (OAuth2Error oauth2Error : errors) {
            if (!StringUtils.hasText(oauth2Error.getDescription())) {
                return String.format(DECODING_ERROR_MESSAGE_TEMPLATE, oauth2Error.getDescription());
            }
        }
        return "Unable to validate personal access token";
    }

    public static OAuth2TokenValidator<PersonalAccessToken> createDefault() {
        return new DelegatingOAuth2TokenValidator<>(new PersonalAccessTokenTimestampValidator());
    }
}
