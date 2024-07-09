package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for after security.
 *
 * @author johnniang
 * @since 2.18
 */
public interface AfterSecurityWebFilter extends WebFilter, ExtensionPoint {

}
