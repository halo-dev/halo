package run.halo.app.identity.authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Utility methods for the OAuth 2.0 Protocol Endpoints.
 *
 * @author guqing
 * @since 2.0.0
 */
public class OAuth2EndpointUtils {
    static final String ACCESS_TOKEN_REQUEST_ERROR_URI =
        "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private OAuth2EndpointUtils() {
    }

    static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            if (values.length > 0) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }

    static void throwError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error
            error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        throw new OAuth2AuthenticationException(error);
    }
}
