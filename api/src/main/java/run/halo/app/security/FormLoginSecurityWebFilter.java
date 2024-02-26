package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for form login.
 *
 * @author johnniang
 */
public interface FormLoginSecurityWebFilter extends WebFilter, ExtensionPoint {

}
