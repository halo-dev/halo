package run.halo.app.security.switchuser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.security.authorization.AuthorityUtils;


@SpringBootTest
@AutoConfigureWebTestClient
class SwitchUserConfigurerTest {

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    ReactiveUserDetailsService userDetailsService;

    @Test
    void shouldSwitchWithSuperRole() {
        when(userDetailsService.findByUsername("faker"))
            .thenReturn(Mono.fromSupplier(() -> User.withUsername("faker")
                .password("password")
                .roles("user")
                .build()));
        var result = webClient.mutateWith(csrf())
            .mutateWith(mockUser("admin").roles(AuthorityUtils.SUPER_ROLE_NAME))
            .post()
            .uri("/login/impersonate?username={username}&redirect_uri={redirect_uri}",
                "faker", "/fake-success"
            )
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/fake-success")
            .expectCookie().exists("SESSION")
            .expectBody().returnResult();
        var session = result.getResponseCookies().getFirst("SESSION");
        assertNotNull(session);

        webClient.mutateWith(csrf())
            .post().uri("/logout/impersonate?redirect_uri={redirect_uri}", "/fake-logout-success")
            .cookie(session.getName(), session.getValue())
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/fake-logout-success");
    }

    @Test
    @WithMockUser(username = "admin", roles = "non-super-role")
    void shouldNotSwitchWithoutSuperRole() {
        webClient.mutateWith(csrf())
            .post()
            .uri("/login/impersonate?username={username}&redirect_uri={redirect_uri}",
                "faker", "/fake-success"
            )
            .exchange()
            .expectStatus().isForbidden();
    }

}
