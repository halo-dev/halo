package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.security.profile.ProfileCompletionFlow;
import run.halo.app.security.profile.ProfileCompletionStep;

class ProfileCompletionFilterTest {

    private static final String USERNAME = "alice";
    private static final ProfileCompletionStep PHONE_STEP = new ProfileCompletionStep(
            URI.create("/complete-profile/phone"), URI.create("phone-not-set"), "A verified phone number is required.");

    private final ProfileCompletionFlow profileCompletionFlow = mock(ProfileCompletionFlow.class);
    private final ServerRequestCache requestCache = mock(ServerRequestCache.class);
    private final WebFilterChain chain = mock(WebFilterChain.class);
    private final ProfileCompletionFilter filter =
            new ProfileCompletionFilter(profileCompletionFlow, requestCache, responseContext());

    @Test
    void shouldRedirectToRequiredCompletionStep() {
        var exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/uc").accept(MediaType.TEXT_HTML));
        when(profileCompletionFlow.findNext(USERNAME)).thenReturn(Mono.just(PHONE_STEP));
        when(requestCache.saveRequest(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticatedUser()))
                .as(StepVerifier::create)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(PHONE_STEP.location());
        verify(requestCache).saveRequest(exchange);
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldPassWhenAuthenticationIsMissing() {
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));

        pass(exchange, null);

        verifyNoInteractions(profileCompletionFlow, requestCache);
    }

    @Test
    void shouldPassAnonymousAuthentication() {
        var authentication = new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_anonymous")));
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));

        pass(exchange, authentication);

        verifyNoInteractions(profileCompletionFlow, requestCache);
    }

    @Test
    void shouldPassWhenNoCompletionStepIsRequired() {
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));
        when(profileCompletionFlow.findNext(USERNAME)).thenReturn(Mono.empty());

        pass(exchange, authenticatedUser());

        verifyNoInteractions(requestCache);
    }

    @Test
    void shouldPassSuperRoleWithoutEvaluatingRequirements() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                USERNAME, "password", List.of(new SimpleGrantedAuthority("ROLE_super-role")));
        var exchange = exchange(MockServerHttpRequest.get("/archives").accept(MediaType.TEXT_HTML));

        pass(exchange, authentication);

        verifyNoInteractions(profileCompletionFlow, requestCache);
    }

    @Test
    void shouldNotResolveRawOAuth2SubjectAsHaloUsername() {
        var principal = new DefaultOAuth2User(List.of(), Map.of("sub", USERNAME), "sub");
        var authentication = new OAuth2AuthenticationToken(principal, List.of(), "github");
        var exchange = exchange(MockServerHttpRequest.get("/uc").accept(MediaType.TEXT_HTML));

        pass(exchange, authentication);

        verifyNoInteractions(profileCompletionFlow, requestCache);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/oauth2/authorization/github",
                "/login/oauth2/register",
                "/signup",
                "/password-reset/email",
                "/password-reset/email/reset-token",
                "/logout",
                "/logout/impersonate",
                "/complete-profile",
                "/complete-profile/send-email-code",
                "/complete-profile/phone",
                "/complete-profile/phone/send-code",
                "/system/setup",
                "/favicon.ico"
            })
    void shouldPassExemptPath(String path) {
        var exchange = exchange(MockServerHttpRequest.get(path).accept(MediaType.TEXT_HTML));

        pass(exchange, authenticatedUser());

        verifyNoInteractions(profileCompletionFlow, requestCache);
    }

    @Test
    void shouldPassPasswordResetPost() {
        var exchange = exchange(
                MockServerHttpRequest.post("/password-reset/email/reset-token").accept(MediaType.TEXT_HTML));

        pass(exchange, authenticatedUser());

        verifyNoInteractions(profileCompletionFlow, requestCache);
    }

    @Test
    void shouldReturnStepSpecificProblemDetailForApiRequest() {
        var exchange = exchange(MockServerHttpRequest.get("/apis/api.console.halo.run/v1alpha1/users")
                .accept(MediaType.APPLICATION_JSON));
        when(profileCompletionFlow.findNext(USERNAME)).thenReturn(Mono.just(PHONE_STEP));

        var body = filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticatedUser()))
                .then(Mono.defer(exchange.getResponse()::getBodyAsString))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(body)
                .contains("\"type\":\"phone-not-set\"")
                .contains("\"detail\":\"A verified phone number is required.\"");
        verify(chain, never()).filter(any());
        verifyNoInteractions(requestCache);
    }

    @Test
    void shouldLeaveStaticResourceExclusionToSecurityChain() {
        var exchange = exchange(MockServerHttpRequest.method(HttpMethod.GET, "/ui-assets/app.js")
                .accept(MediaType.APPLICATION_JSON));
        when(profileCompletionFlow.findNext(USERNAME)).thenReturn(Mono.just(PHONE_STEP));

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticatedUser()))
                .as(StepVerifier::create)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldInstallFilterAfterAnonymousAuthentication() {
        var http = mock(ServerHttpSecurity.class);

        new ProfileCompletionSecurityConfigurer(profileCompletionFlow, requestCache, responseContext()).configure(http);

        verify(http)
                .addFilterAfter(
                        any(ProfileCompletionFilter.class), eq(SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION));
    }

    @Test
    void shouldNotRegisterFilterAsGlobalWebFilter() {
        assertThat(AnnotatedElementUtils.hasAnnotation(ProfileCompletionFilter.class, Component.class))
                .isFalse();
    }

    private void pass(MockServerWebExchange exchange, Authentication authentication) {
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        var result = filter.filter(exchange, chain);
        if (authentication != null) {
            result = result.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        result.as(StepVerifier::create).verifyComplete();
        verify(chain).filter(exchange);
    }

    private static MockServerWebExchange exchange(MockServerHttpRequest.BaseBuilder<?> request) {
        return MockServerWebExchange.from(request);
    }

    private static UsernamePasswordAuthenticationToken authenticatedUser() {
        return UsernamePasswordAuthenticationToken.authenticated(
                USERNAME, "password", List.of(new SimpleGrantedAuthority("ROLE_authenticated")));
    }

    private static ServerResponse.Context responseContext() {
        var strategies = HandlerStrategies.withDefaults();
        return new ServerResponse.Context() {
            @Override
            public List<org.springframework.http.codec.HttpMessageWriter<?>> messageWriters() {
                return strategies.messageWriters();
            }

            @Override
            public List<org.springframework.web.reactive.result.view.ViewResolver> viewResolvers() {
                return strategies.viewResolvers();
            }
        };
    }
}
