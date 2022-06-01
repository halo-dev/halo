package run.halo.app.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.security.jwt.TokenAuthenticationConverter;
import run.halo.app.security.jwt.TokenAuthenticationFailureHandler;
import run.halo.app.security.jwt.TokenAuthenticationSuccessHandler;

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
        ServerResponse.Context context) {
        http.csrf().disable()
            .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**"))
            .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
            .oauth2ResourceServer().jwt();

        http.addFilterAt(tokenFilter(codec, context), SecurityWebFiltersOrder.FORM_LOGIN);

        return http.build();
    }

    @Bean
    SecurityWebFilterChain webFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(
                exchanges -> exchanges.pathMatchers("/v3/api-docs/**", "/v3/api-docs.yaml",
                    "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll())
            .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
            .cors(withDefaults()).httpBasic(withDefaults()).formLogin(withDefaults())
            .logout(withDefaults());

        return http.build();
    }

    // @Bean
    AuthenticationWebFilter tokenFilter(ServerCodecConfigurer codec,
        ServerResponse.Context context) {
        var filter = new AuthenticationWebFilter(
            authenticationManager(userDetailsService(), passwordEncoder()));

        var requiresMatcher = new AndServerWebExchangeMatcher(
            pathMatchers(HttpMethod.POST, "/api/auth/token"),
            new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON));
        filter.setRequiresAuthenticationMatcher(requiresMatcher);
        filter.setServerAuthenticationConverter(
            new TokenAuthenticationConverter(codec.getReaders()));
        filter.setAuthenticationSuccessHandler(
            new TokenAuthenticationSuccessHandler(jwtEncoder(), jwtProp, context));
        filter.setAuthenticationFailureHandler(new TokenAuthenticationFailureHandler(context));
        return filter;
    }

    ReactiveAuthenticationManager authenticationManager(
        ReactiveUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        var authenticationManager =
            new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        if (userDetailsService instanceof ReactiveUserDetailsPasswordService passwordService) {
            authenticationManager.setUserDetailsPasswordService(passwordService);
        }
        return authenticationManager;
    }

    @Bean
    ReactiveUserDetailsService userDetailsService() {
        //TODO Implement details service when User Extension is ready.
        return new MapReactiveUserDetailsService(
            // for test
            User.withDefaultPasswordEncoder().username("user").password("password").roles("USER")
                .build(),
            // for test
            User.withDefaultPasswordEncoder().username("admin").password("password").roles("ADMIN")
                .build());
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
