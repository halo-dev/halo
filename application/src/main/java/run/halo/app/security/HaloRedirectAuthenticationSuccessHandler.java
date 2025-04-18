package run.halo.app.security;

import static run.halo.app.security.HaloServerRequestCache.uriInApplication;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

/**
 * This class is responsible for handling the redirection after a successful authentication.
 * It checks if a valid 'redirect_uri' query parameter is present in the request. If it is,
 * the user is redirected to the specified URI. Otherwise, the user is redirected to a default
 * location.
 *
 * @author johnniang
 */
@Slf4j
public class HaloRedirectAuthenticationSuccessHandler
    implements ServerAuthenticationSuccessHandler {

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private final URI location;

    public HaloRedirectAuthenticationSuccessHandler(String location) {
        this.location = URI.create(location);
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
        Authentication authentication) {
        var request = webFilterExchange.getExchange().getRequest();
        var redirectUriQuery = request.getQueryParams()
            .getFirst("redirect_uri");
        if (redirectUriQuery == null || redirectUriQuery.isBlank()) {
            return redirectStrategy.sendRedirect(webFilterExchange.getExchange(), location);
        }
        var redirectUri = uriInApplication(request, URI.create(redirectUriQuery));
        if (log.isDebugEnabled()) {
            log.debug(
                "Redirecting to: {} after switching to {}",
                redirectUri, authentication.getName()
            );
        }
        return redirectStrategy.sendRedirect(
            webFilterExchange.getExchange(), URI.create(redirectUri)
        );
    }
}
