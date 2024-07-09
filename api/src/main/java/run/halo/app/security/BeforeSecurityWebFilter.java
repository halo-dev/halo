package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.web.server.WebFilter;

/**
 * Security web filter for before security.
 *
 * @author johnniang
 * @since 2.18
 */
public interface BeforeSecurityWebFilter extends WebFilter, ExtensionPoint {

}
