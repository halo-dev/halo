package run.halo.app.security.preauth;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;
import static run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken.authenticated;

import java.net.URI;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.OAuth2RegisterData;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.exception.EmailAlreadyTakenException;
import run.halo.app.infra.exception.OAuth2UserAlreadyBoundException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;

/**
 * Pre-auth endpoints for unbound OAuth2 users: a choice page (bind existing account or register a new one) and a
 * register page that creates a new account and binds it to the OAuth2 provider.
 *
 * <p>All endpoints read the cached OAuth2 token from the session, so refreshing the page keeps the OAuth2 login state.
 * If no token is cached, the user is redirected to the login page.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Slf4j
@Component
class PreAuthOAuth2Endpoint {

    private final OAuth2AuthenticationTokenCache authenticationCache;

    private final UserService userService;

    private final UserConnectionService connectionService;

    private final ReactiveUserDetailsService userDetailsService;

    private final ServerSecurityContextRepository securityContextRepository;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final SystemConfigFetcher systemConfigFetcher;

    private final Validator validator;

    private final AgreementPageFetcher agreementPageFetcher;

    private Clock clock = Clock.systemDefaultZone();

    PreAuthOAuth2Endpoint(
            OAuth2AuthenticationTokenCache authenticationCache,
            UserService userService,
            UserConnectionService connectionService,
            ReactiveUserDetailsService userDetailsService,
            ServerSecurityContextRepository securityContextRepository,
            LoginHandlerEnhancer loginHandlerEnhancer,
            SystemConfigFetcher systemConfigFetcher,
            Validator validator,
            AgreementPageFetcher agreementPageFetcher) {
        this.authenticationCache = authenticationCache;
        this.userService = userService;
        this.connectionService = connectionService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.systemConfigFetcher = systemConfigFetcher;
        this.validator = validator;
        this.agreementPageFetcher = agreementPageFetcher;
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> preAuthOAuth2Endpoints() {
        return RouterFunctions.nest(
                path("/login/oauth2"),
                RouterFunctions.route()
                        .GET("", this::choicePage)
                        .GET("/register", this::registerPage)
                        .POST("/register", contentType(APPLICATION_FORM_URLENCODED), this::register)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private Mono<ServerResponse> choicePage(ServerRequest request) {
        var exchange = request.exchange();
        return authenticationCache
                .getToken(exchange)
                .flatMap(token -> {
                    var attrs = token.getPrincipal().getAttributes();
                    var model = new HashMap<String, Object>();
                    model.put("registrationId", token.getAuthorizedClientRegistrationId());
                    model.put("displayName", extractDisplayName(attrs));
                    model.put("email", extractEmail(attrs));
                    return systemConfigFetcher
                            .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                            .map(SystemSetting.User::isAllowRegistration)
                            .defaultIfEmpty(false)
                            .doOnNext(allowRegistration -> model.put("allowRegistration", allowRegistration))
                            .thenReturn(model)
                            .flatMap(m -> ServerResponse.ok().render("oauth2_choice", m));
                })
                .switchIfEmpty(Mono.defer(() -> redirectToLogin(exchange)));
    }

    private Mono<ServerResponse> registerPage(ServerRequest request) {
        var exchange = request.exchange();
        return authenticationCache
                .getToken(exchange)
                .flatMap(token -> {
                    var attrs = token.getPrincipal().getAttributes();
                    var data = new OAuth2RegisterData();
                    data.setUsername(deriveUsername(attrs));
                    data.setDisplayName(extractDisplayName(attrs));
                    data.setEmail(extractEmail(attrs));
                    var bindingResult = new BeanPropertyBindingResult(data, "form");
                    var model = bindingResult.getModel();
                    model.put("registrationId", token.getAuthorizedClientRegistrationId());
                    model.put("displayName", data.getDisplayName());
                    model.put("email", data.getEmail());
                    model.put("agreementPages", agreementPageFetcher.fetchAgreementPages());
                    return ServerResponse.ok().render("oauth2_register", model);
                })
                .switchIfEmpty(Mono.defer(() -> redirectToLogin(exchange)));
    }

    private Mono<ServerResponse> register(ServerRequest request) {
        var exchange = request.exchange();
        return authenticationCache
                .getToken(exchange)
                .flatMap(token ->
                        request.bind(OAuth2RegisterData.class).flatMap(data -> registerUser(exchange, token, data)))
                .switchIfEmpty(Mono.defer(() -> redirectToLogin(exchange)));
    }

    private Mono<ServerResponse> registerUser(
            ServerWebExchange exchange, OAuth2AuthenticationToken token, OAuth2RegisterData data) {
        var bindingResult = validate(data, validator, exchange);
        var model = bindingResult.getModel();
        model.put("registrationId", token.getAuthorizedClientRegistrationId());
        model.put("displayName", data.getDisplayName());
        model.put("email", data.getEmail());
        var registrationId = token.getAuthorizedClientRegistrationId();
        var oauth2User = token.getPrincipal();
        return agreementPageFetcher.fetchAgreementPages().flatMap(agreementPages -> {
            model.put("agreementPages", agreementPages);
            if (bindingResult.hasErrors()) {
                return ServerResponse.ok().render("oauth2_register", model);
            }
            var username = data.getUsername().trim();
            var displayName = data.getDisplayName().trim();
            data.setUsername(username);
            data.setDisplayName(displayName);
            return systemConfigFetcher
                    .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                    .defaultIfEmpty(new SystemSetting.User())
                    .flatMap(setting -> {
                        if (!setting.isAllowRegistration()) {
                            model.put("error", "registration-disallowed");
                            return ServerResponse.ok().render("oauth2_register", model);
                        }
                        if (!SystemSetting.User.isUsernameAllowed(setting, username)) {
                            model.put("error", "restricted-username");
                            return ServerResponse.ok().render("oauth2_register", model);
                        }
                        if (!SystemSetting.User.isDisplayNameAllowed(setting, displayName)) {
                            model.put("error", "restricted-display-name");
                            return ServerResponse.ok().render("oauth2_register", model);
                        }
                        if (!StringUtils.hasText(setting.getDefaultRole())) {
                            model.put("error", "no-default-role");
                            return ServerResponse.ok().render("oauth2_register", model);
                        }
                        if (!CollectionUtils.isEmpty(agreementPages) && !Boolean.TRUE.equals(data.getAgreedToTerms())) {
                            bindingResult.addError(new FieldError(
                                    "form",
                                    "agreedToTerms",
                                    data.getAgreedToTerms(),
                                    true,
                                    new String[] {"oauth2.register.error.agreed-to-terms.required"},
                                    null,
                                    "Please agree to the terms"));
                            return ServerResponse.ok().render("oauth2_register", model);
                        }
                        return createUserAndConnection(exchange, token, setting, data)
                                .then(ServerResponse.status(HttpStatus.FOUND)
                                        .location(URI.create("/"))
                                        .build())
                                .onErrorResume(e -> renderRegisterError(model, bindingResult, data, e));
                    });
        });
    }

    private Mono<Void> createUserAndConnection(
            ServerWebExchange exchange,
            OAuth2AuthenticationToken token,
            SystemSetting.User setting,
            OAuth2RegisterData data) {
        var registrationId = token.getAuthorizedClientRegistrationId();
        var oauth2User = token.getPrincipal();
        var email =
                Optional.ofNullable(data.getEmail()).map(String::toLowerCase).orElse(null);
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(data.getUsername());
        user.setSpec(new User.UserSpec());
        var spec = user.getSpec();
        spec.setPassword("");
        spec.setDisplayName(data.getDisplayName());
        spec.setEmail(email);
        spec.setEmailVerified(true);
        spec.setRegisteredAt(clock.instant());

        Mono<Void> emailCheck;
        if (StringUtils.hasText(email)) {
            emailCheck = userService
                    .findUserByVerifiedEmail(email)
                    .hasElement()
                    .filter(has -> !has)
                    .switchIfEmpty(Mono.error(() -> new EmailAlreadyTakenException("Email already taken")))
                    .then();
        } else {
            emailCheck = Mono.empty();
        }
        return connectionService
                .getByProviderUserId(registrationId, oauth2User.getName())
                .flatMap(connection -> Mono.<Void>error(() -> new OAuth2UserAlreadyBoundException(connection)))
                .switchIfEmpty(Mono.defer(() -> emailCheck
                        .then(Mono.defer(() -> userService.createUser(user, Set.of(setting.getDefaultRole()))))
                        .flatMap(created -> connectionService.createUserConnection(
                                created.getMetadata().getName(), registrationId, oauth2User))
                        .flatMap(connection -> userDetailsService.findByUsername(
                                connection.getSpec().getUsername()))
                        .map(userDetails -> authenticated(userDetails, token))
                        .flatMap(haloOAuth2Token -> {
                            var securityContext = new SecurityContextImpl(haloOAuth2Token);
                            return securityContextRepository
                                    .save(exchange, securityContext)
                                    .then(loginHandlerEnhancer.onLoginSuccess(exchange, haloOAuth2Token))
                                    .then(authenticationCache.removeToken(exchange));
                        })))
                .then();
    }

    private Mono<ServerResponse> renderRegisterError(
            Map<String, Object> model, BindingResult bindingResult, OAuth2RegisterData data, Throwable error) {
        if (error instanceof DuplicateNameException) {
            bindingResult.addError(new FieldError(
                    "form",
                    "username",
                    data.getUsername(),
                    true,
                    new String[] {"oauth2.register.error.duplicate-username"},
                    null,
                    "Username already taken"));
        } else if (error instanceof EmailAlreadyTakenException) {
            bindingResult.addError(new FieldError(
                    "form",
                    "email",
                    data.getEmail(),
                    true,
                    new String[] {"oauth2.register.error.email-already-taken"},
                    null,
                    "Email already taken"));
        } else if (error instanceof OAuth2UserAlreadyBoundException) {
            model.put("error", "oauth2-already-bound");
        } else {
            log.warn("Failed to register OAuth2 user", error);
            model.put("error", "unknown");
        }
        return ServerResponse.ok().render("oauth2_register", model);
    }

    private Mono<ServerResponse> redirectToLogin(ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create("/login"))
                .build();
    }

    private static String deriveUsername(Map<String, Object> attrs) {
        for (var key : List.of("login", "preferred_username", "name")) {
            var value = attrs.get(key);
            if (value instanceof String s && StringUtils.hasText(s)) {
                var candidate = s.trim().toLowerCase();
                if (candidate.length() >= 4
                        && candidate.length() <= 63
                        && ValidationUtils.NAME_PATTERN.matcher(candidate).matches()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static String extractDisplayName(Map<String, Object> attrs) {
        for (var key : List.of("name", "login", "preferred_username")) {
            var value = attrs.get(key);
            if (value instanceof String s && StringUtils.hasText(s)) {
                return s;
            }
        }
        return null;
    }

    private static String extractEmail(Map<String, Object> attrs) {
        var value = attrs.get("email");
        if (value instanceof String s && StringUtils.hasText(s)) {
            return s.toLowerCase();
        }
        return null;
    }
}
