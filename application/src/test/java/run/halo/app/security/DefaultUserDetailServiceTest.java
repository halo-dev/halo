package run.halo.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.authorityListToSet;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Role;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.exception.UserNotFoundException;

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

        when(userService.getUser("faker")).thenReturn(Mono.just(foundUser));
        when(roleService.getRolesByUsername("faker")).thenReturn(Flux.just("fake-role"));

        var userDetailsMono = userDetailService.findByUsername("faker");

        StepVerifier.create(userDetailsMono)
            .expectSubscription()
            .assertNext(gotUser -> {
                assertEquals(foundUser.getMetadata().getName(), gotUser.getUsername());
                assertEquals(foundUser.getSpec().getPassword(), gotUser.getPassword());
                assertEquals(
                    Set.of("ROLE_fake-role", "ROLE_authenticated", "ROLE_anonymous"),
                    authorityListToSet(gotUser.getAuthorities()));
            })
            .verifyComplete();
    }

    @Test
    void shouldFindHaloUserDetailsWith2faDisabledWhen2faNotEnabled() {
        var fakeUser = createFakeUser();
        when(userService.getUser("faker")).thenReturn(Mono.just(fakeUser));
        when(roleService.getRolesByUsername("faker")).thenReturn(Flux.empty());
        userDetailService.findByUsername("faker")
            .as(StepVerifier::create)
            .assertNext(userDetails -> {
                assertInstanceOf(HaloUserDetails.class, userDetails);
                assertFalse(((HaloUserDetails) userDetails).isTwoFactorAuthEnabled());
            })
            .verifyComplete();
    }

    @Test
    void shouldFindHaloUserDetailsWith2faDisabledWhen2faEnabledButNoTotpConfigured() {
        var fakeUser = createFakeUser();
        fakeUser.getSpec().setTwoFactorAuthEnabled(true);
        when(userService.getUser("faker")).thenReturn(Mono.just(fakeUser));
        when(roleService.getRolesByUsername("faker")).thenReturn(Flux.empty());
        userDetailService.findByUsername("faker")
            .as(StepVerifier::create)
            .assertNext(userDetails -> {
                assertInstanceOf(HaloUserDetails.class, userDetails);
                assertFalse(((HaloUserDetails) userDetails).isTwoFactorAuthEnabled());
            })
            .verifyComplete();
    }

    @Test
    void shouldFindHaloUserDetailsWith2faEnabledWhen2faEnabledAndTotpConfigured() {
        var fakeUser = createFakeUser();
        fakeUser.getSpec().setTwoFactorAuthEnabled(true);
        fakeUser.getSpec().setTotpEncryptedSecret("fake-totp-encrypted-secret");
        when(userService.getUser("faker")).thenReturn(Mono.just(fakeUser));
        when(roleService.getRolesByUsername("faker")).thenReturn(Flux.empty());
        userDetailService.findByUsername("faker")
            .as(StepVerifier::create)
            .assertNext(userDetails -> {
                assertInstanceOf(HaloUserDetails.class, userDetails);
                assertTrue(((HaloUserDetails) userDetails).isTwoFactorAuthEnabled());
            })
            .verifyComplete();
    }

    @Test
    void shouldFindHaloUserDetailsWith2faDisabledWhen2faDisabledGlobally() {
        userDetailService.setTwoFactorAuthDisabled(true);
        var fakeUser = createFakeUser();
        fakeUser.getSpec().setTwoFactorAuthEnabled(true);
        fakeUser.getSpec().setTotpEncryptedSecret("fake-totp-encrypted-secret");
        when(userService.getUser("faker")).thenReturn(Mono.just(fakeUser));
        when(roleService.getRolesByUsername("faker")).thenReturn(Flux.empty());
        userDetailService.findByUsername("faker")
            .as(StepVerifier::create)
            .assertNext(userDetails -> {
                assertInstanceOf(HaloUserDetails.class, userDetails);
                assertFalse(((HaloUserDetails) userDetails).isTwoFactorAuthEnabled());
            })
            .verifyComplete();
    }

    @Test
    void shouldFindUserDetailsByExistingUsernameButWithoutAnyRoles() {
        var foundUser = createFakeUser();

        when(userService.getUser("faker")).thenReturn(Mono.just(foundUser));
        when(roleService.getRolesByUsername("faker")).thenReturn(Flux.empty());

        StepVerifier.create(userDetailService.findByUsername("faker"))
            .expectSubscription()
            .assertNext(gotUser -> {
                assertEquals(foundUser.getMetadata().getName(), gotUser.getUsername());
                assertEquals(foundUser.getSpec().getPassword(), gotUser.getPassword());
                assertEquals(
                    Set.of("ROLE_anonymous", "ROLE_authenticated"),
                    authorityListToSet(gotUser.getAuthorities()));
            })
            .verifyComplete();
    }

    @Test
    void shouldNotFindUserDetailsByNonExistingUsername() {
        when(userService.getUser("non-existing-user")).thenReturn(
            Mono.error(() -> new UserNotFoundException("non-existing-user")));

        var userDetailsMono = userDetailService.findByUsername("non-existing-user");

        StepVerifier.create(userDetailsMono)
            .expectError(AuthenticationException.class)
            .verify();
    }

    Role createRole(String roleName) {
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName(roleName);
        return role;
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