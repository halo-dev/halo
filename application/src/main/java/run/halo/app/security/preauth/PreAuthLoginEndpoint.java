package run.halo.app.security.preauth;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.security.SecurityConstant.REMEMBER_ME_PARAMETER_NAME;
import static run.halo.app.security.SecurityConstant.USERNAME_PARAMETER_NAME;

import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.plugin.PluginConst;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.HaloServerRequestCache;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.authentication.CryptoService;

/**
 * Pre-auth login endpoints.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
@RequiredArgsConstructor
class PreAuthLoginEndpoint {

    private final CryptoService cryptoService;

    private final GlobalInfoService globalInfoService;

    private final AuthProviderService authProviderService;

    private final ServerRequestCache serverRequestCache = new HaloServerRequestCache();

    private final LoginParameterRequestCache parameterRequestCache;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> preAuthLoginEndpoints() {
        return RouterFunctions.nest(
                path("/login"),
                RouterFunctions.route()
                        .GET("", request -> {
                            var exchange = request.exchange();
                            var contextPath = exchange.getRequest()
                                    .getPath()
                                    .contextPath()
                                    .value();
                            var publicKey = cryptoService
                                    .readPublicKey()
                                    .map(key -> Base64.getEncoder().encodeToString(key));
                            var globalInfo = globalInfoService.getGlobalInfo().cache();
                            var authProviders =
                                    authProviderService.getEnabledProviders().cache();

                            var allFormProviders = authProviders
                                    .filter(ap -> AuthProvider.AuthType.FORM.equals(
                                            ap.getSpec().getAuthType()))
                                    .cache();

                            var authProvider = Mono.justOrEmpty(request.queryParam("method"))
                                    .flatMap(method -> allFormProviders
                                            .filter(ap -> Objects.equals(
                                                    method, ap.getMetadata().getName()))
                                            .next()
                                            .switchIfEmpty(Mono.error(() ->
                                                    new ServerWebInputException("Invalid login method " + method))))
                                    .switchIfEmpty(allFormProviders.next())
                                    .cache();

                            var fragmentTemplateName = authProvider.map(ap -> {
                                var templateName = "login_" + ap.getMetadata().getName();
                                return Optional.ofNullable(ap.getMetadata().getLabels())
                                        .map(labels -> labels.get(PluginConst.PLUGIN_NAME_LABEL_NAME))
                                        .filter(StringUtils::isNotBlank)
                                        .map(pluginName -> String.join(":", "plugin", pluginName, templateName))
                                        .orElse(templateName);
                            });

                            var socialAuthProviders = authProviders
                                    .filter(ap -> !AuthProvider.AuthType.FORM.equals(
                                            ap.getSpec().getAuthType()))
                                    .cache();
                            var formAuthProviders = allFormProviders
                                    .filterWhen(ap -> authProvider.map(provider -> !Objects.equals(
                                            provider.getMetadata().getName(),
                                            ap.getMetadata().getName())))
                                    .cache();

                            return serverRequestCache
                                    .saveRequest(exchange)
                                    .then(Mono.defer(() -> ServerResponse.ok()
                                            .render(
                                                    "login",
                                                    Map.of(
                                                            "action",
                                                            contextPath + "/login",
                                                            "publicKey",
                                                            publicKey,
                                                            "globalInfo",
                                                            globalInfo,
                                                            "authProvider",
                                                            authProvider,
                                                            "fragmentTemplateName",
                                                            fragmentTemplateName,
                                                            "socialAuthProviders",
                                                            socialAuthProviders,
                                                            "formAuthProviders",
                                                            formAuthProviders,
                                                            "rememberMe",
                                                            parameterRequestCache.getParameter(
                                                                    exchange, REMEMBER_ME_PARAMETER_NAME),
                                                            "username",
                                                            parameterRequestCache.getParameter(
                                                                    exchange, USERNAME_PARAMETER_NAME)
                                                            // TODO Add more models here
                                                            ))));
                        })
                        .POST("/social/{authProviderName}", request -> {
                            var authProviderName = request.pathVariable("authProviderName");
                            return authProviderService
                                    .getEnabledProviders()
                                    .filter(ap -> Objects.equals(
                                            authProviderName, ap.getMetadata().getName()))
                                    .filter(ap -> !AuthProvider.AuthType.FORM.equals(
                                            ap.getSpec().getAuthType()))
                                    .next()
                                    .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                                            "Auth provider " + authProviderName + " not found or not enabled.")))
                                    .flatMap(ap -> {
                                        var authenticationUrl = ap.getSpec().getAuthenticationUrl();
                                        return parameterRequestCache
                                                .saveParameter(request.exchange(), REMEMBER_ME_PARAMETER_NAME)
                                                .then(Mono.defer(() -> ServerResponse.status(HttpStatus.FOUND)
                                                        .location(URI.create(authenticationUrl))
                                                        .build()));
                                    });
                        })
                        .before(HaloUtils.noCache())
                        .build());
    }
}
