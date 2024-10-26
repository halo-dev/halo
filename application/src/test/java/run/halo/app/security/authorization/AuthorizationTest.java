package run.halo.app.security.authorization;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static run.halo.app.core.extension.Role.ROLE_AGGREGATE_LABEL_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.AnonymousUserConst;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
@Import(AuthorizationTest.TestConfig.class)
class AuthorizationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    ReactiveUserDetailsService userDetailsService;

    @MockitoSpyBean
    ReactiveUserDetailsPasswordService userDetailsPasswordService;

    @MockitoSpyBean
    RoleService roleService;

    @Autowired
    ExtensionClient client;

    @BeforeEach
    void setUp() {
        webClient = webClient.mutateWith(csrf());
    }

    @Test
    void anonymousUserAccessProtectedApi() {
        when(userDetailsService.findByUsername(eq(AnonymousUserConst.PRINCIPAL)))
            .thenReturn(Mono.empty());

        webClient.get().uri("/apis/fake.halo.run/v1/posts")
            .header("X-Requested-With", "XMLHttpRequest")
            .exchange()
            .expectStatus().isUnauthorized();

        webClient.get().uri("/apis/fake.halo.run/v1/posts")
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/login?authentication_required");

        verify(roleService, times(2)).listDependenciesFlux(anySet());
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

        webClient.get().uri("/apis/fake.halo.run/v1/posts/hello-halo")
            .header("X-Requested-With", "XMLHttpRequest")
            .exchange()
            .expectStatus()
            .isUnauthorized();

        webClient.get().uri("/apis/fake.halo.run/v1/posts/hello-halo")
            .exchange()
            .expectStatus()
            .isFound()
            .expectHeader().location("/login?authentication_required");

        verify(roleService, times(3)).listDependenciesFlux(anySet());
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

    @Test
    void anonymousUserShouldAccessResourcesByAggregatedRoles() {
        // create a role
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("fake-role-with-aggregate-to-anonymous");
        role.getMetadata().setLabels(new HashMap<>(Map.of(
            ROLE_AGGREGATE_LABEL_PREFIX + AnonymousUserConst.Role, "true"
        )));
        role.setRules(new ArrayList<>());
        var policyRule = new PolicyRule.Builder()
            .apiGroups("fake.halo.run")
            .verbs("list")
            .resources("fakes")
            .build();
        role.getRules().add(policyRule);
        client.create(role);

        webClient.get().uri("/apis/fake.halo.run/v1/fakes").exchange()
            .expectStatus()
            .isOk();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RouterFunction<ServerResponse> postRoute() {
            return RouterFunctions.route()
                .GET("/apis/fake.halo.run/v1/posts", request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue("returned posts")
                )
                .PUT("/apis/fake.halo.run/v1/posts/{name}", request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue("updated post " + request.pathVariable("name"))
                )
                .GET("/apis/fake.halo.run/v1/fakes", request -> ServerResponse.ok().build())
                .build();
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
