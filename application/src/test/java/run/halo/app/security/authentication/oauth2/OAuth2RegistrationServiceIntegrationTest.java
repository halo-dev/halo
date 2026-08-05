package run.halo.app.security.authentication.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static run.halo.app.extension.ExtensionUtil.defaultSort;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.reconciler.UserReconciler;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
                        .flatMap(result -> Mono.zip(
                                        client.get(User.class, result.username()),
                                        connectionService.getByProviderUserId("github", "oauth-no-email-registration"),
                                        roleService
                                                .getRolesByUsername(result.username())
                                                .collectList())
                                .doOnNext(persisted -> {
                                    var user = persisted.getT1();
                                    var connection = persisted.getT2();
                                    var roles = persisted.getT3();
                                    assertThat(result.needsEmailCompletion()).isTrue();
                                    assertThat(user.getSpec().getEmail()).isNull();
                                    assertThat(user.getSpec().getPassword()).isNull();
                                    assertThat(user.getSpec().isEmailVerified()).isFalse();
                                    assertThat(connection.getSpec().getUsername())
                                            .isEqualTo(result.username());
                                    assertThat(roles).contains("guest");
                                })
                                .thenReturn(result)))
                .assertNext(result -> assertThat(result.username()).isEqualTo("oauth-no-email-registration"))
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
}
