package run.halo.app.core.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.user.service.DefaultRoleService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link DefaultRoleService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultRoleServiceTest {
    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private DefaultRoleService roleService;

    @ParameterizedTest
    @MethodSource("usernamesProvider")
    void shouldReturnEmptyMapIfNoUsernamesProvided(Collection<String> usernames) {
        roleService.getRolesByUsernames(usernames)
            .as(StepVerifier::create)
            .expectNext(Map.of())
            .verifyComplete();
    }

    static Stream<Collection<String>> usernamesProvider() {
        return Stream.of(null, List.of(), Set.of());
    }

    @Nested
    class ListDependenciesTest {
        @Test
        void listDependencies() {
            // prepare test data
            var role1 = createRole("role1", "role2");
            var role2 = createRole("role2", "role3");
            var role3 = createRole("role3");

            var roleNames = Set.of("role1");


            when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(role1))
                .thenReturn(Flux.just(role2))
                .thenReturn(Flux.just(role3))
                .thenReturn(Flux.empty());

            // call the method under test
            var result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(client, times(4)).listAll(
                same(Role.class),
                any(ListOptions.class),
                any(Sort.class)
            );
        }

        @Test
        void listDependenciesWithCycle() {
            // prepare test data
            var role1 = createRole("role1", "role2");
            var role2 = createRole("role2", "role3");
            var role3 = createRole("role3", "role1");

            var roleNames = Set.of("role1");

            // setup mocks
            when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(role1))
                .thenReturn(Flux.just(role2))
                .thenReturn(Flux.just(role3))
                .thenReturn(Flux.empty());

            // call the method under test
            var result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(client, times(4)).listAll(
                same(Role.class),
                any(ListOptions.class),
                any(Sort.class)
            );
        }

        @Test
        void listDependenciesWithMiddleCycle() {
            // prepare test data
            // role1 -> role2 -> role3 -> role4
            //             \<-----|
            var role1 = createRole("role1", "role2");
            var role2 = createRole("role2", "role3");
            var role3 = createRole("role3", "role2", "role4");
            var role4 = createRole("role4");

            var roleNames = Set.of("role1");

            when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(role1))
                .thenReturn(Flux.just(role2))
                .thenReturn(Flux.just(role3))
                .thenReturn(Flux.just(role4))
                .thenReturn(Flux.empty());

            // call the method under test
            var result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role3)
                .expectNext(role4)
                .verifyComplete();

            // verify the mock invocations
            verify(client, times(5)).listAll(
                same(Role.class),
                any(ListOptions.class),
                any(Sort.class)
            );
        }

        @Test
        void listDependenciesWithCycleAndSequence() {
            // prepare test data
            // role1 -> role2 -> role3
            //   \->role4  \<-----|
            Role role1 = createRole("role1", "role4", "role2");
            Role role2 = createRole("role2", "role3");
            Role role3 = createRole("role3", "role2");
            Role role4 = createRole("role4");

            Set<String> roleNames = Set.of("role1");

            when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(role1))
                .thenReturn(Flux.just(role4, role2))
                .thenReturn(Flux.just(role3))
                .thenReturn(Flux.empty());

            // call the method under test
            var result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role4)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(client, times(4)).listAll(same(Role.class), any(), any());
        }

        @Test
        void listDependenciesAfterCycle() {
            // prepare test data
            // role1 -> role2 -> role3
            //   \->role4  \<-----|
            Role role1 = createRole("role1", "role4", "role2");
            Role role2 = createRole("role2", "role3");
            Role role3 = createRole("role3", "role2");
            Role role4 = createRole("role4");

            Set<String> roleNames = Set.of("role2");

            when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(role2))
                .thenReturn(Flux.just(role3))
                .thenReturn(Flux.empty());

            // call the method under test
            var result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(client, times(3)).listAll(
                same(Role.class),
                any(ListOptions.class),
                any(Sort.class)
            );
        }

        @Test
        void listDependenciesWithNullParam() {
            var result = roleService.listDependenciesFlux(null);

            // verify the result
            StepVerifier.create(result)
                .verifyComplete();

            result = roleService.listDependenciesFlux(Set.of());
            StepVerifier.create(result)
                .verifyComplete();

            // verify the mock invocations
            verify(client, never()).listAll(
                eq(Role.class),
                any(ListOptions.class),
                any(Sort.class)
            );
        }

        @Test
        void listDependenciesAndSomeOneNotFound() {
            var role1 = createRole("role1", "role2");
            var role2 = createRole("role2", "role3", "role4");
            var role4 = createRole("role4");

            var roleNames = Set.of("role1");

            when(client.listAll(same(Role.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(role1))
                .thenReturn(Flux.just(role2))
                .thenReturn(Flux.just(role4))
                .thenReturn(Flux.empty())
            ;

            var result = roleService.listDependenciesFlux(roleNames);
            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role4)
                .verifyComplete();

            // verify the mock invocations
            verify(client, times(4)).listAll(
                same(Role.class),
                any(ListOptions.class),
                any(Sort.class)
            );
        }

        @Test
        void testSubjectMatch() {
            RoleBinding fakeAuthenticatedBinding =
                createRoleBinding("authenticated-fake-binding", "fake", "authenticated");
            RoleBinding fakeEditorBinding =
                createRoleBinding("editor-fake-binding", "fake", "editor");
            RoleBinding fakeAnonymousBinding =
                createRoleBinding("test-anonymous-binding", "test", "anonymous");

            RoleBinding.Subject subject = new RoleBinding.Subject();
            subject.setName("authenticated");
            subject.setKind(Role.KIND);
            subject.setApiGroup(Role.GROUP);

            Predicate<RoleBinding> predicate = roleService.getRoleBindingPredicate(subject);
            List<RoleBinding> result =
                Stream.of(fakeAuthenticatedBinding, fakeEditorBinding, fakeAnonymousBinding)
                    .filter(predicate)
                    .toList();
            AssertionsForInterfaceTypes.assertThat(result)
                .containsExactly(fakeAuthenticatedBinding);

            subject.setName("editor");
            predicate = roleService.getRoleBindingPredicate(subject);
            result =
                Stream.of(fakeAuthenticatedBinding, fakeEditorBinding, fakeAnonymousBinding)
                    .filter(predicate)
                    .toList();
            AssertionsForInterfaceTypes.assertThat(result).containsExactly(fakeEditorBinding);
        }

        RoleBinding createRoleBinding(String name, String refName, String subjectName) {
            RoleBinding roleBinding = new RoleBinding();
            roleBinding.setMetadata(new Metadata());
            roleBinding.getMetadata().setName(name);
            roleBinding.setRoleRef(new RoleBinding.RoleRef());
            roleBinding.getRoleRef().setKind(Role.KIND);
            roleBinding.getRoleRef().setApiGroup(Role.GROUP);
            roleBinding.getRoleRef().setName(refName);
            roleBinding.setSubjects(List.of(new RoleBinding.Subject()));
            roleBinding.getSubjects().get(0).setKind(Role.KIND);
            roleBinding.getSubjects().get(0).setName(subjectName);
            roleBinding.getSubjects().get(0).setApiGroup(Role.GROUP);
            return roleBinding;
        }

        private Role createRole(String name, String... dependencies) {
            Role role = new Role();
            role.setMetadata(new Metadata());
            role.getMetadata().setName(name);

            Map<String, String> annotations = new HashMap<>();
            annotations.put(Role.ROLE_DEPENDENCIES_ANNO, JsonUtils.objectToJson(dependencies));
            role.getMetadata().setAnnotations(annotations);
            return role;
        }
    }
}
