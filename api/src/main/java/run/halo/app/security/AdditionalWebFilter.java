package run.halo.app.security;

import org.pf4j.ExtensionPoint;
import org.springframework.core.Ordered;
import org.springframework.web.server.WebFilter;

/**
 * Contract for interception-style, chained processing of Web requests that may be used to
 * implement cross-cutting, application-agnostic requirements such as security, timeouts, and
 * others.
 *
 * @author guqing
 * @since 2.4.0
 */
public interface AdditionalWebFilter extends WebFilter, ExtensionPoint, Ordered {

    /**
     * Gets the order value of the object.
     *
     * @return the order value
     */
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
