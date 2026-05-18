package run.halo.app.security.preauth;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.SignUpData;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.*;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;

/**
 * Pre-auth sign up endpoint.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
class PreAuthSignUpEndpoint {

    private final GlobalInfoService globalInfoService;

    private final Validator validator;

    private final UserService userService;

    private final EmailVerificationService emailVerificationService;

    private final RateLimiterRegistry rateLimiterRegistry;

    private final SystemConfigFetcher systemConfigFetcher;

    private final ReactiveExtensionClient client;

    PreAuthSignUpEndpoint(
            GlobalInfoService globalInfoService,
            Validator validator,
            UserService userService,
            EmailVerificationService emailVerificationService,
            RateLimiterRegistry rateLimiterRegistry,
            SystemConfigFetcher systemConfigFetcher,
            ReactiveExtensionClient client) {
        this.globalInfoService = globalInfoService;
        this.validator = validator;
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.systemConfigFetcher = systemConfigFetcher;
        this.client = client;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> preAuthSignUpEndpoints() {
        return RouterFunctions.nest(
                path("/signup"),
                RouterFunctions.route()
                        .GET("", request -> {
                            var signUpData = new SignUpData();
                            var bindingResult = new BeanPropertyBindingResult(signUpData, "form");
                            var model = bindingResult.getModel();
                            model.put("globalInfo", globalInfoService.getGlobalInfo());
                            model.put("agreementPages", fetchAgreementPages());
                            return ServerResponse.ok().render("signup", model);
                        })
                        .POST(
                                "",
                                contentType(APPLICATION_FORM_URLENCODED),
                                request -> request.bind(SignUpData.class).flatMap(signUpData -> {
                                    // sign up
                                    var bindingResult = validate(signUpData, validator, request.exchange());
                                    var model = bindingResult.getModel();
                                    model.put("globalInfo", globalInfoService.getGlobalInfo());
                                    model.put("agreementPages", fetchAgreementPages());
                                    if (bindingResult.hasErrors()) {
                                        return ServerResponse.ok().render("signup", model);
                                    }
                                    return userService
                                            .signUp(signUpData)
                                            .flatMap(user -> ServerResponse.status(HttpStatus.FOUND)
                                                    .location(URI.create("/login?signup"))
                                                    .build())
                                            .doOnError(t -> {
                                                model.put("error", "unknown");
                                                model.put("errorMessage", t.getMessage());
                                            })
                                            .doOnError(EmailVerificationFailed.class, e -> {
                                                bindingResult.addError(new FieldError(
                                                        "form",
                                                        "emailCode",
                                                        signUpData.getEmailCode(),
                                                        true,
                                                        new String[] {"signup.error.email-code.invalid"},
                                                        null,
                                                        "Invalid Email Code"));
                                            })
                                            .doOnError(EmailAlreadyTakenException.class, e -> {
                                                bindingResult.addError(new FieldError(
                                                        "form",
                                                        "email",
                                                        signUpData.getEmail(),
                                                        true,
                                                        new String[] {"signup.error.email.already-taken"},
                                                        null,
                                                        "Email Already Taken"));
                                            })
                                            .doOnError(
                                                    RateLimitExceededException.class,
                                                    e -> model.put("error", "rate-limit-exceeded"))
                                            .doOnError(
                                                    DuplicateNameException.class,
                                                    e -> model.put("error", "duplicate-username"))
                                            .doOnError(
                                                    RestrictedNameException.class,
                                                    e -> model.put("error", "restricted-username"))
                                            .doOnError(AgreementNotAcceptedException.class, e -> {
                                                bindingResult.addError(new FieldError(
                                                        "form",
                                                        "agreedToTerms",
                                                        signUpData.getAgreedToTerms(),
                                                        true,
                                                        new String[] {"signup.error.agreed-to-terms.required"},
                                                        null,
                                                        "Please agree to the terms"));
                                            })
                                            .onErrorResume(
                                                    e -> ServerResponse.ok().render("signup", model));
                                }))
                        .POST(
                                "/send-email-code",
                                contentType(APPLICATION_JSON),
                                request -> request.bodyToMono(SendEmailCodeBody.class)
                                        .flatMap(body -> {
                                            var bindingResult = validate(body, "body", validator, request.exchange());
                                            if (bindingResult.hasErrors()) {
                                                return Mono.error(new RequestBodyValidationException(bindingResult));
                                            }
                                            var email = body.getEmail();
                                            return emailVerificationService
                                                    .sendRegisterVerificationCode(email)
                                                    .transformDeferred(rateLimiterForSendingEmailCode(
                                                            request.exchange().getRequest()))
                                                    .onErrorMap(
                                                            RequestNotPermitted.class, RateLimitExceededException::new);
                                        })
                                        .then(ServerResponse.accepted().build()))
                        .before(HaloUtils.noCache())
                        .build());
    }

    private <T> RateLimiterOperator<T> rateLimiterForSendingEmailCode(ServerHttpRequest request) {
        var clientIp = IpAddressUtils.getClientIp(request);
        var rateLimiterKey = "send-email-code-for-signing-up-from-" + clientIp;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-email-verification-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    private Mono<List<HashMap<String, String>>> fetchAgreementPages() {
        return Optional.ofNullable(systemConfigFetcher)
                .map(fetcher -> fetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .flatMapMany(userSetting -> {
                            var pages = userSetting.getRequiredAgreementPages();
                            if (CollectionUtils.isEmpty(pages)) {
                                return Flux.empty();
                            }
                            return Flux.fromIterable(pages);
                        })
                        .flatMap(pageName -> client.fetch(SinglePage.class, pageName)
                                .map(page -> {
                                    var map = new HashMap<String, String>();
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

    @Data
    public static class SendEmailCodeBody {

        @Email
        @NotBlank
        String email;
    }
}
