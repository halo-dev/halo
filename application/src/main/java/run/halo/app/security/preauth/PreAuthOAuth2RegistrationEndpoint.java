package run.halo.app.security.preauth;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.security.authentication.oauth2.OAuth2IdentityFingerprint.PARAMETER_NAME;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AgreementNotAcceptedException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationSession;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2BindIntent;
import run.halo.app.security.authentication.oauth2.OAuth2IdentityFingerprint;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationException;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;
import run.halo.app.security.authorization.AuthorityUtils;
import run.halo.app.security.profile.ProfileCompletionFlow;

@Component
@RequiredArgsConstructor
class PreAuthOAuth2RegistrationEndpoint {

    private static final String LOGIN_PATH = "/login";

    private static final String FLOW_EXPIRED_REDIRECT = LOGIN_PATH + "?error=oauth2-flow-expired";

    private final OAuth2AuthenticationTokenCache tokenCache;

    private final OAuth2BindIntent bindIntent;

    private final AuthProviderService authProviderService;

    private final SystemConfigFetcher systemConfigFetcher;

    private final AgreementPageFetcher agreementPageFetcher;

    private final OAuth2RegistrationService registrationService;

    private final OAuth2AuthenticationSession authenticationSession;

    private final ProfileCompletionFlow profileCompletionFlow;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 99)
    RouterFunction<ServerResponse> preAuthOAuth2RegistrationEndpoints() {
        return RouterFunctions.nest(
                path("/login"),
                RouterFunctions.route()
                        .GET("", oauth2SelectParam(), this::renderSelectPage)
                        .POST("/oauth2/register", contentType(APPLICATION_FORM_URLENCODED), this::register)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private static RequestPredicate oauth2SelectParam() {
        return request -> request.queryParam("oauth2_select").isPresent();
    }

    private Mono<ServerResponse> renderSelectPage(ServerRequest request) {
        return tokenCache
                .getToken(request.exchange())
                .flatMap(token -> bindIntent
                        .clear(request.exchange())
                        .then(selectionModel(token))
                        .flatMap(model -> ServerResponse.ok().render("login_oauth2_select", model)))
                .switchIfEmpty(redirect(LOGIN_PATH));
    }

    private Mono<ServerResponse> register(ServerRequest request) {
        return tokenCache
                .getToken(request.exchange())
                .flatMap(token -> request.formData().flatMap(formData -> {
                    var submittedIdentity = formData.getFirst(PARAMETER_NAME);
                    if (!Objects.equals(submittedIdentity, OAuth2IdentityFingerprint.from(token))) {
                        return clearOAuth2State(request).then(redirect(FLOW_EXPIRED_REDIRECT));
                    }
                    var agreedToTerms = Boolean.parseBoolean(formData.getFirst("agreedToTerms"));
                    return selectionModel(token)
                            .flatMap(model -> registrationService
                                    .register(token, agreedToTerms)
                                    .map(username -> Mono.defer(() -> authenticationSession
                                            .establishAndGetAuthentication(request.exchange(), username, token)
                                            .flatMap(authentication -> {
                                                var roles = AuthorityUtils.authoritiesToRoles(
                                                        authentication.getAuthorities());
                                                var redirectUri = AuthorityUtils.containsSuperRole(roles)
                                                        ? profileCompletionFlow.getRedirectUriAfterCompletion(
                                                                request.exchange())
                                                        : profileCompletionFlow.getRedirectUri(
                                                                username, request.exchange());
                                                return redirectUri.flatMap(uri -> redirect(uri.toString()));
                                            })))
                                    .onErrorResume(error -> Mono.just(renderRegistrationError(error, model)))
                                    .flatMap(response -> response));
                }))
                .switchIfEmpty(redirect(LOGIN_PATH));
    }

    private Mono<Map<String, Object>> selectionModel(OAuth2AuthenticationToken token) {
        var provider = authProviderService
                .getEnabledProviders()
                .filter(candidate ->
                        Objects.equals(candidate.getMetadata().getName(), token.getAuthorizedClientRegistrationId()))
                .next();
        var userSetting = systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
        var agreementPages = agreementPageFetcher.fetchAgreementPages();
        return Mono.zip(provider, userSetting, agreementPages).map(tuple -> {
            Map<String, Object> model = new HashMap<>();
            model.put("provider", tuple.getT1());
            model.put(PARAMETER_NAME, OAuth2IdentityFingerprint.from(token));
            model.put("allowRegistration", tuple.getT2().isAllowRegistration());
            model.put("agreementPages", tuple.getT3());
            return model;
        });
    }

    private Mono<Void> clearOAuth2State(ServerRequest request) {
        return Mono.when(tokenCache.removeToken(request.exchange()), bindIntent.clear(request.exchange()));
    }

    private Mono<ServerResponse> renderRegistrationError(Throwable error, Map<String, Object> model) {
        model.put("error", registrationError(error));
        return ServerResponse.ok().render("login_oauth2_select", model);
    }

    private static String registrationError(Throwable error) {
        if (error instanceof AgreementNotAcceptedException) {
            return "agreement-not-accepted";
        }
        if (error instanceof OAuth2RegistrationException registrationException) {
            return registrationException.getErrorCode();
        }
        return "registration-failed";
    }

    private static Mono<ServerResponse> redirect(String location) {
        return ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }
}
