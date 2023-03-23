package run.halo.app.security.authentication.jwt;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.security.LoginUtils;

@Disabled
@SpringBootTest
@AutoConfigureWebTestClient
class JwtAuthenticationTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ReactiveUserDetailsService userDetailsService;

    @MockBean
    RoleService roleService;

    @BeforeEach
    void setUp() {
        lenient().when(roleService.getMonoRole(eq(AnonymousUserConst.Role)))
            .thenReturn(Mono.empty());
        webClient = webClient.mutateWith(csrf());
    }

    @Test
    void accessProtectedApiWithoutToken() {
        webClient.get().uri("/api/v1/test/hello").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessProtectedApiUsingBearerToken() {
        when(userDetailsService.findByUsername(anyString())).thenReturn(Mono.just(
            User.withDefaultPasswordEncoder()
                .username("username")
                .password("password")
                .roles("USER")
                .build()
        ));

        var role = new Role();
        var metadata = new Metadata();
        metadata.setName("USER");
        role.setMetadata(metadata);
        role.setRules(List.of(new Role.PolicyRule.Builder()
            .apiGroups("")
            .resources("test")
            .resourceNames("hello")
            .build()));
        when(roleService.getMonoRole("authenticated")).thenReturn(Mono.empty());
        when(roleService.getMonoRole("USER")).thenReturn(Mono.just(role));

        final var token = LoginUtils.login(webClient, "username", "password").block();
        webClient.get().uri("/api/v1/test/hello")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().isForbidden()
            .expectHeader().value(HttpHeaders.WWW_AUTHENTICATE,
                containsString("insufficient_scope"));
    }

}
