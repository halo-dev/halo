package run.halo.app.security.authorization;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.AnonymousUserConst;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(AuthorizationTest.TestConfig.class)
class AuthorizationTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ReactiveUserDetailsService userDetailsService;

    @MockBean
    ReactiveUserDetailsPasswordService userDetailsPasswordService;

    @MockBean
    RoleService roleService;

    @BeforeEach
    void setUp() {
        webClient = webClient.mutateWith(csrf());
    }

    @Test
    void anonymousUserAccessProtectedApi() {
        when(userDetailsService.findByUsername(eq(AnonymousUserConst.PRINCIPAL)))
            .thenReturn(Mono.empty());
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.empty());

        webClient.get().uri("/apis/fake.halo.run/v1/posts").exchange().expectStatus()
            .isUnauthorized();

        verify(roleService).listDependenciesFlux(anySet());
    }

    @Test
    void anonymousUserAccessAuthenticationFreeApi() {
        when(userDetailsService.findByUsername(eq(AnonymousUserConst.PRINCIPAL)))
            .thenReturn(Mono.empty());
        Role role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName(AnonymousUserConst.Role);
        role.setRules(new ArrayList<>());
        PolicyRule policyRule = new PolicyRule.Builder()
            .apiGroups("fake.halo.run")
            .verbs("list")
            .resources("posts")
            .build();
        role.getRules().add(policyRule);
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));
        webClient.get().uri("/apis/fake.halo.run/v1/posts").exchange().expectStatus()
            .isOk()
            .expectBody(String.class).isEqualTo("returned posts");

        verify(roleService).listDependenciesFlux(anySet());

        webClient.get().uri("/apis/fake.halo.run/v1/posts/hello-halo").exchange()
            .expectStatus()
            .isUnauthorized();

        verify(roleService, times(2)).listDependenciesFlux(anySet());
    }

    @Test
    @WithMockUser(username = "user", roles = "post.read")
    void authenticatedUserAccessAuthenticationFreeApi() {
        Role role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName(AnonymousUserConst.Role);
        role.setRules(new ArrayList<>());
        PolicyRule policyRule = new PolicyRule.Builder()
            .apiGroups("fake.halo.run")
            .verbs("list")
            .resources("posts")
            .build();
        role.getRules().add(policyRule);

        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));

        webClient.get().uri("/apis/fake.halo.run/v1/posts").exchange().expectStatus()
            .isOk()
            .expectBody(String.class).isEqualTo("returned posts");
        verify(roleService).listDependenciesFlux(anySet());
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
