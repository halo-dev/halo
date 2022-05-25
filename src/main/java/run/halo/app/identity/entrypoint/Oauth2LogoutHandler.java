package run.halo.app.identity.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2TokenType;
import run.halo.app.identity.authentication.verifier.JwtAuthenticationToken;

/**
 * <p>Performs a logout by {@link OAuth2AuthorizationService}.</p>
 * <p>Will remove the {@link Authentication} from the {@link OAuth2AuthorizationService} if the
 * specific instance of {@link Authentication} is {@link JwtAuthenticationToken}.</p>
 *
 * @author guqing
 * @see OAuth2AuthorizationService
 * @see Authentication
 * @see JwtAuthenticationToken
 * @since 2.0.0
 */
@Slf4j
public class Oauth2LogoutHandler implements LogoutHandler {

    private final OAuth2AuthorizationService oauth2AuthorizationService;

    public Oauth2LogoutHandler(OAuth2AuthorizationService oauth2AuthorizationService) {
        this.oauth2AuthorizationService = oauth2AuthorizationService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
        log.debug("Logging out [{}]", authentication);
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tokenValue = jwtAuthenticationToken.getToken().getTokenValue();
            var oauth2Authorization = oauth2AuthorizationService.findByToken(
                tokenValue, OAuth2TokenType.ACCESS_TOKEN);
            oauth2AuthorizationService.remove(oauth2Authorization);
            log.debug("Removed oauth2Authorization [{}]", oauth2Authorization);
        }
    }
}
