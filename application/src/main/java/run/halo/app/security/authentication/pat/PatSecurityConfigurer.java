package run.halo.app.security.authentication.pat;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * PAT security configurer.
 *
 * @author johnniang
 * @since 2.20.4
 */
@Component
// Specific an order here to control the order or security configurer initialization
@Order(0)
class PatSecurityConfigurer implements SecurityConfigurer {

    private final ReactiveExtensionClient client;

    private final CryptoService cryptoService;

    public PatSecurityConfigurer(ReactiveExtensionClient client, CryptoService cryptoService) {
        this.client = client;
        this.cryptoService = cryptoService;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter =
            new AuthenticationWebFilter(new PatAuthenticationManager(client, cryptoService));
        filter.setServerAuthenticationConverter(new PatAuthenticationConverter());
        var entryPoint = new BearerTokenServerAuthenticationEntryPoint();
        var failureHandler = new ServerAuthenticationEntryPointFailureHandler(entryPoint);
        filter.setAuthenticationFailureHandler(failureHandler);
        http.addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }

}
