package run.halo.app.core.extension.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.TestRole;
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
    private ReactiveExtensionClient extensionClient;

    private DefaultRoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new DefaultRoleService(extensionClient);
    }

    @Test
    void listDependencies() {
        Role roleManage = TestRole.getRoleManage();
        Map<String, String> manageAnnotations = new HashMap<>();
        manageAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-view\"]");
        roleManage.getMetadata().setAnnotations(manageAnnotations);

        Role roleView = TestRole.getRoleView();
        Map<String, String> viewAnnotations = new HashMap<>();
        viewAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-other\"]");
        roleView.getMetadata().setAnnotations(viewAnnotations);

        Role roleOther = TestRole.getRoleOther();

        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-manage")))
            .thenReturn(Mono.just(roleManage));
        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-view")))
            .thenReturn(Mono.just(roleView));
        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-other")))
            .thenReturn(Mono.just(roleOther));

        // list without cycle
        List<Role> roles = roleService.listDependencies(Set.of("role-template-apple-manage"));

        verify(extensionClient, times(1)).fetch(same(Role.class), eq("role-template-apple-manage"));
        verify(extensionClient, times(1)).fetch(same(Role.class), eq("role-template-apple-view"));
        verify(extensionClient, times(1)).fetch(same(Role.class), eq("role-template-apple-other"));

        assertThat(roles).hasSize(3);
        assertThat(roles).containsExactly(roleManage, roleView, roleOther);

        // list dependencies with a cycle relation
        Map<String, String> anotherAnnotations = new HashMap<>();
        anotherAnnotations.put(Role.ROLE_DEPENDENCIES_ANNO, "[\"role-template-apple-view\"]");
        roleOther.getMetadata().setAnnotations(anotherAnnotations);
        when(extensionClient.fetch(same(Role.class), eq("role-template-apple-other")))
            .thenReturn(Mono.just(roleOther));
        // correct behavior is to ignore the cycle relation
        List<Role> rolesFromCycle =
            roleService.listDependencies(Set.of("role-template-apple-manage"));
        assertThat(rolesFromCycle).hasSize(3);
    }

    @Nested
    class ListDependenciesTest {
        @Test
        void listDependencies() {
            // prepare test data
            Role role1 = createRole("role1", "role2");
            Role role2 = createRole("role2", "role3");
            Role role3 = createRole("role3");

            Set<String> roleNames = Set.of("role1");

            // setup mocks
            when(extensionClient.fetch(Role.class, "role1")).thenReturn(Mono.just(role1));
            when(extensionClient.fetch(Role.class, "role2")).thenReturn(Mono.just(role2));
            when(extensionClient.fetch(Role.class, "role3")).thenReturn(Mono.just(role3));
            when(extensionClient.list(eq(Role.class), any(), any()))
                .thenReturn(Flux.empty());

            // call the method under test
            Flux<Role> result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(3)).fetch(eq(Role.class), anyString());
        }

        @Test
        void listDependenciesWithCycle() {
            // prepare test data
            Role role1 = createRole("role1", "role2");
            Role role2 = createRole("role2", "role3");
            Role role3 = createRole("role3", "role1");

            Set<String> roleNames = Set.of("role1");

            // setup mocks
            when(extensionClient.fetch(Role.class, "role1")).thenReturn(Mono.just(role1));
            when(extensionClient.fetch(Role.class, "role2")).thenReturn(Mono.just(role2));
            when(extensionClient.fetch(Role.class, "role3")).thenReturn(Mono.just(role3));
            when(extensionClient.list(eq(Role.class), any(), any()))
                .thenReturn(Flux.empty());

            // call the method under test
            Flux<Role> result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(3)).fetch(eq(Role.class), anyString());
        }

        @Test
        void listDependenciesWithMiddleCycle() {
            // prepare test data
            // role1 -> role2 -> role3 -> role4
            //             \<-----|
            Role role1 = createRole("role1", "role2");
            Role role2 = createRole("role2", "role3");
            Role role3 = createRole("role3", "role2", "role4");
            Role role4 = createRole("role4");

            Set<String> roleNames = Set.of("role1");

            // setup mocks
            when(extensionClient.fetch(Role.class, "role1")).thenReturn(Mono.just(role1));
            when(extensionClient.fetch(Role.class, "role2")).thenReturn(Mono.just(role2));
            when(extensionClient.fetch(Role.class, "role3")).thenReturn(Mono.just(role3));
            when(extensionClient.fetch(Role.class, "role4")).thenReturn(Mono.just(role4));
            when(extensionClient.list(eq(Role.class), any(), any()))
                .thenReturn(Flux.empty());

            // call the method under test
            Flux<Role> result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role3)
                .expectNext(role4)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(4)).fetch(eq(Role.class), anyString());
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

            // setup mocks
            when(extensionClient.fetch(Role.class, "role1")).thenReturn(Mono.just(role1));
            when(extensionClient.fetch(Role.class, "role2")).thenReturn(Mono.just(role2));
            when(extensionClient.fetch(Role.class, "role3")).thenReturn(Mono.just(role3));
            when(extensionClient.fetch(Role.class, "role4")).thenReturn(Mono.just(role4));
            when(extensionClient.list(eq(Role.class), any(), any()))
                .thenReturn(Flux.empty());

            // call the method under test
            Flux<Role> result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role4)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(4)).fetch(eq(Role.class), anyString());
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

            // setup mocks
            lenient().when(extensionClient.fetch(Role.class, "role1")).thenReturn(Mono.just(role1));
            when(extensionClient.fetch(Role.class, "role2")).thenReturn(Mono.just(role2));
            when(extensionClient.fetch(Role.class, "role3")).thenReturn(Mono.just(role3));
            lenient().when(extensionClient.fetch(Role.class, "role4")).thenReturn(Mono.just(role4));
            when(extensionClient.list(eq(Role.class), any(), any()))
                .thenReturn(Flux.empty());

            // call the method under test
            Flux<Role> result = roleService.listDependenciesFlux(roleNames);

            // verify the result
            StepVerifier.create(result)
                .expectNext(role2)
                .expectNext(role3)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(2)).fetch(eq(Role.class), anyString());
        }

        @Test
        void listDependenciesWithNullParam() {
            Flux<Role> result = roleService.listDependenciesFlux(null);
            // verify the result
            StepVerifier.create(result)
                .verifyComplete();

            result = roleService.listDependenciesFlux(Set.of());
            StepVerifier.create(result)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(0)).fetch(eq(Role.class), anyString());
        }

        @Test
        void listDependenciesAndSomeOneNotFound() {
            Role role1 = createRole("role1", "role2");
            Role role2 = createRole("role2", "role3", "role4");
            Role role4 = createRole("role4");

            Set<String> roleNames = Set.of("role1");

            // setup mocks
            when(extensionClient.fetch(Role.class, "role1")).thenReturn(Mono.just(role1));
            when(extensionClient.fetch(Role.class, "role2")).thenReturn(Mono.just(role2));
            when(extensionClient.fetch(Role.class, "role3")).thenReturn(Mono.empty());
            when(extensionClient.fetch(Role.class, "role4")).thenReturn(Mono.just(role4));
            when(extensionClient.list(eq(Role.class), any(), any()))
                .thenReturn(Flux.empty());

            Flux<Role> result = roleService.listDependenciesFlux(roleNames);
            // verify the result
            StepVerifier.create(result)
                .expectNext(role1)
                .expectNext(role2)
                .expectNext(role4)
                .verifyComplete();

            // verify the mock invocations
            verify(extensionClient, times(4)).fetch(eq(Role.class), anyString());
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
