package run.halo.app.integration.security;

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
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
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
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.test.context.TestPropertySource;
import run.halo.app.extension.Metadata;
import run.halo.app.identity.apitoken.DefaultPersonalAccessTokenDecoder;
import run.halo.app.identity.apitoken.PersonalAccessTokenDecoder;
import run.halo.app.identity.apitoken.PersonalAccessTokenUtils;
import run.halo.app.identity.authentication.InMemoryOAuth2AuthorizationService;
import run.halo.app.identity.authentication.JwtGenerator;
import run.halo.app.identity.authentication.OAuth2AuthorizationService;
import run.halo.app.identity.authentication.OAuth2PasswordAuthenticationProvider;
import run.halo.app.identity.authentication.OAuth2RefreshTokenAuthenticationProvider;
import run.halo.app.identity.authentication.OAuth2TokenEndpointFilter;
import run.halo.app.identity.authentication.ProviderContextFilter;
import run.halo.app.identity.authentication.ProviderSettings;
import run.halo.app.identity.authentication.verifier.BearerTokenAuthenticationFilter;
import run.halo.app.identity.authentication.verifier.JwtAccessTokenNonBlockedValidator;
import run.halo.app.identity.authentication.verifier.TokenAuthenticationManagerResolver;
import run.halo.app.identity.authorization.PolicyRule;
import run.halo.app.identity.authorization.RequestInfoAuthorizationManager;
import run.halo.app.identity.authorization.Role;
import run.halo.app.identity.entrypoint.Oauth2LogoutHandler;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.infra.utils.HaloUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@TestConfiguration
@EnableWebSecurity
@TestPropertySource(properties = {"halo.security.oauth2.jwt.public-key-location=classpath:app.pub",
    "halo.security.oauth2.jwt.private-key-location=classpath:app.key"})
@EnableConfigurationProperties(JwtProperties.class)
public class TestWebSecurityConfig {
    private final RSAPublicKey key;

    private final RSAPrivateKey priv;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public TestWebSecurityConfig(JwtProperties jwtProperties,
        AuthenticationManagerBuilder authenticationManagerBuilder) throws IOException {
        this.key = jwtProperties.readPublicKey();
        this.priv = jwtProperties.readPrivateKey();
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ProviderSettings providerSettings = providerSettings();
        ProviderContextFilter providerContextFilter = new ProviderContextFilter(providerSettings);
        http
            .authorizeHttpRequests((authorize) -> authorize
                .antMatchers(providerSettings.getTokenEndpoint()).permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/logout").authenticated()
                .antMatchers("/api/**", "/apis/**").access(requestInfoAuthorizationManager())
                .anyRequest().access(requestInfoAuthorizationManager())
            )
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .logout(logoutConfigurer -> {
                logoutConfigurer.addLogoutHandler(oauth2LogoutHandler())
                    .clearAuthentication(true);
            })
            .addFilterBefore(new OAuth2TokenEndpointFilter(authenticationManager(),
                    providerSettings.getTokenEndpoint()),
                FilterSecurityInterceptor.class)
            .addFilterBefore(new BearerTokenAuthenticationFilter(authenticationManagerResolver()),
                LogoutFilter.class)
            .addFilterAfter(providerContextFilter, SecurityContextHolderFilter.class)
            .sessionManagement(
                (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    Oauth2LogoutHandler oauth2LogoutHandler() {
        return new Oauth2LogoutHandler(oauth2AuthorizationService());
    }

    public RequestInfoAuthorizationManager requestInfoAuthorizationManager() {
        return new RequestInfoAuthorizationManager(name -> {
            // role getter
            Role role = new Role();
            List<PolicyRule> rules = List.of(
                new PolicyRule.Builder().apiGroups("").resources("posts").verbs("list", "get")
                    .build(),
                new PolicyRule.Builder().apiGroups("").resources("categories").verbs("*")
                    .build(),
                new PolicyRule.Builder().nonResourceURLs("/healthy").verbs("get", "post", "head")
                    .build()
            );
            role.setRules(rules);
            Metadata metadata = new Metadata();
            metadata.setName("ruleReadPost");
            role.setMetadata(metadata);
            return role;
        });
    }

    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        return new TokenAuthenticationManagerResolver(jwtDecoder(), personalAccessTokenDecoder());
    }

    @Bean
    PersonalAccessTokenDecoder personalAccessTokenDecoder() {
        String salt = HaloUtils.readClassPathResourceAsString("apiToken.salt");
        SecretKey secretKey = PersonalAccessTokenUtils.convertStringToSecretKey(salt);
        return new DefaultPersonalAccessTokenDecoder(oauth2AuthorizationService(), secretKey);
    }

    @Bean
    AuthenticationManager authenticationManager() {
        authenticationManagerBuilder.authenticationProvider(passwordAuthenticationProvider())
            .authenticationProvider(oauth2RefreshTokenAuthenticationProvider());
        return authenticationManagerBuilder.getOrBuild();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(this.key).build();

        JwtAccessTokenNonBlockedValidator jwtAccessTokenNonBlockedValidator =
            new JwtAccessTokenNonBlockedValidator(oauth2AuthorizationService());
        OAuth2TokenValidator<Jwt> jwtValidator = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            jwtAccessTokenNonBlockedValidator);

        jwtDecoder.setJwtValidator(jwtValidator);
        return jwtDecoder;
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
        UserDetails user = User.withUsername("test_user")
            .password(passwordEncoder().encode("123456"))
            .roles("ruleReadPost")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }
}
