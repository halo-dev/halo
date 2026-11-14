package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.notDeleting;
import static run.halo.app.extension.index.query.Queries.equal;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.reconciler.UserReconciler;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserPostCreatingHandler;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(OAuth2RegistrationServiceIntegrationTest.FailureConfiguration.class)
class OAuth2RegistrationServiceIntegrationTest {

    @Autowired
    OAuth2RegistrationService registrationService;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoSpyBean
    UserConnectionService connectionService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserReconciler userReconciler;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @Autowired
    FailingPostCreatingHandler failingPostCreatingHandler;

    @Test
    void shouldPersistUserWithoutEmailDefaultRoleAndConnection() {
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setMustVerifyEmailOnRegistration(true);
        setting.setDefaultRole("guest");
        doReturn(Mono.just(setting))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        var oauth2User = new DefaultOAuth2User(List.of(), Map.of("sub", "oauth-no-email-registration"), "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");

        StepVerifier.create(registrationService
                        .register(token, false)
                        .flatMap(username -> Mono.zip(
                                        client.get(User.class, username),
                                        connectionService.getByProviderUserId("github", "oauth-no-email-registration"),
                                        roleService.getRolesByUsername(username).collectList())
                                .doOnNext(persisted -> {
                                    var user = persisted.getT1();
                                    var connection = persisted.getT2();
                                    var roles = persisted.getT3();
                                    assertThat(user.getSpec().getEmail()).isNull();
                                    assertThat(user.getSpec().getPassword()).isNull();
                                    assertThat(user.getSpec().isEmailVerified()).isFalse();
                                    assertThat(connection.getSpec().getUsername())
                                            .isEqualTo(username);
                                    assertThat(roles).contains("guest");
                                })
                                .thenReturn(username)))
                .expectNext("oauth-no-email-registration")
                .verifyComplete();
    }

    @Test
    void shouldRemoveUserAndRoleBindingSubjectAfterConnectionFailureCompensation() {
        var username = "oauth-compensation-user";
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setDefaultRole("guest");
        doReturn(Mono.just(setting))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        var oauth2User = new DefaultOAuth2User(List.of(), Map.of("sub", username), "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github");
        var original = new IllegalStateException("connection creation failed");
        doAnswer(ignored -> Mono.zip(
                                client.get(User.class, username),
                                roleService.getRolesByUsername(username).collectList())
                        .doOnNext(persisted -> {
                            assertThat(persisted.getT1().getMetadata().getFinalizers())
                                    .contains("user-protection");
                            assertThat(persisted.getT2()).contains("guest");
                        })
                        .then(Mono.<UserConnection>error(original)))
                .when(connectionService)
                .createUserConnection(eq(username), eq("github"), any());

        StepVerifier.create(registrationService.register(token, false))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(original))
                .verify();

        var request = new Reconciler.Request(username);
        assertThatThrownBy(() -> userReconciler.reconcile(request)).isInstanceOf(RequeueException.class);
        userReconciler.reconcile(request);

        StepVerifier.create(awaitCompensation(username)).verifyComplete();
    }

    @Test
    void shouldAtomicallyResolveConcurrentRegistrationForSameProviderIdentity() {
        var setting = registrationSetting();
        doReturn(Mono.just(setting))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        var oauth2User =
                new DefaultOAuth2User(List.of(), Map.of("sub", "x", "name", "Concurrent OAuth Identity"), "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "concurrent-provider");

        StepVerifier.create(Mono.zip(
                                registrationService.register(token, false), registrationService.register(token, false))
                        .flatMap(results -> {
                            assertThat(results.getT1()).isEqualTo(results.getT2());
                            return assertSingleSurvivingRegistration("concurrent-provider", "x", results.getT1());
                        }))
                .verifyComplete();
    }

    @Test
    void shouldCompensateUserPersistedBeforePostCreatingHandlerFailure() {
        var username = "oauth-post-handler-failure";
        var failure = new IllegalStateException("post creating failed");
        failingPostCreatingHandler.fail(username, failure);
        doReturn(Mono.just(registrationSetting()))
                .when(systemConfigFetcher)
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        var oauth2User = new DefaultOAuth2User(List.of(), Map.of("sub", username), "sub");
        var token = new OAuth2AuthenticationToken(oauth2User, List.of(), "github-post-failure");

        StepVerifier.create(registrationService.register(token, false))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
                .verify();

        var request = new Reconciler.Request(username);
        assertThatThrownBy(() -> userReconciler.reconcile(request)).isInstanceOf(RequeueException.class);
        userReconciler.reconcile(request);
        StepVerifier.create(awaitCompensation(username)).verifyComplete();
        StepVerifier.create(connectionService.getByProviderUserId("github-post-failure", username))
                .verifyComplete();
    }

    private Mono<Void> assertSingleSurvivingRegistration(
            String registrationId, String providerUserId, String winnerUsername) {
        var connections = ListOptions.builder()
                .andQuery(equal("spec.registrationId", registrationId))
                .andQuery(equal("spec.providerUserId", providerUserId))
                .andQuery(notDeleting())
                .build();
        return Mono.defer(() -> client.listAll(User.class, new ListOptions(), defaultSort())
                        .filter(user -> "Concurrent OAuth Identity"
                                .equals(user.getSpec().getDisplayName()))
                        .collectList()
                        .publishOn(Schedulers.boundedElastic())
                        .doOnNext(this::reconcileDeletedUsers)
                        .flatMap(candidateUsers -> {
                            var candidateNames = candidateUsers.stream()
                                    .map(user -> user.getMetadata().getName())
                                    .collect(Collectors.toSet());
                            return Mono.zip(
                                    Mono.just(candidateUsers.stream()
                                            .filter(user -> !isDeleted(user))
                                            .toList()),
                                    client.listAll(UserConnection.class, connections, defaultSort())
                                            .collectList(),
                                    client.listAll(RoleBinding.class, new ListOptions(), defaultSort())
                                            .filter(binding -> containsAnyUser(binding, candidateNames))
                                            .collectList());
                        })
                        .flatMap(state -> {
                            if (state.getT1().size() != 1
                                    || state.getT2().size() != 1
                                    || state.getT3().size() != 1) {
                                return Mono.<Void>error(new IllegalStateException(
                                        "Concurrent registration compensation has not completed: users="
                                                + state.getT1().size()
                                                + ", connections="
                                                + state.getT2().size()
                                                + ", roleBindings="
                                                + state.getT3().size()));
                            }
                            assertThat(state.getT1())
                                    .singleElement()
                                    .satisfies(user -> assertThat(
                                                    user.getMetadata().getName())
                                            .isEqualTo(winnerUsername));
                            assertThat(state.getT2())
                                    .singleElement()
                                    .satisfies(connection -> assertThat(
                                                    connection.getSpec().getUsername())
                                            .isEqualTo(winnerUsername));
                            assertThat(state.getT3())
                                    .singleElement()
                                    .satisfies(binding -> assertThat(binding.getSubjects())
                                            .anyMatch(RoleBinding.Subject.isUser(winnerUsername)));
                            return Mono.<Void>empty();
                        }))
                .retryWhen(Retry.backoff(50, Duration.ofMillis(20)).maxBackoff(Duration.ofMillis(200)));
    }

    private void reconcileDeletedUsers(List<User> candidateUsers) {
        candidateUsers.stream().filter(user -> isDeleted(user)).forEach(user -> {
            var request = new Reconciler.Request(user.getMetadata().getName());
            try {
                userReconciler.reconcile(request);
            } catch (RequeueException ignored) {
                userReconciler.reconcile(request);
            }
        });
    }

    private static boolean containsAnyUser(RoleBinding binding, Set<String> usernames) {
        return binding.getSubjects() != null
                && binding.getSubjects().stream()
                        .anyMatch(subject -> usernames.contains(subject.getName())
                                && RoleBinding.Subject.isUser(subject.getName()).test(subject));
    }

    private static SystemSetting.User registrationSetting() {
        var setting = new SystemSetting.User();
        setting.setAllowRegistration(true);
        setting.setDefaultRole("guest");
        return setting;
    }

    private Mono<Void> awaitCompensation(String username) {
        return Mono.defer(() -> Mono.zip(
                                client.fetch(User.class, username).hasElement(),
                                client.listAll(RoleBinding.class, new ListOptions(), defaultSort())
                                        .filter(binding -> binding.getSubjects() != null
                                                && binding.getSubjects().stream()
                                                        .anyMatch(RoleBinding.Subject.isUser(username)))
                                        .hasElements())
                        .flatMap(state -> {
                            if (!state.getT1() && !state.getT2()) {
                                return Mono.<Void>empty();
                            }
                            return Mono.<Void>error(new IllegalStateException("Compensation has not completed."));
                        }))
                .retryWhen(Retry.backoff(50, Duration.ofMillis(20)).maxBackoff(Duration.ofMillis(200)));
    }

    @TestConfiguration
    static class FailureConfiguration {

        @Bean
        FailingPostCreatingHandler failingPostCreatingHandler() {
            return new FailingPostCreatingHandler();
        }
    }

    static class FailingPostCreatingHandler implements UserPostCreatingHandler {

        private final AtomicReference<Failure> failure = new AtomicReference<>();

        void fail(String username, RuntimeException error) {
            failure.set(new Failure(username, error));
        }

        @Override
        public Mono<Void> postCreating(User user) {
            var configured = failure.get();
            if (configured != null
                    && configured.username().equals(user.getMetadata().getName())) {
                return Mono.error(configured.error());
            }
            return Mono.empty();
        }

        private record Failure(String username, RuntimeException error) {}
    }
}
