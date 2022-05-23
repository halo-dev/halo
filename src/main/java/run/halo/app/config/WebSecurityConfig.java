package run.halo.app.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.identity.authentication.InMemoryOAuth2AuthorizationService;
import run.halo.app.identity.authentication.JwtGenerator;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2PasswordAuthenticationProvider;
import run.halo.app.identity.authentication.OAuth2RefreshTokenAuthenticationProvider;
import run.halo.app.identity.authentication.OAuth2TokenEndpointFilter;
import run.halo.app.identity.authentication.ProviderContextFilter;
import run.halo.app.identity.authentication.ProviderSettings;
import run.halo.app.identity.authentication.verifier.BearerTokenAuthenticationFilter;
import run.halo.app.identity.authentication.verifier.JwtProvidedDecoderAuthenticationManagerResolver;
import run.halo.app.identity.authorization.DefaultRoleBindingLister;
import run.halo.app.identity.authorization.DefaultRoleGetter;
import run.halo.app.identity.authorization.RequestInfoAuthorizationManager;
import run.halo.app.identity.authorization.RoleBindingLister;
import run.halo.app.identity.authorization.RoleGetter;
import run.halo.app.identity.entrypoint.JwtAccessDeniedHandler;
import run.halo.app.identity.entrypoint.JwtAuthenticationEntryPoint;
import run.halo.app.infra.properties.JwtProperties;

/**
 * @author guqing
 * @since 2022-04-12
 */
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class WebSecurityConfig {

    private final RSAPublicKey key;

    private final RSAPrivateKey priv;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final ExtensionClient extensionClient;

    public WebSecurityConfig(JwtProperties jwtProperties,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        ExtensionClient extensionClient) throws IOException {
        this.key = jwtProperties.readPublicKey();
        this.priv = jwtProperties.readPrivateKey();
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.extensionClient = extensionClient;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ProviderSettings providerSettings = providerSettings();
        ProviderContextFilter providerContextFilter = new ProviderContextFilter(providerSettings);
        http
            .authorizeHttpRequests((authorize) -> authorize
                .antMatchers(providerSettings.getTokenEndpoint()).permitAll()
                // for static path
                .antMatchers("/static/js/**").permitAll()
                // for swagger ui
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .antMatchers("/api/**", "/apis/**").access(requestInfoAuthorizationManager())
                .anyRequest().access(requestInfoAuthorizationManager())
            )
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .addFilterBefore(new OAuth2TokenEndpointFilter(authenticationManager(),
                    providerSettings.getTokenEndpoint()),
                FilterSecurityInterceptor.class)
            .addFilterBefore(new BearerTokenAuthenticationFilter(authenticationManagerResolver()),
                BasicAuthenticationFilter.class)
            .addFilterAfter(providerContextFilter, SecurityContextHolderFilter.class)
            .sessionManagement(
                (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDeniedHandler())
            );
        return http.build();
    }

    RequestInfoAuthorizationManager requestInfoAuthorizationManager() {
        RoleBindingLister roleBindingLister = new DefaultRoleBindingLister();
        RoleGetter roleGetter = new DefaultRoleGetter(extensionClient);
        return new RequestInfoAuthorizationManager(roleGetter, roleBindingLister);
    }

    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        return new JwtProvidedDecoderAuthenticationManagerResolver(jwtDecoder());
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        authenticationManagerBuilder.authenticationProvider(passwordAuthenticationProvider())
            .authenticationProvider(oauth2RefreshTokenAuthenticationProvider());
        return authenticationManagerBuilder.getOrBuild();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    OAuth2AuthorizationService oauth2AuthorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    OAuth2PasswordAuthenticationProvider passwordAuthenticationProvider() {
        OAuth2PasswordAuthenticationProvider authenticationProvider =
            new OAuth2PasswordAuthenticationProvider(jwtGenerator(), oauth2AuthorizationService());
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    OAuth2RefreshTokenAuthenticationProvider oauth2RefreshTokenAuthenticationProvider() {
        return new OAuth2RefreshTokenAuthenticationProvider(oauth2AuthorizationService(),
            jwtGenerator());
    }

    @Bean
    JwtGenerator jwtGenerator() {
        return new JwtGenerator(jwtEncoder());
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // TODO fake role and role bindings, only used for testing/development
        //  It'll be deleted next time
        UserDetails user = User.withUsername("user")
            .password(passwordEncoder().encode("123456"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }
}
