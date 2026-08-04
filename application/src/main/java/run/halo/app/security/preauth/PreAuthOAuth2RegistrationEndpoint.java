package run.halo.app.security.preauth;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationService;

/** Pre-auth endpoints for OAuth2 selection page and registration. */
@Component
@Slf4j
class PreAuthOAuth2RegistrationEndpoint {

    private final OAuth2RegistrationService registrationService;
    private final OAuth2AuthenticationTokenCache tokenCache;
    private final ServerSecurityContextRepository securityContextRepository;
    private final ReactiveUserDetailsService userDetailsService;
    private final LoginHandlerEnhancer loginHandlerEnhancer;
    private final ServerRequestCache requestCache;
    private final GlobalInfoService globalInfoService;
    private final AuthProviderService authProviderService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final ReactiveExtensionClient extensionClient;

    public PreAuthOAuth2RegistrationEndpoint(
            OAuth2RegistrationService registrationService,
            OAuth2AuthenticationTokenCache tokenCache,
            ServerSecurityContextRepository securityContextRepository,
            ReactiveUserDetailsService userDetailsService,
            LoginHandlerEnhancer loginHandlerEnhancer,
            ServerRequestCache requestCache,
            GlobalInfoService globalInfoService,
            AuthProviderService authProviderService,
            SystemConfigFetcher systemConfigFetcher,
            ReactiveExtensionClient extensionClient) {
        this.registrationService = registrationService;
        this.tokenCache = tokenCache;
        this.securityContextRepository = securityContextRepository;
        this.userDetailsService = userDetailsService;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.requestCache = requestCache;
        this.globalInfoService = globalInfoService;
        this.authProviderService = authProviderService;
        this.systemConfigFetcher = systemConfigFetcher;
        this.extensionClient = extensionClient;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 99)
    RouterFunction<ServerResponse> preAuthOAuth2RegistrationEndpoints() {
        return RouterFunctions.nest(
                path("/login"),
                RouterFunctions.route()
                        .GET("", oauth2SelectParam(), this::renderSelectPage)
                        .POST("/oauth2/register", this::register)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private static RequestPredicate oauth2SelectParam() {
        return request -> request.queryParam("oauth2_select").isPresent();
    }

    private Mono<ServerResponse> renderSelectPage(ServerRequest request) {
        var exchange = request.exchange();
        return tokenCache
                .getToken(exchange)
                .flatMap(token -> {
                    var registrationId = token.getAuthorizedClientRegistrationId();
                    var providerMono = authProviderService
                            .getEnabledProviders()
                            .filter(ap -> Objects.equals(
                                    registrationId, ap.getMetadata().getName()))
                            .next()
                            .defaultIfEmpty(new AuthProvider());
                    var userSettingMono = systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class);
                    return Mono.zip(providerMono, userSettingMono).flatMap(tuple -> {
                        var model = new HashMap<String, Object>();
                        model.put(
                                "globalInfo", globalInfoService.getGlobalInfo().cache());
                        model.put("authProvider", tuple.getT1());
                        model.put("allowRegistration", tuple.getT2().isAllowRegistration());
                        model.put("agreementPages", fetchAgreementPages().cache());
                        return ServerResponse.ok().render("login_oauth2_select", model);
                    });
                })
                .switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/login"))
                        .build()));
    }

    private Mono<ServerResponse> register(ServerRequest request) {
        var exchange = request.exchange();
        var agreedToTermsMono = request.formData()
                .map(form -> Boolean.parseBoolean(form.getFirst("agreedToTerms")))
                .defaultIfEmpty(false);
        return Mono.zip(tokenCache.getToken(exchange), agreedToTermsMono)
                .flatMap(tuple -> registrationService
                        .register(tuple.getT1(), tuple.getT2())
                        .flatMap(result -> authenticate(exchange, result.username(), tuple.getT1())
                                .thenReturn(result))
                        .flatMap(result -> redirectAfterRegister(exchange, result)))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/login"))
                        .build()))
                .doOnError(e -> log.warn("Registration failed for OAuth2 user", e))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/login?oauth2_select&error=registration-failed"))
                        .build());
    }

    private Mono<Void> authenticate(ServerWebExchange exchange, String username, OAuth2AuthenticationToken token) {
        return userDetailsService
                .findByUsername(username)
                .map(userDetails -> HaloOAuth2AuthenticationToken.authenticated(userDetails, token))
                .flatMap(haloToken -> {
                    var securityContext = new SecurityContextImpl(haloToken);
                    return securityContextRepository
                            .save(exchange, securityContext)
                            .then(loginHandlerEnhancer.onLoginSuccess(exchange, haloToken));
                });
    }

    private Mono<ServerResponse> redirectAfterRegister(
            ServerWebExchange exchange, OAuth2RegistrationService.RegistrationResult result) {
        if (result.needsEmailCompletion()) {
            return ServerResponse.status(HttpStatus.FOUND)
                    .location(URI.create("/complete-profile"))
                    .build();
        }
        return requestCache
                .getRedirectUri(exchange)
                .defaultIfEmpty(URI.create("/uc"))
                .flatMap(uri ->
                        ServerResponse.status(HttpStatus.FOUND).location(uri).build());
    }

    private Mono<List<Map<String, String>>> fetchAgreementPages() {
        return Optional.ofNullable(systemConfigFetcher)
                .map(fetcher -> fetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .flatMapMany(userSetting -> {
                            var pages = userSetting.getRequiredAgreementPages();
                            if (CollectionUtils.isEmpty(pages)) {
                                return Flux.empty();
                            }
                            return Flux.fromIterable(pages);
                        })
                        .flatMap(pageName -> extensionClient
                                .fetch(SinglePage.class, pageName)
                                .map(page -> {
                                    Map<String, String> map = new HashMap<>();
                                    map.put("title", page.getSpec().getTitle());
                                    var status = page.getStatus();
                                    if (status != null) {
                                        map.put("permalink", status.getPermalink());
                                    }
                                    return map;
                                })
                                .onErrorResume(e -> Mono.empty()))
                        .collectList())
                .orElseGet(() -> Mono.just(List.of()));
    }
}
