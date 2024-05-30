package run.halo.app.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.authentication.ServerWebExchangeDelegatingReactiveAuthenticationManagerResolver.builder;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.session.MapSession;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.security.DefaultUserDetailService;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.impl.RsaKeyService;
import run.halo.app.security.authentication.login.PublicKeyRouteBuilder;
import run.halo.app.security.authentication.pat.PatAuthenticationManager;
import run.halo.app.security.authentication.pat.PatServerWebExchangeMatcher;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthorizationManager;
import run.halo.app.security.authorization.RequestInfoAuthorizationManager;
import run.halo.app.security.session.InMemoryReactiveIndexedSessionRepository;
import run.halo.app.security.session.ReactiveIndexedSessionRepository;

/**
 * Security configuration for WebFlux.
 *
 * @author johnniang
 */
@Configuration
@EnableSpringWebSession
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebServerSecurityConfig {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http,
        RoleService roleService,
        ObjectProvider<SecurityConfigurer> securityConfigurers,
        ServerSecurityContextRepository securityContextRepository,
        ReactiveExtensionClient client,
        CryptoService cryptoService,
        HaloProperties haloProperties) {

        http.securityMatcher(pathMatchers("/**"))
            .authorizeExchange(spec -> spec.pathMatchers(
                    "/api/**",
                    "/apis/**",
                    "/oauth2/**",
                    "/login/**",
                    "/logout",
                    "/actuator/**"
                )
                .access(
                    new TwoFactorAuthorizationManager(
                        new RequestInfoAuthorizationManager(roleService)
                    )
                )
                .anyExchange().permitAll())
            .anonymous(spec -> {
                spec.authorities(AnonymousUserConst.Role);
                spec.principal(AnonymousUserConst.PRINCIPAL);
            })
            .securityContextRepository(securityContextRepository)
            .httpBasic(withDefaults())
            .oauth2ResourceServer(oauth2 -> {
                var authManagerResolver = builder().add(
                        new PatServerWebExchangeMatcher(),
                        new PatAuthenticationManager(client, cryptoService)
                    )
                    // TODO Add other authentication mangers here. e.g.: JwtAuthenticationManager.
                    .build();
                oauth2.authenticationManagerResolver(authManagerResolver);
            })
            .headers(headerSpec -> headerSpec
                .frameOptions(frameSpec -> {
                    var frameOptions = haloProperties.getSecurity().getFrameOptions();
                    frameSpec.mode(frameOptions.getMode());
                    if (frameOptions.isDisabled()) {
                        frameSpec.disable();
                    }
                })
                .referrerPolicy(referrerPolicySpec -> referrerPolicySpec.policy(
                    haloProperties.getSecurity().getReferrerOptions().getPolicy())
                )
                .hsts(hstsSpec -> hstsSpec.includeSubdomains(false))
            );

        // Integrate with other configurers separately
        securityConfigurers.orderedStream()
            .forEach(securityConfigurer -> securityConfigurer.configure(http));
        return http.build();
    }

    @Bean
    ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public ReactiveIndexedSessionRepository<MapSession> reactiveSessionRepository(
        SessionProperties sessionProperties,
        ServerProperties serverProperties) {
        var repository = new InMemoryReactiveIndexedSessionRepository(new ConcurrentHashMap<>());
        var timeout = sessionProperties.determineTimeout(
            () -> serverProperties.getReactive().getSession().getTimeout());
        repository.setDefaultMaxInactiveInterval(timeout);
        return repository;
    }

    @Bean
    DefaultUserDetailService userDetailsService(UserService userService,
        RoleService roleService) {
        return new DefaultUserDetailService(userService, roleService);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    RouterFunction<ServerResponse> publicKeyRoute(CryptoService cryptoService) {
        return new PublicKeyRouteBuilder(cryptoService).build();
    }

    @Bean
    CryptoService cryptoService(HaloProperties haloProperties) {
        return new RsaKeyService(haloProperties.getWorkDir().resolve("keys"));
    }

}
