package run.halo.app.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.security.DefaultUserDetailService;
import run.halo.app.security.SuperAdminInitializer;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authorization.RequestInfoAuthorizationManager;

/**
 * Security configuration for WebFlux.
 *
 * @author johnniang
 */
@Configuration
@EnableWebFluxSecurity
public class WebServerSecurityConfig {

    private final JwtProperties jwtProp;

    public WebServerSecurityConfig(JwtProperties jwtProp) {
        this.jwtProp = jwtProp;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityWebFilterChain apiFilterChain(ServerHttpSecurity http,
        RoleService roleService,
        ObjectProvider<SecurityConfigurer> securityConfigurers) {

        http.csrf().csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .cors(corsSpec -> corsSpec.configurationSource(apiCorsConfigurationSource()))
            .securityMatcher(pathMatchers("/api/**", "/apis/**", "/login", "/logout"))
            .authorizeExchange(exchanges ->
                exchanges.anyExchange().access(new RequestInfoAuthorizationManager(roleService)))
            .anonymous(anonymousSpec -> {
                anonymousSpec.authorities(AnonymousUserConst.Role);
                anonymousSpec.principal(AnonymousUserConst.PRINCIPAL);
            })
            .httpBasic(withDefaults())
            // for reuse the JWT authentication
            .oauth2ResourceServer().jwt();

        // Integrate with other configurers separately
        securityConfigurers.orderedStream()
            .forEach(securityConfigurer -> securityConfigurer.configure(http));

        return http.build();
    }

    CorsConfigurationSource apiCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(
            List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                "X-XSRF-TOKEN", HttpHeaders.COOKIE));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/apis/**", configuration);
        // TODO Remove both login and logout path until we provide the console proxy.
        source.registerCorsConfiguration("/login", configuration);
        source.registerCorsConfiguration("/logout", configuration);
        return source;
    }

    @Bean
    ReactiveUserDetailsService userDetailsService(UserService userService,
        RoleService roleService) {
        return new DefaultUserDetailService(userService, roleService);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        return new SupplierReactiveJwtDecoder(
            () -> NimbusReactiveJwtDecoder.withPublicKey(jwtProp.getPublicKey())
                .signatureAlgorithm(jwtProp.getJwsAlgorithm())
                .build());
    }

    @Bean
    JwtEncoder jwtEncoder() {
        var rsaKey = new RSAKey.Builder(jwtProp.getPublicKey())
            .privateKey(jwtProp.getPrivateKey())
            .algorithm(JWSAlgorithm.parse(jwtProp.getJwsAlgorithm().getName()))
            .build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    @ConditionalOnProperty(name = "halo.security.initializer.disabled",
        havingValue = "false",
        matchIfMissing = true)
    SuperAdminInitializer superAdminInitializer(ReactiveExtensionClient client,
        HaloProperties halo) {
        return new SuperAdminInitializer(client, passwordEncoder(),
            halo.getSecurity().getInitializer());
    }
}
