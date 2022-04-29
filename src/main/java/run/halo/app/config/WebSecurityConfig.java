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
import java.util.List;
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
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
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
import run.halo.app.identity.authorization.AuthorizationFilter;
import run.halo.app.identity.authorization.PolicyRule;
import run.halo.app.identity.authorization.Role;
import run.halo.app.identity.authorization.RoleBinding;
import run.halo.app.identity.authorization.RoleRef;
import run.halo.app.identity.authorization.Subject;
import run.halo.app.identity.entrypoint.JwtAccessDeniedHandler;
import run.halo.app.identity.entrypoint.JwtAuthenticationEntryPoint;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.infra.types.ObjectMeta;

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

    public WebSecurityConfig(JwtProperties jwtProperties,
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
                .antMatchers("/api/**", "/apis/**").authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .addFilterBefore(new OAuth2TokenEndpointFilter(authenticationManager(),
                    providerSettings.getTokenEndpoint()),
                FilterSecurityInterceptor.class)
            .addFilterBefore(new BearerTokenAuthenticationFilter(authenticationManagerResolver()),
                BasicAuthenticationFilter.class)
            .addFilterAfter(providerContextFilter, SecurityContextPersistenceFilter.class)
            .addFilterBefore(authorizationFilter(), FilterSecurityInterceptor.class)
            .sessionManagement(
                (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDeniedHandler())
            );
        return http.build();
    }

    public AuthorizationFilter authorizationFilter() {
        // TODO fake role and role bindings, only used for testing/development
        //  It'll be deleted next time
        return new AuthorizationFilter(name -> {
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
            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName("ruleReadPost");
            role.setObjectMeta(objectMeta);
            return role;
        }, () -> {
            // role binding lister
            RoleBinding roleBinding = new RoleBinding();

            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName("userRoleBinding");
            roleBinding.setObjectMeta(objectMeta);

            Subject subject = new Subject();
            subject.setName("user");
            subject.setKind("User");
            subject.setApiGroup("");
            roleBinding.setSubjects(List.of(subject));

            RoleRef roleRef = new RoleRef();
            roleRef.setKind("Role");
            roleRef.setName("ruleReadPost");
            roleRef.setApiGroup("");
            roleBinding.setRoleRef(roleRef);

            return List.of(roleBinding);
        });
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
