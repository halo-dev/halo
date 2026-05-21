package run.halo.app.core.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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
import run.halo.app.infra.utils.JsonUtils;

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
    void shouldResolveRolesContainingPostContributor() {
        var roles = List.of(
                role("super-role"),
                role("guest"),
                role("post-contributor", "role-template-post-contributor"),
                role("role-template-post-contributor"),
                role("role-template-post-publisher"),
                role("role-template-post-author", "role-template-post-contributor", "role-template-post-publisher"),
                role("post-author", "role-template-post-author"),
                role("role-template-manage-posts", "role-template-post-author"),
                role("post-editor", "role-template-manage-posts"),
                role("custom-author", "role-template-post-contributor"));
        when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(roles));

        StepVerifier.create(resolver.resolveRolesContaining("role-template-post-contributor"))
                .assertNext(result -> assertEquals(
                        Set.of(
                                "super-role",
                                "post-contributor",
                                "role-template-post-contributor",
                                "role-template-post-author",
                                "post-author",
                                "role-template-manage-posts",
                                "post-editor",
                                "custom-author"),
                        result))
                .verifyComplete();
    }

    @Test
    void shouldResolveAggregateRolesContainingTargetRole() {
        var roles = List.of(
                role("role-template-post-contributor"),
                role("base-author"),
                aggregateRole("aggregated-author", "base-author", "role-template-post-contributor"));
        when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(roles));

        StepVerifier.create(resolver.resolveRolesContaining("role-template-post-contributor"))
                .assertNext(result -> assertEquals(
                        Set.of("role-template-post-contributor", "base-author", "aggregated-author"), result))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptySetWhenTargetRoleIsUnknown() {
        var roles = List.of(role("super-role"), role("post-author", "role-template-post-author"));
        when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(roles));

        StepVerifier.create(resolver.resolveRolesContaining("missing-role"))
                .expectNext(Set.of())
                .verifyComplete();
    }

    private Role role(String name, String... dependencies) {
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName(name);
        if (dependencies.length > 0) {
            role.getMetadata()
                    .setAnnotations(Map.of(Role.ROLE_DEPENDENCIES_ANNO, JsonUtils.objectToJson(dependencies)));
        }
        return role;
    }

    private Role aggregateRole(String name, String aggregateToRole, String... dependencies) {
        var role = role(name, dependencies);
        var labels = new HashMap<String, String>();
        labels.put(Role.ROLE_AGGREGATE_LABEL_PREFIX + aggregateToRole, Boolean.TRUE.toString());
        role.getMetadata().setLabels(labels);
        return role;
    }
}
