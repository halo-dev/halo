package run.halo.app.security.authentication.pat;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.pat.PersonalAccessToken;
import run.halo.app.core.extension.pat.PersonalAccessToken.PersonalAccessTokenSpec;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(PatAuthenticationTest.TestConfig.class)
public class PatAuthenticationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    JwtEncoder jwtEncoder;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @Test
    void requestApiWithPat() {
        var claims = JwtClaimsSet.builder()
            .claim("patName", "my-pat")
            .build();
        var encoderParameters = JwtEncoderParameters.from(claims);
        var jwt = jwtEncoder.encode(encoderParameters);

        var pat = new PersonalAccessToken();
        var metadata = new Metadata();
        metadata.setName("my-pat");
        pat.setMetadata(metadata);
        var spec = new PersonalAccessTokenSpec();
        spec.setCreatedBy("admin");
        spec.setDescription("My pat");
        spec.setScopes(Set.of("scoped:read"));
        spec.setEncodedToken(passwordEncoder.encode(jwt.getTokenValue()));
        pat.setSpec(spec);

        User user = new User();
        var userMetadata = new Metadata();
        userMetadata.setName("admin");
        user.setMetadata(userMetadata);
        var userSpec = new User.UserSpec();
        userSpec.setEmail("fake@halo.run");
        userSpec.setDisplayName("Admin");
        userSpec.setPassword(passwordEncoder.encode("123456"));
        user.setSpec(userSpec);

        Metadata roleMetadata = new Metadata();
        roleMetadata.setName("fake-role");
        var role = new Role();
        role.setMetadata(roleMetadata);
        role.setRules(List.of(new Role.PolicyRule.Builder()
            .nonResourceURLs("/api/scoped")
            .verbs("*")
            .build()));
        StepVerifier.create(client.create(role))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(client.create(pat))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(client.create(user))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(userService.grantRoles("admin", Set.of("fake-role")))
            .expectNextCount(1)
            .verifyComplete();

        webClient.get()
            .uri("/api/scoped")
            .exchange()
            .expectStatus().isUnauthorized();

        webClient.get()
            .uri("/api/scoped")
            .header(HttpHeaders.AUTHORIZATION, "Bearer pat_" + jwt.getTokenValue())
            .exchange()
            .expectStatus().isOk();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        RouterFunction<ServerResponse> scopedEndpoint() {
            return RouterFunctions.route()
                .GET("/api/scoped", request -> ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .doOnNext(authentication -> {
                        if (authentication instanceof PatAuthenticationToken) {
                            // check the scope
                            authentication.getAuthorities()
                                .stream().map(GrantedAuthority::getAuthority)
                                .filter(
                                    authority -> Objects.equals(authority, "SCOPE_scoped:read"))
                                .findFirst()
                                .orElseThrow(() -> new InsufficientAuthenticationException(
                                    "Insufficient scope"));
                        }
                    })
                    .flatMap(a -> ServerResponse.ok().build()))
                .build();
        }
    }

}
