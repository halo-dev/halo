package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for OAuth2 authorization code.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface OAuth2AuthorizationCodeSecurityWebFilter extends WebFilter, ExtensionPoint {

}
