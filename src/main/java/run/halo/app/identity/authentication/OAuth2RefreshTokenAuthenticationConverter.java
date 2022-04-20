package run.halo.app.identity.authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Attempts to extract an Access Token Request from {@link HttpServletRequest} for the OAuth 2.0
 * Refresh Token Grant
 * and then converts it to an {@link OAuth2RefreshTokenAuthenticationToken} used for
 * authenticating the authorization grant.
 *
 * @author guqing
 * @see AuthenticationConverter
 * @see OAuth2RefreshTokenAuthenticationToken
 * @see OAuth2TokenEndpointFilter
 * @since 2.0.0
 */
public class OAuth2RefreshTokenAuthenticationConverter implements AuthenticationConverter {

    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {
        // grant_type (REQUIRED)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType)) {
            return null;
        }

        MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);

        // refresh_token (REQUIRED)
        String refreshToken = parameters.getFirst(OAuth2ParameterNames.REFRESH_TOKEN);
        if (!StringUtils.hasText(refreshToken)
            || parameters.get(OAuth2ParameterNames.REFRESH_TOKEN).size() != 1) {
            OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST,
                OAuth2ParameterNames.REFRESH_TOKEN,
                OAuth2EndpointUtils.ERROR_URI);
        }

        // scope (OPTIONAL)
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope)
            && parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
            OAuth2EndpointUtils.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                OAuth2ParameterNames.SCOPE,
                OAuth2EndpointUtils.ERROR_URI);
        }
        Set<String> requestedScopes = null;
        if (StringUtils.hasText(scope)) {
            requestedScopes = new HashSet<>(
                Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE)
                && !key.equals(OAuth2ParameterNames.REFRESH_TOKEN)
                && !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, value.get(0));
            }
        });

        return new OAuth2RefreshTokenAuthenticationToken(refreshToken, requestedScopes,
            additionalParameters);
    }
}
