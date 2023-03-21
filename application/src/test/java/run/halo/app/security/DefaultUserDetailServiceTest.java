package run.halo.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.GroupVersionKind.fromExtension;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class DefaultUserDetailServiceTest {

    @Mock
    UserService userService;

    @Mock
    RoleService roleService;

    @InjectMocks
    DefaultUserDetailService userDetailService;

    @Test
    void shouldUpdatePasswordSuccessfully() {
        var fakeUser = createFakeUserDetails();

        var user = new run.halo.app.core.extension.User();

        when(userService.updatePassword("faker", "new-fake-password")).thenReturn(
            Mono.just(user)
        );

        var userDetailsMono = userDetailService.updatePassword(fakeUser, "new-fake-password");

        StepVerifier.create(userDetailsMono)
            .expectSubscription()
            .assertNext(userDetails -> assertEquals("new-fake-password", userDetails.getPassword()))
            .verifyComplete();

        verify(userService, times(1)).updatePassword(eq("faker"), eq("new-fake-password"));
    }

    @Test
    void shouldReturnErrorWhenFailedToUpdatePassword() {
        var fakeUser = createFakeUserDetails();

        var exception = new RuntimeException("failed to update password");
        when(userService.updatePassword("faker", "new-fake-password")).thenReturn(
            Mono.error(exception)
        );

        var userDetailsMono = userDetailService.updatePassword(fakeUser, "new-fake-password");

        StepVerifier.create(userDetailsMono)
            .expectSubscription()
            .expectErrorMatches(throwable -> throwable == exception)
            .verify();
        verify(userService, times(1)).updatePassword(eq("faker"), eq("new-fake-password"));
    }

    @Test
    void shouldFindUserDetailsByExistingUsername() {
        var foundUser = createFakeUser();

        var roleGvk = new Role().groupVersionKind();
        var roleRef = new RoleRef();
        roleRef.setKind(roleGvk.kind());
        roleRef.setApiGroup(roleGvk.group());
        roleRef.setName("fake-role");

        var userGvk = foundUser.groupVersionKind();
        var subject = new Subject(userGvk.kind(), "faker", userGvk.group());

        when(userService.getUser("faker")).thenReturn(Mono.just(foundUser));
        when(roleService.listRoleRefs(subject)).thenReturn(Flux.just(roleRef));

        var userDetailsMono = userDetailService.findByUsername("faker");

        StepVerifier.create(userDetailsMono)
            .expectSubscription()
            .assertNext(gotUser -> {
                assertEquals(foundUser.getMetadata().getName(), gotUser.getUsername());
                assertEquals(foundUser.getSpec().getPassword(), gotUser.getPassword());
                assertEquals(List.of("ROLE_fake-role"),
                    gotUser.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
            })
            .verifyComplete();
    }

    @Test
    void shouldFindUserDetailsByExistingUsernameButKindOfRoleRefIsNotRole() {
        var foundUser = createFakeUser();

        var roleGvk = new Role().groupVersionKind();
        var roleRef = new RoleRef();
        roleRef.setKind("FakeRole");
        roleRef.setApiGroup("fake.halo.run");
        roleRef.setName("fake-role");

        var userGvk = foundUser.groupVersionKind();
        var subject = new Subject(userGvk.kind(), "faker", userGvk.group());

        when(userService.getUser("faker")).thenReturn(Mono.just(foundUser));
        when(roleService.listRoleRefs(subject)).thenReturn(Flux.just(roleRef));

        var userDetailsMono = userDetailService.findByUsername("faker");

        StepVerifier.create(userDetailsMono)
            .expectSubscription()
            .assertNext(gotUser -> {
                assertEquals(foundUser.getMetadata().getName(), gotUser.getUsername());
                assertEquals(foundUser.getSpec().getPassword(), gotUser.getPassword());
                assertEquals(0, gotUser.getAuthorities().size());
            })
            .verifyComplete();
    }

    @Test
    void shouldFindUserDetailsByExistingUsernameButWithoutAnyRoles() {
        var foundUser = createFakeUser();

        var userGvk = foundUser.groupVersionKind();
        var subject = new Subject(userGvk.kind(), "faker", userGvk.group());

        when(userService.getUser("faker")).thenReturn(Mono.just(foundUser));
        when(roleService.listRoleRefs(subject)).thenReturn(Flux.empty());

        var userDetailsMono = userDetailService.findByUsername("faker");

        StepVerifier.create(userDetailsMono)
            .expectSubscription()
            .assertNext(gotUser -> {
                assertEquals(foundUser.getMetadata().getName(), gotUser.getUsername());
                assertEquals(foundUser.getSpec().getPassword(), gotUser.getPassword());
                assertEquals(0, gotUser.getAuthorities().size());
            })
            .verifyComplete();
    }

    @Test
    void shouldNotFindUserDetailsByNonExistingUsername() {
        when(userService.getUser("non-existing-user")).thenReturn(
            Mono.error(() -> new ExtensionNotFoundException(
                fromExtension(run.halo.app.core.extension.User.class), "non-existing-user")));

        var userDetailsMono = userDetailService.findByUsername("non-existing-user");

        StepVerifier.create(userDetailsMono)
            .expectError(AuthenticationException.class)
            .verify();
    }

    UserDetails createFakeUserDetails() {
        return User.builder()
            .username("faker")
            .password("fake-password")
            .roles("fake-role")
            .build();
    }

    run.halo.app.core.extension.User createFakeUser() {
        var metadata = new Metadata();
        metadata.setName("faker");

        var userSpec = new run.halo.app.core.extension.User.UserSpec();
        userSpec.setPassword("fake-password");

        var user = new run.halo.app.core.extension.User();
        user.setMetadata(metadata);
        user.setSpec(userSpec);
        return user;

    }
}