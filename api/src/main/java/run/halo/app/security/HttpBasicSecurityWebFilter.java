package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for HTTP basic.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface HttpBasicSecurityWebFilter extends WebFilter, ExtensionPoint {

}
