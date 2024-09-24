package run.halo.app.infra;

import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * Secure server web exchange without application context available.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class SecureServerWebExchange extends ServerWebExchangeDecorator {

    public SecureServerWebExchange(ServerWebExchange delegate) {
        super(delegate);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        // Always return null to prevent access to application context
        return null;
    }

}
