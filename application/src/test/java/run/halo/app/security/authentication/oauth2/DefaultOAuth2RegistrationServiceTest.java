package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AgreementNotAcceptedException;

@ExtendWith(MockitoExtension.class)
class DefaultOAuth2RegistrationServiceTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    UserService userService;

    @Mock
    UserConnectionService connectionService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    jakarta.validation.Validator validator;

    @Mock
    Clock clock;

    DefaultOAuth2RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultOAuth2RegistrationService(
                client, userService, connectionService, systemConfigFetcher, validator, clock);
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-08-04T00:00:00Z"));
        lenient().when(validator.validate(any())).thenReturn(Set.of());
        lenient().when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
    }

    SystemSetting.User userSetting() {
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setDefaultRole("author");
        return setting;
    }

    OAuth2AuthenticationToken token(String name, Map<String, Object> attributes) {
        var user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_authenticated")), attributes, "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "github");
    }

    AtomicReference<User> stubSuccessfulRegistration(
            SystemSetting.User setting, String providerUserId, String expectedUsername) {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(connectionService.getByProviderUserId("github", providerUserId)).thenReturn(Mono.empty());
        when(client.fetch(User.class, expectedUsername)).thenReturn(Mono.empty());
        var created = new AtomicReference<User>();
        when(userService.createUser(any(User.class), eq(Set.of(setting.getDefaultRole()))))
                .thenAnswer(invocation -> {
                    created.set(invocation.getArgument(0));
                    return Mono.just(created.get());
                });
        when(userService.getUser(expectedUsername)).thenAnswer(invocation -> Mono.just(created.get()));
        when(connectionService.createUserConnection(eq(expectedUsername), eq("github"), any()))
                .thenReturn(Mono.just(new UserConnection()));
        return created;
    }

    static Stream<Arguments> remainingUsernameAttributes() {
        return Stream.of(
                Arguments.of("username", "UsernameCandidate", "usernamecandidate"),
                Arguments.of("user_name", "user-name-candidate", "user-name-candidate"),
                Arguments.of("nickname", "NickName", "nickname"));
    }

    static Stream<String> validBoundaryUsernames() {
        return Stream.of("abcd", "a" + "b".repeat(62));
    }

    @Test
    void shouldRegisterWithOAuth2NameAndVerifiedEmail() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("alice@example.com")).thenReturn(Mono.just(false));

        var created = new User();
        created.setMetadata(new Metadata());
        created.getMetadata().setName("alice");
        when(userService.createUser(any(User.class), eq(Set.of("author")))).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, User.class);
            created.setSpec(user.getSpec());
            return Mono.just(created);
        });
        when(userService.getUser("alice")).thenReturn(Mono.just(created));
        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        when(connectionService.createUserConnection(eq("alice"), eq("github"), any()))
                .thenReturn(Mono.just(connection));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com", "name", "Alice"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();

        verify(userService).createUser(any(User.class), eq(Set.of("author")));
        assertThat(created.getSpec().getEmail()).isEqualTo("alice@example.com");
        assertThat(created.getSpec().isEmailVerified()).isTrue();
        assertThat(created.getSpec().getDisplayName()).isEqualTo("Alice");
        assertThat(created.getSpec().getRegisteredAt()).isEqualTo(Instant.parse("2026-08-04T00:00:00Z"));
    }

    @Test
    void shouldPreferLoginAttributeOverGetName() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        // getName() returns the "sub" attribute, but "login" should win as the username candidate
        when(connectionService.getByProviderUserId("github", "sub-123")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("alice@example.com")).thenReturn(Mono.just(false));
        when(userService.createUser(any(User.class), anySet()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userService.getUser("alice")).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName("alice");
            var userSpec = new User.UserSpec();
            userSpec.setEmail("alice@example.com");
            userSpec.setEmailVerified(true);
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var token = token("alice", Map.of("sub", "sub-123", "login", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), anySet());
        assertThat(captor.getValue().getMetadata().getName()).isEqualTo("alice");
    }

    @ParameterizedTest
    @MethodSource("remainingUsernameAttributes")
    void shouldUseRemainingCommonUsernameAttributes(String attribute, String value, String expectedUsername) {
        var providerUserId = "sub-" + attribute.replace('_', '-');
        var created = stubSuccessfulRegistration(userSetting(), providerUserId, expectedUsername);
        var attributes = new java.util.HashMap<String, Object>();
        attributes.put("sub", providerUserId);
        attributes.put(attribute, value);

        StepVerifier.create(service.register(token(providerUserId, attributes), false))
                .assertNext(result -> assertThat(result.username()).isEqualTo(expectedUsername))
                .verifyComplete();

        assertThat(created.get().getMetadata().getName()).isEqualTo(expectedUsername);
    }

    @ParameterizedTest
    @MethodSource("validBoundaryUsernames")
    void shouldAcceptUsernameLengthBoundaries(String username) {
        stubSuccessfulRegistration(userSetting(), username, username);

        StepVerifier.create(service.register(token(username, Map.of("sub", username)), false))
                .assertNext(result -> assertThat(result.username()).isEqualTo(username))
                .verifyComplete();
    }

    @Test
    void shouldPreferPreferredUsernameForOidcUser() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "sub-123")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("user@example.com")).thenReturn(Mono.just(false));
        when(userService.createUser(any(User.class), anySet()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userService.getUser("alice")).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName("alice");
            var userSpec = new User.UserSpec();
            userSpec.setEmail("user@example.com");
            userSpec.setEmailVerified(true);
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("sub-123");
        when(oidcUser.getPreferredUsername()).thenReturn("alice");
        when(oidcUser.getClaimAsString("email")).thenReturn("user@example.com");
        when(oidcUser.getClaimAsBoolean("email_verified")).thenReturn(true);
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "github");

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), anySet());
        assertThat(captor.getValue().getMetadata().getName()).isEqualTo("alice");
    }

    @Test
    void shouldUseRandomUsernameWhenCandidateIsTaken() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.just(new User()));
        when(client.fetch(eq(User.class), org.mockito.ArgumentMatchers.argThat(name -> !"alice".equals(name))))
                .thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        when(userService.createUser(any(User.class), anySet()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userService.getUser(anyString())).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName(invocation.getArgument(0));
            var userSpec = new User.UserSpec();
            userSpec.setEmail("alice@example.com");
            userSpec.setEmailVerified(true);
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).matches("user-[a-z0-9]{8}"))
                .verifyComplete();
    }

    @Test
    void shouldUseRandomUsernameWhenCandidateIsProtected() {
        var setting = userSetting();
        setting.setProtectedUsernames("alice");
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(eq(User.class), anyString())).thenReturn(Mono.empty());
        var created = new AtomicReference<User>();
        when(userService.createUser(any(User.class), anySet())).thenAnswer(invocation -> {
            created.set(invocation.getArgument(0));
            return Mono.just(created.get());
        });
        when(userService.getUser(anyString())).thenAnswer(invocation -> Mono.just(created.get()));
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        StepVerifier.create(service.register(token("alice", Map.of("sub", "alice")), false))
                .assertNext(result -> assertThat(result.username()).matches("user-[a-z0-9]{8}"))
                .verifyComplete();
    }

    @Test
    void shouldFailAfterTwentyRandomUsernameCollisions() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(eq(User.class), anyString())).thenReturn(Mono.just(new User()));

        StepVerifier.create(service.register(token("alice", Map.of("sub", "alice")), false))
                .expectError(ServerWebInputException.class)
                .verify();

        verify(client, org.mockito.Mockito.times(21)).fetch(eq(User.class), anyString());
        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldUseDisplayNameAttribute() {
        var displayNameUser = stubSuccessfulRegistration(userSetting(), "display-sub", "alice");
        StepVerifier.create(service.register(
                        token(
                                "display-sub",
                                Map.of("sub", "display-sub", "login", "alice", "display_name", "Alice Display")),
                        false))
                .expectNextCount(1)
                .verifyComplete();
        assertThat(displayNameUser.get().getSpec().getDisplayName()).isEqualTo("Alice Display");
    }

    @Test
    void shouldFallbackToUsernameWhenDisplayNameIsProtected() {
        var protectedSetting = userSetting();
        protectedSetting.setProtectedUsernames("admin");
        var protectedDisplayNameUser = stubSuccessfulRegistration(protectedSetting, "protected-sub", "bobby");
        StepVerifier.create(service.register(
                        token("protected-sub", Map.of("sub", "protected-sub", "login", "bobby", "name", "Admin")),
                        false))
                .expectNextCount(1)
                .verifyComplete();
        assertThat(protectedDisplayNameUser.get().getSpec().getDisplayName()).isEqualTo("bobby");
    }

    @Test
    void shouldLeaveEmailBlankWhenEmailIsTaken() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("alice@example.com")).thenReturn(Mono.just(true));
        when(userService.createUser(any(User.class), anySet()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userService.getUser(anyString())).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName(invocation.getArgument(0));
            var userSpec = new User.UserSpec();
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), anySet());
        assertThat(captor.getValue().getSpec().getEmail()).isNull();
        assertThat(captor.getValue().getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldKeepEmailUnverifiedForOidcUserWhenEmailVerifiedIsFalse() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "sub-123")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "sub-123")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified("user@example.com")).thenReturn(Mono.just(false));
        when(userService.createUser(any(User.class), anySet()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userService.getUser("sub-123")).thenAnswer(invocation -> {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName("sub-123");
            var userSpec = new User.UserSpec();
            userSpec.setEmail("user@example.com");
            userSpec.setEmailVerified(false);
            user.setSpec(userSpec);
            return Mono.just(user);
        });
        when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenReturn(Mono.just(new UserConnection()));

        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("sub-123");
        when(oidcUser.getClaimAsString("email")).thenReturn("user@example.com");
        when(oidcUser.getClaimAsBoolean("email_verified")).thenReturn(false);
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "github");

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("sub-123"))
                .verifyComplete();

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture(), anySet());
        assertThat(captor.getValue().getSpec().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldLeaveEmailBlankWhenOidcEmailClaimIsMissing() {
        var created = stubSuccessfulRegistration(userSetting(), "sub-123", "sub-123");
        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("sub-123");
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "github");

        StepVerifier.create(service.register(token, false)).expectNextCount(1).verifyComplete();

        assertThat(created.get().getSpec().getEmail()).isNull();
        assertThat(created.get().getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldRejectWhenRegistrationDisabled() {
        var setting = userSetting();
        setting.setAllowRegistration(false);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        var token = token("alice", Map.of("sub", "alice"));

        StepVerifier.create(service.register(token, false))
                .expectError(ServerWebInputException.class)
                .verify();
        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldRejectWhenAgreementNotAccepted() {
        var setting = userSetting();
        setting.setRequiredAgreementPages(List.of("page"));
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        var token = token("alice", Map.of("sub", "alice"));

        StepVerifier.create(service.register(token, false))
                .expectError(AgreementNotAcceptedException.class)
                .verify();
    }

    @Test
    void shouldReturnExistingUsernameWhenConnectionExists() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        var spec = new UserConnection.UserConnectionSpec();
        spec.setUsername("alice");
        connection.setSpec(spec);
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.just(connection));
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("alice");
        var userSpec = new User.UserSpec();
        userSpec.setEmail("alice@example.com");
        userSpec.setEmailVerified(true);
        user.setSpec(userSpec);
        when(userService.getUser("alice")).thenReturn(Mono.just(user));

        var token = token("alice", Map.of("sub", "alice"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();
        verify(userService, never()).createUser(any(), anySet());
    }

    @Test
    void shouldDeleteUserWhenConnectionCreationFails() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        when(connectionService.getByProviderUserId("github", "alice")).thenReturn(Mono.empty());
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        var created = new User();
        created.setMetadata(new Metadata());
        created.getMetadata().setName("alice");
        when(userService.createUser(any(User.class), anySet())).thenReturn(Mono.just(created));
        when(connectionService.createUserConnection(eq("alice"), eq("github"), any()))
                .thenReturn(Mono.error(new IllegalStateException("already bound")));
        when(client.delete(created)).thenReturn(Mono.just(created));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .expectError(IllegalStateException.class)
                .verify();
        verify(client).delete(created);
    }

    @Test
    void shouldLoginConcurrentWinnerWhenConnectionCreationLosesRace() {
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(userSetting()));
        var winnerConnection = new UserConnection();
        winnerConnection.setMetadata(new Metadata());
        var connectionSpec = new UserConnection.UserConnectionSpec();
        connectionSpec.setUsername("winner");
        winnerConnection.setSpec(connectionSpec);
        when(connectionService.getByProviderUserId("github", "alice"))
                .thenReturn(Mono.empty(), Mono.just(winnerConnection));
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty());
        when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        var created = new User();
        created.setMetadata(new Metadata());
        created.getMetadata().setName("alice");
        when(userService.createUser(any(User.class), anySet())).thenReturn(Mono.just(created));
        when(connectionService.createUserConnection(eq("alice"), eq("github"), any()))
                .thenReturn(Mono.error(new IllegalStateException("already bound")));
        when(client.delete(created)).thenReturn(Mono.just(created));
        var winner = new User();
        winner.setMetadata(new Metadata());
        winner.getMetadata().setName("winner");
        var winnerSpec = new User.UserSpec();
        winnerSpec.setEmailVerified(true);
        winner.setSpec(winnerSpec);
        when(userService.getUser("winner")).thenReturn(Mono.just(winner));

        var token = token("alice", Map.of("sub", "alice", "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("winner"))
                .verifyComplete();
        verify(client).delete(created);
    }
}
