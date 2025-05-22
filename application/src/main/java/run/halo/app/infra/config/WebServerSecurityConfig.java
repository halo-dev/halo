package run.halo.app.infra.config;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.session.MapSession;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.security.DefaultUserDetailService;
import run.halo.app.security.HaloServerRequestCache;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.impl.RsaKeyService;
import run.halo.app.security.authorization.AuthorityUtils;
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
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class WebServerSecurityConfig {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http,
        ObjectProvider<SecurityConfigurer> securityConfigurers,
        ServerSecurityContextRepository securityContextRepository,
        HaloProperties haloProperties,
        ServerRequestCache serverRequestCache) {

        var pathMatcher = pathMatchers("/**");
        var staticResourcesMatcher = pathMatchers(HttpMethod.GET,
            "/console/assets/**",
            "/uc/assets/**",
            "/themes/{themeName}/assets/{*resourcePaths}",
            "/plugins/{pluginName}/assets/**",
            "/webjars/**",
            "/js/**",
            "/styles/**",
            "/halo-tracker.js",
            "/images/**"
        );

        var securityMatcher = new AndServerWebExchangeMatcher(pathMatcher,
            new NegatedServerWebExchangeMatcher(staticResourcesMatcher));

        http.securityMatcher(securityMatcher)
            .anonymous(spec -> {
                spec.authorities(AuthorityUtils.ROLE_PREFIX + AnonymousUserConst.Role);
                spec.principal(AnonymousUserConst.PRINCIPAL);
            })
            .securityContextRepository(securityContextRepository)
            .httpBasic(basic -> {
                if (haloProperties.getSecurity().getBasicAuth().isDisabled()) {
                    basic.disable();
                }
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
            )
            .requestCache(spec -> spec.requestCache(serverRequestCache));

        // Integrate with other configurers separately
        securityConfigurers.orderedStream()
            .forEach(securityConfigurer -> securityConfigurer.configure(http));
        return http.build();
    }

    @Bean
    ServerRequestCache serverRequestCache() {
        return new HaloServerRequestCache();
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
        RoleService roleService,
        HaloProperties haloProperties) {
        var userDetailService = new DefaultUserDetailService(userService, roleService);
        var twoFactorAuthDisabled = haloProperties.getSecurity().getTwoFactorAuth().isDisabled();
        userDetailService.setTwoFactorAuthDisabled(twoFactorAuthDisabled);
        return userDetailService;
    }

    @Bean
    @SuppressWarnings("deprecation")
    PasswordEncoder passwordEncoder() {
        // For removing the length limit of password, we have to create an argon2 password encoder
        // as default encoder.
        // When https://github.com/spring-projects/spring-security/issues/16879 resolved,
        // we can remove this code.
        var encodingId = "argon2@SpringSecurity_v5_8";
        var encoders = new HashMap<String, PasswordEncoder>();
        encoders.put(encodingId, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder(encodingId, encoders);
    }

    @Bean
    CryptoService cryptoService(HaloProperties haloProperties) {
        return new RsaKeyService(haloProperties.getWorkDir().resolve("keys"));
    }


}
