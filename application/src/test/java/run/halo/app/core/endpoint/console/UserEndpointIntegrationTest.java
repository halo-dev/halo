package run.halo.app.core.endpoint.console;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "fake-user", password = "fake-password", roles = "fake-super-role")
public class UserEndpointIntegrationTest {
    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoBean
    RoleService roleService;

    @BeforeEach
    void setUp() {
        var rule = new Role.PolicyRule.Builder()
            .apiGroups("*")
            .resources("*")
            .verbs("*")
            .build();
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("fake-super-role");
        role.setRules(List.of(rule));
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));
        webClient = webClient.mutateWith(csrf());
    }

    @Nested
    class UserListTest {
        @Test
        void shouldFilterUsersWhenDisplayNameKeywordProvided() {
            var expectUser =
                createUser("fake-user-2", "expected display name");
            var unexpectedUser1 =
                createUser("fake-user-1", "first fake display name");
            var unexpectedUser2 =
                createUser("fake-user-3", "second fake display name");

            client.create(expectUser).block();
            client.create(unexpectedUser1).block();
            client.create(unexpectedUser2).block();

            when(roleService.list(anySet())).thenReturn(Flux.empty());
            when(roleService.getRolesByUsernames(
                List.of("fake-user-2")
            )).thenReturn(Mono.just(Map.of("fake-user-2", Set.of("fake-super-role"))));

            webClient.get().uri("/apis/api.console.halo.run/v1alpha1/users?keyword=Expected")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].user.metadata.name").isEqualTo("fake-user-2");

        }

        @Test
        void shouldFilterUsersWhenUserNameKeywordProvided() {
            var expectUser =
                createUser("fake-user", "expected display name");
            var unexpectedUser1 =
                createUser("fake-user-1", "first fake display name");
            var unexpectedUser2 =
                createUser("fake-user-3", "second fake display name");

            client.create(expectUser).block();
            client.create(unexpectedUser1).block();
            client.create(unexpectedUser2).block();

            when(roleService.list(anySet())).thenReturn(Flux.empty());
            when(roleService.getRolesByUsernames(List.of("fake-user")))
                .thenReturn(Mono.just(Map.of("fake-user", Set.of("fake-super-role"))));

            webClient.get().uri("/apis/api.console.halo.run/v1alpha1/users?keyword=fake-user")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].user.metadata.name").isEqualTo("fake-user");
        }
    }

    User createUser(String name, String displayName) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.now());
        var spec = new User.UserSpec();
        spec.setEmail("fake-email");
        spec.setDisplayName(displayName);
        var user = new User();
        user.setMetadata(metadata);
        user.setSpec(spec);
        return user;
    }
}
