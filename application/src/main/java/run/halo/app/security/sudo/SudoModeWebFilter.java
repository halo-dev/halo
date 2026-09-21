package run.halo.app.security.sudo;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Handles sudo protocol paths and enforces sudo confirmation on sensitive APIs.
 *
 * @author johnniang
 * @since 2.27.0
 */
class SudoModeWebFilter implements WebFilter {

    static final String SUDO_PATH = "/sudo";
    static final String SUDO_CODE_PATH = "/sudo/code";
    static final String SUDO_CONFIRM_PATH = "/sudo/confirm";

    private final SudoService sudoService;
    private final ServerResponse.Context responseContext;
    private final List<HttpMessageReader<?>> messageReaders;
    private final RouterFunction<ServerResponse> sudoRoutes;
    private final ServerWebExchangeMatcher sudoProtocolMatcher;
    private final ServerWebExchangeMatcher sensitiveMatcher;

    SudoModeWebFilter(SudoService sudoService, ServerResponse.Context responseContext) {
        this.sudoService = sudoService;
        this.responseContext = responseContext;
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
        this.sudoRoutes = RouterFunctions.route()
                .GET(SUDO_PATH, this::getStatus)
                .POST(SUDO_CODE_PATH, this::sendCode)
                .POST(SUDO_CONFIRM_PATH, this::confirm)
                .build();
        this.sudoProtocolMatcher = new OrServerWebExchangeMatcher(
                pathMatchers(HttpMethod.GET, SUDO_PATH),
                pathMatchers(HttpMethod.POST, SUDO_CODE_PATH, SUDO_CONFIRM_PATH));
        this.sensitiveMatcher = createSensitiveMatcher();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return sudoProtocolMatcher.matches(exchange).flatMap(sudoProtocol -> {
            if (sudoProtocol.isMatch()) {
                return handleSudoProtocol(exchange);
            }
            return sensitiveMatcher.matches(exchange).flatMap(sensitive -> {
                if (!sensitive.isMatch()) {
                    return chain.filter(exchange);
                }
                return enforceSudo(exchange).then(Mono.defer(() -> chain.filter(exchange)));
            });
        });
    }

    private Mono<Void> handleSudoProtocol(ServerWebExchange exchange) {
        var request = ServerRequest.create(exchange, messageReaders);
        return sudoRoutes
                .route(request)
                .flatMap(handler -> handler.handle(request))
                .flatMap(response -> response.writeTo(exchange, responseContext));
    }

    private Mono<ServerResponse> getStatus(ServerRequest request) {
        return sudoService
                .status(request.exchange())
                .flatMap(status -> ServerResponse.ok().bodyValue(status));
    }

    private Mono<ServerResponse> sendCode(ServerRequest request) {
        return request.formData()
                .map(form -> requiredFormValue(form, "method"))
                .flatMap(method -> sudoService.sendCode(method, request.exchange()))
                .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> confirm(ServerRequest request) {
        return request.formData()
                .flatMap(form -> {
                    var method = requiredFormValue(form, "method");
                    var code = requiredFormValue(form, "code");
                    return sudoService.confirm(method, code, request.exchange());
                })
                .then(ServerResponse.noContent().build());
    }

    private static String requiredFormValue(MultiValueMap<String, String> form, String name) {
        var value = form.getFirst(name);
        if (!StringUtils.hasText(value)) {
            throw new ServerWebInputException(name + " is required");
        }
        return value;
    }

    private Mono<Void> enforceSudo(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(SudoService::isSessionAuthentication)
                .flatMap(auth -> sudoService.requireSudo(exchange, auth.getName()));
    }

    private static ServerWebExchangeMatcher createSensitiveMatcher() {
        return new OrServerWebExchangeMatcher(
                pathMatchers(
                        HttpMethod.PUT,
                        "/apis/uc.api.halo.run/v1alpha1/users/-/password",
                        "/apis/api.console.halo.run/v1alpha1/users/-/password",
                        "/apis/api.console.halo.run/v1alpha1/users/*/password"),
                pathMatchers(HttpMethod.POST, "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens"),
                pathMatchers(HttpMethod.DELETE, "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/*"),
                pathMatchers(
                        HttpMethod.PUT,
                        "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/*/actions/revocation",
                        "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/*/actions/restoration"));
    }
}
