package run.halo.app.security.switchuser;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.AutoConfigureWebTestClient;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
class SwitchUserConfigurerTest {

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    ReactiveUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = "super-role")
    void shouldSwitchWithSuperRole() {
        when(userDetailsService.findByUsername("faker"))
            .thenReturn(Mono.fromSupplier(() -> User.withUsername("faker")
                .password("password")
                .roles("user")
                .build()));
        var result = webClient
            .mutateWith(csrf())
            .post()
            .uri("/login/impersonate?username={username}&redirect_uri={redirect_uri}",
                "faker", "/fake-success"
            )
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/fake-success")
            .expectCookie().exists("SESSION")
            .expectBody().returnResult();
        //
        // webClient.mutateWith(csrf())
        //     .post().uri("/logout/impersonate?redirect_uri={redirect_uri}", "/fake-logout-success")
        //     .cookie(session.getName(), session.getValue())
        //     .exchange()
        //     .expectStatus().isFound()
        //     .expectHeader().location("/fake-logout-success");
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
