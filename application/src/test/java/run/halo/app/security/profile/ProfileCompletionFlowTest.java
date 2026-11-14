package run.halo.app.security.profile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

class ProfileCompletionFlowTest {

    private static final String USERNAME = "alice";

    private final UserService userService = mock(UserService.class);
    private final SystemConfigFetcher systemConfigFetcher = mock(SystemConfigFetcher.class);
    private final ServerRequestCache requestCache = mock(ServerRequestCache.class);

    @Test
    void shouldReturnFirstRequiredStepInProviderOrder() {
        var emailStep = new ProfileCompletionStep(
                URI.create("/complete-profile"), URI.create("email-not-set"), "A verified email address is required.");
        var phoneStep = new ProfileCompletionStep(
                URI.create("/complete-profile/phone"),
                URI.create("phone-not-set"),
                "A verified phone number is required.");
        var flow = flow(username -> Mono.just(emailStep), username -> Mono.just(phoneStep));

        StepVerifier.create(flow.findNext(USERNAME)).expectNext(emailStep).verifyComplete();
    }

    @Test
    void shouldSkipDisabledEmailRequirementBeforeLoadingUser() {
        var flow = flow(new EmailProfileCompletionRequirement(systemConfigFetcher, userService));
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(false);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(userService.getUser(USERNAME))
                .thenReturn(Mono.error(new AssertionError("Disabled requirements must not load the user")));

        StepVerifier.create(flow.findNext(USERNAME)).verifyComplete();

        verify(userService, never()).getUser(USERNAME);
    }

    @Test
    void shouldUseRequiredStepBeforeSavedRequest() {
        var phoneStep = new ProfileCompletionStep(
                URI.create("/complete-profile/phone"),
                URI.create("phone-not-set"),
                "A verified phone number is required.");
        var flow = flow(username -> Mono.just(phoneStep));
        var exchange = exchange();

        StepVerifier.create(flow.getRedirectUri(USERNAME, exchange))
                .expectNext(phoneStep.location())
                .verifyComplete();

        verify(requestCache, never()).getRedirectUri(exchange);
    }

    @Test
    void shouldUseSavedRequestWhenProfileIsComplete() {
        var flow = flow();
        var exchange = exchange();
        when(requestCache.getRedirectUri(exchange)).thenReturn(Mono.just(URI.create("/dashboard")));

        StepVerifier.create(flow.getRedirectUri(USERNAME, exchange))
                .expectNext(URI.create("/dashboard"))
                .verifyComplete();
    }

    @Test
    void shouldDefaultToUserCenterWhenProfileIsCompleteAndRequestIsNotSaved() {
        var flow = flow();
        var exchange = exchange();
        when(requestCache.getRedirectUri(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(flow.getRedirectUri(USERNAME, exchange))
                .expectNext(URI.create("/uc"))
                .verifyComplete();
    }

    @Test
    void emailRequirementShouldRequireVerifiedEmailWhenPolicyIsEnabled() {
        var user = new User();
        user.getSpec().setEmailVerified(false);
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);

        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user));
        var requirement = new EmailProfileCompletionRequirement(systemConfigFetcher, userService);

        StepVerifier.create(requirement.evaluate(USERNAME))
                .expectNext(new ProfileCompletionStep(
                        URI.create("/complete-profile"),
                        URI.create("email-not-set"),
                        "A verified email address is required."))
                .verifyComplete();
    }

    @Test
    void emailRequirementShouldRejectVerifiedFlagWithoutEmail() {
        var user = new User();
        user.getSpec().setEmailVerified(true);
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user));

        var requirement = new EmailProfileCompletionRequirement(systemConfigFetcher, userService);

        StepVerifier.create(requirement.evaluate(USERNAME))
                .expectNext(new ProfileCompletionStep(
                        URI.create("/complete-profile"),
                        URI.create("email-not-set"),
                        "A verified email address is required."))
                .verifyComplete();
    }

    @Test
    void emailRequirementShouldBeCompleteWhenEmailIsVerifiedOrPolicyIsDisabled() {
        var requirement = new EmailProfileCompletionRequirement(systemConfigFetcher, userService);
        var user = new User();
        var setting = new SystemSetting.User();

        user.getSpec().setEmail("alice@example.com");
        user.getSpec().setEmailVerified(true);
        setting.setMustVerifyEmailOnRegistration(true);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user));
        StepVerifier.create(requirement.evaluate(USERNAME)).verifyComplete();

        user.getSpec().setEmailVerified(false);
        setting.setMustVerifyEmailOnRegistration(false);
        StepVerifier.create(requirement.evaluate(USERNAME)).verifyComplete();
    }

    private static MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/original"));
    }

    @SafeVarargs
    private ProfileCompletionFlow flow(ProfileCompletionRequirement... requirements) {
        @SuppressWarnings("unchecked")
        var provider = (ObjectProvider<ProfileCompletionRequirement>) mock(ObjectProvider.class);
        when(provider.orderedStream()).thenReturn(Stream.of(requirements));
        return new ProfileCompletionFlow(requestCache, provider);
    }
}
