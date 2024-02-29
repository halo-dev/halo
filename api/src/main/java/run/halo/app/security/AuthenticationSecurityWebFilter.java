package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for normal authentication.
 *
 * @author johnniang
 */
public interface AuthenticationSecurityWebFilter extends WebFilter, ExtensionPoint {

}
