package run.halo.app.security.authorization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.security.LoginUtils;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(AuthorizationTest.TestConfig.class)
class AuthorizationTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ReactiveUserDetailsService userDetailsService;

    @MockBean
    RoleService roleService;

    @Test
    void accessProtectedApiWithoutSufficientRole() {
        when(userDetailsService.findByUsername(eq("user"))).thenReturn(
            Mono.just(User.withDefaultPasswordEncoder().username("user").password("password")
                .roles("invalid-role").build()));
        when(roleService.getMonoRole(any())).thenReturn(Mono.empty());
        var basicAuth = LoginUtils.buildBasicAuthHeader("user", "password");
        webClient.get().uri("/apis/fake.halo.run/v1/posts")
            .header(HttpHeaders.AUTHORIZATION, basicAuth)
            .exchange()
            .expectStatus().isForbidden();

        verify(roleService, times(1)).getMonoRole("authenticated");
        verify(roleService, times(1)).getMonoRole("invalid-role");
    }

    @Test
    void accessProtectedApiWithSufficientRole() {
        when(userDetailsService.findByUsername(eq("user"))).thenReturn(Mono.just(
            User.withDefaultPasswordEncoder().username("user").password("password")
                .roles("post.read").build()));

        var role = new Role();
        role.setRules(List.of(
            new PolicyRule.Builder().apiGroups("fake.halo.run").verbs("list").resources("posts")
                .build()));

        when(roleService.getMonoRole("post.read")).thenReturn(Mono.just(role));
        when(roleService.getMonoRole("authenticated")).thenReturn(
            Mono.error(ExtensionNotFoundException::new));

        String basicAuth = LoginUtils.buildBasicAuthHeader("user", "password");
        webClient.get().uri("/apis/fake.halo.run/v1/posts")
            .header(HttpHeaders.AUTHORIZATION, basicAuth)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("returned posts");

        webClient.put().uri("/apis/fake.halo.run/v1/posts/hello-halo")
            .header(HttpHeaders.AUTHORIZATION, basicAuth)
            .exchange()
            .expectStatus().isForbidden();

        verify(roleService, times(2)).getMonoRole("authenticated");
        verify(roleService, times(2)).getMonoRole("post.read");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RouterFunction<ServerResponse> postRoute() {
            return route(
                GET("/apis/fake.halo.run/v1/posts").and(accept(MediaType.APPLICATION_JSON)),
                this::queryPosts).andRoute(
                PUT("/apis/fake.halo.run/v1/posts/{name}").and(accept(MediaType.APPLICATION_JSON)),
                this::updatePost);
        }

        @NonNull
        Mono<ServerResponse> queryPosts(ServerRequest request) {
            return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                .bodyValue("returned posts");
        }

        @NonNull
        Mono<ServerResponse> updatePost(ServerRequest request) {
            var name = request.pathVariable("name");
            return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                .bodyValue("updated post " + name);
        }

    }
}
