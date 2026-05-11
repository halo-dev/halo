package run.halo.app.security.authentication.token;

import static org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder.withJwkSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.SecurityConfigurer;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
class TokenSecurityConfigurer implements SecurityConfigurer {

    private final CryptoService cryptoService;

    private final ReactiveExtensionClient client;

    @Override
    public void configure(ServerHttpSecurity http) {
        var jwtDecoder =
                withJwkSource(signedJWT -> Flux.just(cryptoService.getJwk())).build();
        var tokenAuthenticationManager = new TokenAuthenticationManager(client, jwtDecoder);

        var entryPoint = new BearerTokenServerAuthenticationEntryPoint();
        var failureHandler = new ServerAuthenticationEntryPointFailureHandler(entryPoint);

        var filter = new AuthenticationWebFilter(tokenAuthenticationManager);
        filter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());
        filter.setAuthenticationFailureHandler(failureHandler);
        http.addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }
}
