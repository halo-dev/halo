package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for anonymous authentication.
 *
 * @author johnniang
 */
public interface AnonymousAuthenticationSecurityWebFilter extends WebFilter, ExtensionPoint {

}
