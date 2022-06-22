package run.halo.app.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
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
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.security.DefaultUserDetailService;
import run.halo.app.security.authentication.jwt.LoginAuthenticationFilter;
import run.halo.app.security.authentication.jwt.LoginAuthenticationManager;
import run.halo.app.security.authorization.RequestInfoAuthorizationManager;

/**
 * Security configuration for WebFlux.
 *
 * @author johnniang
 */
@EnableWebFluxSecurity
public class WebServerSecurityConfig {

    private final JwtProperties jwtProp;

    public WebServerSecurityConfig(JwtProperties jwtProp) {
        this.jwtProp = jwtProp;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityWebFilterChain apiFilterChain(ServerHttpSecurity http,
        ServerCodecConfigurer codec,
        ServerResponse.Context context,
        UserService userService,
        RoleService roleService) {
        http.csrf().disable()
            .cors(corsSpec -> corsSpec.configurationSource(apiCorsConfigurationSource()))
            .securityMatcher(pathMatchers("/api/**", "/apis/**"))
            .authorizeExchange(exchanges ->
                exchanges.anyExchange().access(new RequestInfoAuthorizationManager(roleService)))
            // for reuse the JWT authentication
            .oauth2ResourceServer().jwt();

        var loginManager = new LoginAuthenticationManager(
            userDetailsService(userService, roleService),
            passwordEncoder());
        var loginFilter = new LoginAuthenticationFilter(loginManager,
            codec,
            jwtEncoder(),
            jwtProp,
            context);

        http.addFilterAt(loginFilter, SecurityWebFiltersOrder.FORM_LOGIN);

        return http.build();
    }

    @Bean
    @Order(0)
    SecurityWebFilterChain webFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges.pathMatchers(
                "/actuator/**"
            ).permitAll())
            .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
            .cors(withDefaults())
            .httpBasic(withDefaults())
            .formLogin(withDefaults())
            .csrf().csrfTokenRepository(new CookieServerCsrfTokenRepository()).and()
            .logout(withDefaults());

        return http.build();
    }

    CorsConfigurationSource apiCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(
            List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/apis/**", configuration);
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

}
