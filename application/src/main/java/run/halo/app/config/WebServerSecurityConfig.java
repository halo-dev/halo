package run.halo.app.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.DefaultUserDetailService;
import run.halo.app.security.DynamicMatcherSecurityWebFilterChain;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.login.CryptoService;
import run.halo.app.security.authentication.login.PublicKeyRouteBuilder;
import run.halo.app.security.authentication.login.RsaKeyScheduledGenerator;
import run.halo.app.security.authentication.login.impl.RsaKeyService;
import run.halo.app.security.authorization.RequestInfoAuthorizationManager;

/**
 * Security configuration for WebFlux.
 *
 * @author johnniang
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebServerSecurityConfig {

    @Bean(name = "apiSecurityFilterChain")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityWebFilterChain apiFilterChain(ServerHttpSecurity http,
        RoleService roleService,
        ObjectProvider<SecurityConfigurer> securityConfigurers,
        ServerSecurityContextRepository securityContextRepository,
        ExtensionGetter extensionGetter) {

        http.securityMatcher(pathMatchers("/api/**", "/apis/**", "/oauth2/**",
                "/login/**", "/logout", "/actuator/**"))
            .authorizeExchange(spec -> {
                spec.anyExchange().access(new RequestInfoAuthorizationManager(roleService));
            })
            .anonymous(spec -> {
                spec.authorities(AnonymousUserConst.Role);
                spec.principal(AnonymousUserConst.PRINCIPAL);
            })
            .securityContextRepository(securityContextRepository)
            .httpBasic(withDefaults());

        // Integrate with other configurers separately
        securityConfigurers.orderedStream()
            .forEach(securityConfigurer -> securityConfigurer.configure(http));
        return new DynamicMatcherSecurityWebFilterChain(extensionGetter, http.build());
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    SecurityWebFilterChain portalFilterChain(ServerHttpSecurity http,
        ServerSecurityContextRepository securityContextRepository,
        HaloProperties haloProperties) {
        var pathMatcher = pathMatchers(HttpMethod.GET, "/**");
        var mediaTypeMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        mediaTypeMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        http.securityMatcher(new AndServerWebExchangeMatcher(pathMatcher, mediaTypeMatcher))
            .securityContextRepository(securityContextRepository)
            .authorizeExchange(spec -> {
                spec.anyExchange().permitAll();
            })
            .headers(headerSpec -> headerSpec
                .frameOptions(frameSpec -> {
                    var frameOptions = haloProperties.getSecurity().getFrameOptions();
                    frameSpec.mode(frameOptions.getMode());
                    if (frameOptions.isDisabled()) {
                        frameSpec.disable();
                    }
                })
                .referrerPolicy(referrerPolicySpec -> {
                    referrerPolicySpec.policy(
                        haloProperties.getSecurity().getReferrerOptions().getPolicy());
                })
                .cache(ServerHttpSecurity.HeaderSpec.CacheSpec::disable)
            )
            .anonymous(spec -> spec.authenticationFilter(
                new HaloAnonymousAuthenticationWebFilter("portal", AnonymousUserConst.PRINCIPAL,
                    AuthorityUtils.createAuthorityList(AnonymousUserConst.Role),
                    securityContextRepository)));
        return http.build();
    }

    @Bean
    ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
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

    @Bean
    RsaKeyScheduledGenerator rsaKeyScheduledGenerator(CryptoService cryptoService) {
        return new RsaKeyScheduledGenerator(cryptoService);
    }
}
