package run.halo.app.security.switchuser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;

@SpringBootTest
@AutoConfigureWebTestClient
class SwitchUserConfigurerTest {

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    ReactiveUserDetailsService userDetailsService;

    @MockitoSpyBean
    UserService userService;

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
                .uri("/login/impersonate?username={username}&redirect_uri={redirect_uri}", "faker", "/fake-success")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/fake-success")
                .expectCookie()
                .exists("SESSION")
                .expectBody()
                .returnResult();
        var session = result.getResponseCookies().getFirst("SESSION");
        assertNotNull(session);
    }

    @Test
    @WithSwitchUser(
            username = "admin",
            roles = {"super-role"},
            targetUsername = "faker",
            targetRoles = {"user"})
    void shouldLogoutSuccessfully() {
        webClient
                .mutateWith(csrf())
                .post()
                .uri("/logout/impersonate?redirect_uri={redirect_uri}", "/fake-logout-success")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/fake-logout-success");
    }

    @Test
    @WithMockUser(username = "admin", roles = "non-super-role")
    void shouldNotSwitchWithoutSuperRole() {
        webClient
                .mutateWith(csrf())
                .post()
                .uri("/login/impersonate?username={username}&redirect_uri={redirect_uri}", "faker", "/fake-success")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @WithMockUser(username = "admin", roles = "super-role")
    void shouldRenderSwitchUserPageWithSuperRole() {
        stubFakeUser();
        webClient
                .get()
                .uri("/login/impersonate?username={username}", "faker")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains("action=\"/login/impersonate?username=faker\""));
                    assertTrue(body.contains("name=\"_csrf\""));
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = "non-super-role")
    void shouldNotRenderSwitchUserPageWithoutSuperRole() {
        webClient
                .get()
                .uri("/login/impersonate?username={username}", "faker")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @WithMockUser(username = "admin", roles = "super-role")
    void shouldGetBadRequestIfUsernameMissing() {
        webClient.get().uri("/login/impersonate").exchange().expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "admin", roles = "super-role")
    void shouldGetNotFoundIfUserNotExists() {
        webClient
                .get()
                .uri("/login/impersonate?username={username}", "non-existent-user")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    @WithSwitchUser(
            username = "admin",
            roles = {"super-role"},
            targetUsername = "faker",
            targetRoles = {"user"})
    void shouldRenderExitSwitchUserPageWhileSwitching() {
        stubFakeUser();
        webClient
                .get()
                .uri("/logout/impersonate")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains("action=\"/logout/impersonate\""));
                    assertTrue(body.contains("name=\"_csrf\""));
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = "super-role")
    void shouldNotRenderExitSwitchUserPageIfNotSwitching() {
        webClient.get().uri("/logout/impersonate").exchange().expectStatus().isForbidden();
    }

    private void stubFakeUser() {
        var user = new run.halo.app.core.extension.User();
        var metadata = new Metadata();
        metadata.setName("faker");
        user.setMetadata(metadata);
        user.setSpec(new run.halo.app.core.extension.User.UserSpec());
        user.getSpec().setDisplayName("Faker");
        doReturn(Mono.just(user)).when(userService).getUser("faker");
    }
}
