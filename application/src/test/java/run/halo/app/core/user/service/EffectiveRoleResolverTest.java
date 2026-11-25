package run.halo.app.core.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JacksonUtils;

@ExtendWith(MockitoExtension.class)
class EffectiveRoleResolverTest {

    @Mock
    ReactiveExtensionClient client;

    EffectiveRoleResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new EffectiveRoleResolver(client);
    }

    @Test
    void shouldResolveRolesContainingRequiredRole() {
        var roles = List.of(
                role("super-role"),
                role("guest"),
                role("role-template-post-contributor"),
                role("role-template-post-publisher"),
                role("role-template-post-author", "role-template-post-contributor", "role-template-post-publisher"),
                role("post-author", "role-template-post-author"),
                role("role-template-manage-posts", "role-template-post-author"),
                role("post-editor", "role-template-manage-posts"),
                role("custom-writer", "role-template-post-author"),
                role("publisher-only", "role-template-post-publisher"));
        when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(roles));

        StepVerifier.create(resolver.resolveRolesContaining("role-template-post-contributor"))
                .assertNext(result -> assertEquals(
                        Set.of(
                                "super-role",
                                "role-template-post-contributor",
                                "role-template-post-author",
                                "post-author",
                                "role-template-manage-posts",
                                "post-editor",
                                "custom-writer"),
                        result))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptySetWhenRequiredRoleDoesNotExist() {
        var roles = List.of(role("super-role"), role("guest"), role("post-author", "role-template-post-author"));
        when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(roles));

        StepVerifier.create(resolver.resolveRolesContaining("missing-role"))
                .expectNext(Set.of())
                .verifyComplete();
    }

    @Test
    void shouldNotQueryRolesWhenRequiredRoleIsBlank() {
        StepVerifier.create(resolver.resolveRolesContaining(" "))
                .expectNext(Set.of())
                .verifyComplete();

        verify(client, never()).listAll(any(), any(ListOptions.class), any(Sort.class));
    }

    @Test
    void shouldResolveTransitiveDependencies() {
        var roles = List.of(
                role("role-template-post-contributor"),
                role("level-2", "level-1"),
                role("level-1", "role-template-post-contributor"),
                role("level-3", "level-2"));

        var result = resolver.resolveRolesContaining("role-template-post-contributor", roles);

        assertEquals(Set.of("role-template-post-contributor", "level-1", "level-2", "level-3"), result);
    }

    private Role role(String name, String... dependencies) {
        var role = new Role();
        var metadata = new Metadata();
        metadata.setName(name);
        if (dependencies.length > 0) {
            metadata.setAnnotations(
                    Map.of(Role.ROLE_DEPENDENCIES_ANNO, JacksonUtils.objectToJson(List.of(dependencies))));
        }
        role.setMetadata(metadata);
        return role;
    }
}
