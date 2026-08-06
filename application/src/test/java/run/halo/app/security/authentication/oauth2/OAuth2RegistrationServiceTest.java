package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class OAuth2RegistrationServiceTest {

    private static final Instant REGISTERED_AT = Instant.parse("2026-08-05T00:00:00Z");

    static ValidatorFactory validatorFactory;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    UserService userService;

    @Mock
    UserConnectionService connectionService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    final Map<String, User> users = new ConcurrentHashMap<>();

    final Map<String, UserConnection> connections = new ConcurrentHashMap<>();

    SystemSetting.User setting;

    OAuth2RegistrationService service;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setDefaultRole("guest");

        lenient()
                .when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenAnswer(ignored -> Mono.just(setting));
        lenient()
                .when(client.fetch(eq(User.class), anyString()))
                .thenAnswer(invocation -> Mono.justOrEmpty(users.get(invocation.getArgument(1, String.class))));
        lenient()
                .when(userService.getUser(anyString()))
                .thenAnswer(invocation -> Mono.justOrEmpty(users.get(invocation.getArgument(0, String.class))));
        lenient().when(userService.checkEmailAlreadyVerified(anyString())).thenReturn(Mono.just(false));
        lenient()
                .when(userService.createUser(any(User.class), any()))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    var user = invocation.getArgument(0, User.class);
                    users.put(user.getMetadata().getName(), user);
                    return user;
                }));
        lenient()
                .when(connectionService.getByProviderUserId(anyString(), anyString()))
                .thenAnswer(invocation -> Mono.defer(() -> Mono.justOrEmpty(connections.get(connectionKey(
                        invocation.getArgument(0, String.class), invocation.getArgument(1, String.class))))));
        lenient()
                .when(connectionService.createUserConnection(anyString(), anyString(), any()))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    var username = invocation.getArgument(0, String.class);
                    var registrationId = invocation.getArgument(1, String.class);
                    var oauth2User =
                            invocation.getArgument(2, org.springframework.security.oauth2.core.user.OAuth2User.class);
                    var connection = connection(username);
                    connections.put(connectionKey(registrationId, oauth2User.getName()), connection);
                    return connection;
                }));
        lenient()
                .when(client.delete(any(User.class)))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    var user = invocation.getArgument(0, User.class);
                    users.remove(user.getMetadata().getName());
                    return user;
                }));
        lenient()
                .when(client.update(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, User.class)));

        service = new OAuth2RegistrationService(
                client, userService, connectionService, systemConfigFetcher, validatorFactory.getValidator());
        service.setClock(Clock.fixed(REGISTERED_AT, ZoneOffset.UTC));
    }

    static Stream<Arguments> commonUsernameAttributes() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "sub", "provider-user-id",
                                "login", " LoginUser ",
                                "username", "wrong-username",
                                "user_name", "wrong-user-name",
                                "nickname", "wrong-nickname"),
                        "loginuser"),
                Arguments.of(
                        Map.of(
                                "sub", "provider-user-id",
                                "username", " UsernameUser ",
                                "user_name", "wrong-user-name",
                                "nickname", "wrong-nickname"),
                        "usernameuser"),
                Arguments.of(
                        Map.of(
                                "sub", "provider-user-id",
                                "user_name", " User-Name-User ",
                                "nickname", "wrong-nickname"),
                        "user-name-user"),
                Arguments.of(Map.of("sub", "provider-user-id", "nickname", " NickNameUser "), "nicknameuser"),
                Arguments.of(Map.of("sub", "Provider-User-ID"), "provider-user-id"));
    }

    @ParameterizedTest
    @MethodSource("commonUsernameAttributes")
    void shouldResolveCommonUsernameAttributesInPrecedenceOrder(
            Map<String, Object> attributes, String expectedUsername) {
        var token = token(attributes);

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo(expectedUsername))
                .verifyComplete();

        assertThat(users).containsKey(expectedUsername);
    }

    @Test
    void shouldPreferOidcPreferredUsernameBeforeNicknameAndName() {
        var oidcUser = org.mockito.Mockito.mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("provider-user-id");
        when(oidcUser.getPreferredUsername()).thenReturn(" PreferredUser ");
        lenient().when(oidcUser.getAttribute("nickname")).thenReturn("wrong-nickname");
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "github");

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> assertThat(result.username()).isEqualTo("preferreduser"))
                .verifyComplete();

        assertThat(users.get("preferreduser").getSpec().getDisplayName()).isEqualTo("wrong-nickname");
    }

    @Test
    void shouldUseOidcPreferredUsernameAsDisplayNameAfterCommonAttributes() {
        var oidcUser = org.mockito.Mockito.mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("provider-user-id");
        when(oidcUser.getPreferredUsername()).thenReturn(" Preferred User ");
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "github");

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> {
                    assertThat(result.username()).matches("user-[a-z0-9]{8}");
                    assertThat(users.get(result.username()).getSpec().getDisplayName())
                            .isEqualTo("Preferred User");
                })
                .verifyComplete();
    }

    static Stream<String> validBoundaryUsernames() {
        return Stream.of("AbCd", "a" + "B".repeat(62));
    }

    @ParameterizedTest
    @MethodSource("validBoundaryUsernames")
    void shouldNormalizeAndAcceptUsernameLengthBoundaries(String candidate) {
        StepVerifier.create(service.register(token(Map.of("sub", candidate)), false))
                .assertNext(result -> assertThat(result.username()).isEqualTo(candidate.toLowerCase()))
                .verifyComplete();
    }

    static Stream<String> invalidUsernames() {
        return Stream.of("abc", "a".repeat(64), "invalid_user");
    }

    @ParameterizedTest
    @MethodSource("invalidUsernames")
    void shouldUseRandomUsernameWhenCandidateFailsValidation(String candidate) {
        StepVerifier.create(service.register(token(Map.of("sub", candidate)), false))
                .assertNext(result -> assertThat(result.username()).matches("user-[a-z0-9]{8}"))
                .verifyComplete();
    }

    @Test
    void shouldUseRandomUsernameWhenCandidateIsProtected() {
        setting.setProtectedUsernames("admin, Alice , root");

        StepVerifier.create(service.register(token(Map.of("sub", "provider-user-id", "login", "ALICE")), false))
                .assertNext(result -> assertThat(result.username()).matches("user-[a-z0-9]{8}"))
                .verifyComplete();
    }

    @Test
    void shouldUseRandomUsernameWhenCandidateIsOccupied() {
        users.put("alice", user("alice", true));

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .assertNext(result -> assertThat(result.username()).matches("user-[a-z0-9]{8}"))
                .verifyComplete();
    }

    @Test
    void shouldFailAfterTwentyRandomUsernameCollisionsWithoutCreatingUser() {
        when(client.fetch(eq(User.class), anyString())).thenReturn(Mono.just(user("occupied", true)));

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ServerWebInputException.class);
                    assertThat(error.getMessage()).contains("Failed to generate a unique username.");
                })
                .verify();

        verify(client, times(21)).fetch(eq(User.class), anyString());
        verify(userService, never()).createUser(any(), any());
    }

    static Stream<Arguments> displayNameAttributes() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "sub", "provider-user-id",
                                "login", "alice",
                                "display_name", " Display Name ",
                                "name", " Full Name ",
                                "nickname", "Wrong Nickname"),
                        "Full Name"),
                Arguments.of(
                        Map.of(
                                "sub", "provider-user-id",
                                "login", "alice",
                                "display_name", "Wrong Display Name",
                                "nickname", " Nick Name "),
                        "Nick Name"),
                Arguments.of(
                        Map.of("sub", "provider-user-id", "login", "alice", "display_name", " Display Name "),
                        "Display Name"),
                Arguments.of(Map.of("sub", "provider-user-id", "login", "alice"), "alice"));
    }

    @ParameterizedTest
    @MethodSource("displayNameAttributes")
    void shouldResolveDisplayNameInPrecedenceOrder(Map<String, Object> attributes, String expectedDisplayName) {
        StepVerifier.create(service.register(token(attributes), false))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(users.get("alice").getSpec().getDisplayName()).isEqualTo(expectedDisplayName);
    }

    @Test
    void shouldFallbackToUsernameWhenDisplayNameIsProtected() {
        setting.setProtectedUsernames("Admin, root");

        StepVerifier.create(service.register(
                        token(Map.of("sub", "provider-user-id", "login", "alice", "display_name", " ADMIN ")), false))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(users.get("alice").getSpec().getDisplayName()).isEqualTo("alice");
    }

    @Test
    void shouldNormalizeAndVerifyValidPlainOAuth2Email() {
        StepVerifier.create(service.register(token(Map.of("sub", "alice", "email", "Alice@Example.COM")), false))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(users.get("alice").getSpec().getEmail()).isEqualTo("alice@example.com");
        assertThat(users.get("alice").getSpec().isEmailVerified()).isTrue();
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void shouldFollowOidcEmailVerifiedClaim(boolean emailVerified) {
        var oidcUser = org.mockito.Mockito.mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("oidc-user");
        when(oidcUser.getEmail()).thenReturn("User@Example.COM");
        when(oidcUser.getEmailVerified()).thenReturn(emailVerified);
        var token = new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc");

        StepVerifier.create(service.register(token, false)).expectNextCount(1).verifyComplete();

        assertThat(users.get("oidc-user").getSpec().getEmail()).isEqualTo("user@example.com");
        assertThat(users.get("oidc-user").getSpec().isEmailVerified()).isEqualTo(emailVerified);
    }

    @Test
    void shouldDiscardInvalidEmail() {
        StepVerifier.create(service.register(token(Map.of("sub", "alice", "email", "not-an-email")), false))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(users.get("alice").getSpec().getEmail()).isNull();
        assertThat(users.get("alice").getSpec().isEmailVerified()).isFalse();
        verify(userService, never()).checkEmailAlreadyVerified(anyString());
    }

    @Test
    void shouldDiscardEmailOwnedByVerifiedUser() {
        when(userService.checkEmailAlreadyVerified("alice@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(service.register(token(Map.of("sub", "alice", "email", "Alice@Example.COM")), false))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(users.get("alice").getSpec().getEmail()).isNull();
        assertThat(users.get("alice").getSpec().isEmailVerified()).isFalse();
    }

    @Test
    void shouldRejectDisabledRegistrationBeforeMutation() {
        setting.setAllowRegistration(false);

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOfSatisfying(
                                OAuth2RegistrationException.class,
                                exception ->
                                        assertThat(exception.getErrorCode()).isEqualTo("registration-closed")))
                .verify();

        verifyNoMutation();
    }

    @Test
    void shouldRejectMissingDefaultRoleBeforeMutation() {
        setting.setDefaultRole("  ");

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOfSatisfying(
                                OAuth2RegistrationException.class,
                                exception ->
                                        assertThat(exception.getErrorCode()).isEqualTo("default-role-missing")))
                .verify();

        verifyNoMutation();
    }

    @Test
    void shouldRejectUnacceptedRequiredAgreementsBeforeMutation() {
        setting.setRequiredAgreementPages(List.of("terms"));

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .expectError(AgreementNotAcceptedException.class)
                .verify();

        verifyNoMutation();
    }

    @Test
    void shouldAcceptRequiredAgreementsWhenExplicitlyAgreed() {
        setting.setRequiredAgreementPages(List.of("terms"));

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), true))
                .assertNext(result -> assertThat(result.username()).isEqualTo("alice"))
                .verifyComplete();
    }

    @Test
    void shouldReturnExistingConnectionAndComputeCompletionFromPersistedUser() {
        setting.setMustVerifyEmailOnRegistration(true);
        users.put("existing-user", user("existing-user", false));
        connections.put(connectionKey("github", "provider-user-id"), connection("existing-user"));

        StepVerifier.create(service.register(token(Map.of("sub", "provider-user-id", "login", "new-user")), false))
                .assertNext(result -> {
                    assertThat(result.username()).isEqualTo("existing-user");
                    assertThat(result.needsEmailCompletion()).isTrue();
                })
                .verifyComplete();

        verify(userService, never()).createUser(any(), any());
        verify(connectionService, never()).createUserConnection(anyString(), anyString(), any());
    }

    @Test
    void shouldComputeNoCompletionWhenPersistedEmailIsVerified() {
        setting.setMustVerifyEmailOnRegistration(true);

        StepVerifier.create(service.register(token(Map.of("sub", "alice", "email", "alice@example.com")), false))
                .assertNext(result -> assertThat(result.needsEmailCompletion()).isFalse())
                .verifyComplete();
    }

    @Test
    void shouldComputeNoCompletionWhenVerificationPolicyIsDisabled() {
        setting.setMustVerifyEmailOnRegistration(false);

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .assertNext(result -> assertThat(result.needsEmailCompletion()).isFalse())
                .verifyComplete();
    }

    @Test
    void shouldDeleteCreatedUserAndRethrowOriginalConnectionFailureWhenNoWinnerExists() {
        var original = new IllegalStateException("connection collision");
        var token = token(Map.of("sub", "provider-user-id", "login", "alice"));
        when(connectionService.createUserConnection("alice", "github", token.getPrincipal()))
                .thenReturn(Mono.error(original));

        StepVerifier.create(service.register(token, false))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(original))
                .verify();

        assertThat(users).doesNotContainKey("alice");
        var createdCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(createdCaptor.capture(), eq(Set.of("guest")));
        verify(client).delete(createdCaptor.getValue());
    }

    @Test
    void shouldNotDeleteCollidingUserThatDoesNotOwnRegistrationClaim() {
        var original = new IllegalStateException("user creation raced");
        var collidingUser = user("alice", true);
        when(client.fetch(User.class, "alice")).thenReturn(Mono.empty(), Mono.just(collidingUser));
        when(userService.createUser(any(User.class), eq(Set.of("guest")))).thenReturn(Mono.error(original));

        StepVerifier.create(service.register(token(Map.of("sub", "alice")), false))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(original))
                .verify();

        verify(client, never()).delete(collidingUser);
        verify(connectionService, never()).createUserConnection(anyString(), anyString(), any());
    }

    @Test
    void shouldDeleteCreatedUserThenReturnConcurrentWinnerInRequiredOrder() {
        setting.setMustVerifyEmailOnRegistration(true);
        var original = new IllegalStateException("connection collision");
        var oauth2User =
                token(Map.of("sub", "provider-user-id", "login", "alice")).getPrincipal();
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
        var winner = user("winner", true);
        when(connectionService.createUserConnection("alice", "github", oauth2User))
                .thenReturn(Mono.error(original));
        when(client.delete(any(User.class)))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    var createdUser = invocation.getArgument(0, User.class);
                    users.remove(createdUser.getMetadata().getName());
                    users.put("winner", winner);
                    connections.put(connectionKey("github", "provider-user-id"), connection("winner"));
                    return createdUser;
                }));

        StepVerifier.create(service.register(token, false))
                .assertNext(result -> {
                    assertThat(result.username()).isEqualTo("winner");
                    assertThat(result.needsEmailCompletion()).isFalse();
                })
                .verifyComplete();

        var createdCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(createdCaptor.capture(), eq(Set.of("guest")));
        var createdUser = createdCaptor.getValue();
        var inOrder = inOrder(userService, connectionService, client);
        inOrder.verify(connectionService).getByProviderUserId("github", "provider-user-id");
        inOrder.verify(userService).createUser(any(User.class), eq(Set.of("guest")));
        inOrder.verify(connectionService).createUserConnection(eq("alice"), eq("github"), eq(oauth2User));
        inOrder.verify(client).delete(createdUser);
        inOrder.verify(connectionService).getByProviderUserId("github", "provider-user-id");
    }

    @Test
    void shouldCreateUserConnectionWithPersistedRegistrationFields() {
        var token = token(Map.of(
                "sub", "provider-user-id",
                "login", "alice",
                "display_name", "Alice",
                "email", "alice@example.com"));

        StepVerifier.create(service.register(token, false)).expectNextCount(1).verifyComplete();

        var user = users.get("alice");
        assertThat(user.getMetadata().getName()).isEqualTo("alice");
        assertThat(user.getSpec().getDisplayName()).isEqualTo("Alice");
        assertThat(user.getSpec().getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getSpec().isEmailVerified()).isTrue();
        assertThat(user.getSpec().getRegisteredAt()).isEqualTo(REGISTERED_AT);
        assertThat(user.getSpec().getPassword()).isNull();
        verify(userService).createUser(user, Set.of("guest"));
        verify(connectionService).createUserConnection("alice", "github", token.getPrincipal());
    }

    private void verifyNoMutation() {
        verify(userService, never()).createUser(any(), any());
        verify(connectionService, never()).createUserConnection(anyString(), anyString(), any());
        verify(client, never()).delete(any(User.class));
    }

    private OAuth2AuthenticationToken token(Map<String, Object> attributes) {
        var mutableAttributes = new LinkedHashMap<>(attributes);
        mutableAttributes.putIfAbsent("sub", "provider-user-id");
        var oauth2User = new DefaultOAuth2User(List.of(), mutableAttributes, "sub");
        return new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
    }

    private static User user(String username, boolean emailVerified) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(username);
        user.getSpec().setDisplayName(username);
        user.getSpec().setEmailVerified(emailVerified);
        return user;
    }

    private static UserConnection connection(String username) {
        var connection = new UserConnection();
        connection.setMetadata(new Metadata());
        connection.setSpec(new UserConnection.UserConnectionSpec());
        connection.getSpec().setUsername(username);
        return connection;
    }

    private static String connectionKey(String registrationId, String providerUserId) {
        return registrationId + "/" + providerUserId;
    }
}
