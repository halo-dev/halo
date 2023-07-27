package run.halo.app.security.authentication.login;

import org.pf4j.ExtensionPoint;
import org.springframework.security.authentication.ReactiveAuthenticationManager;

/**
 * An extension point for username password authentication.
 * Any non-authentication exception occurs, the default authentication will be used.
 * If you want to skip authentication, please return Mono.empty() directly, the default
 * authentication will be used.
 *
 * @author johnniang
 * @since 2.8
 */
public interface UsernamePasswordAuthenticationManager
    extends ReactiveAuthenticationManager, ExtensionPoint {

}
