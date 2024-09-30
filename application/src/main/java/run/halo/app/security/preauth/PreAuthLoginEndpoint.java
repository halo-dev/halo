package run.halo.app.security.preauth;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.plugin.PluginConst;
import run.halo.app.security.AuthProviderService;
import run.halo.app.security.HaloServerRequestCache;
import run.halo.app.security.authentication.CryptoService;

/**
 * Pre-auth login endpoints.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
class PreAuthLoginEndpoint {

    private final CryptoService cryptoService;

    private final GlobalInfoService globalInfoService;

    private final AuthProviderService authProviderService;

    private final ServerRequestCache serverRequestCache = new HaloServerRequestCache();

    PreAuthLoginEndpoint(CryptoService cryptoService, GlobalInfoService globalInfoService,
        AuthProviderService authProviderService) {
        this.cryptoService = cryptoService;
        this.globalInfoService = globalInfoService;
        this.authProviderService = authProviderService;
    }

    @Bean
    RouterFunction<ServerResponse> preAuthLoginEndpoints() {
        return RouterFunctions.nest(path("/login"), RouterFunctions.route()
            .GET("", request -> {
                // TODO get redirect URI and cache it
                var exchange = request.exchange();
                var contextPath = exchange.getRequest().getPath().contextPath().value();
                var publicKey = cryptoService.readPublicKey()
                    .map(key -> Base64.getEncoder().encodeToString(key));
                var globalInfo = globalInfoService.getGlobalInfo().cache();
                var loginMethod = request.queryParam("method").orElse("local");
                var authProviders = authProviderService.getEnabledProviders().cache();
                var authProvider = authProviders
                    .filter(ap -> Objects.equals(loginMethod, ap.getMetadata().getName()))
                    .next()
                    .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                        "Invalid login method " + loginMethod)
                    ))
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
                    .filter(ap -> !AuthProvider.AuthType.FORM.equals(ap.getSpec().getAuthType()))
                    .cache();
                var formAuthProviders = authProviders
                    .filter(ap -> AuthProvider.AuthType.FORM.equals(ap.getSpec().getAuthType()))
                    .filter(ap -> !Objects.equals(loginMethod, ap.getMetadata().getName()))
                    .cache();

                return serverRequestCache.saveRequest(exchange).then(Mono.defer(() ->
                    ServerResponse.ok().render("login", Map.of(
                        "action", contextPath + "/login",
                        "publicKey", publicKey,
                        "globalInfo", globalInfo,
                        "authProvider", authProvider,
                        "fragmentTemplateName", fragmentTemplateName,
                        "socialAuthProviders", socialAuthProviders,
                        "formAuthProviders", formAuthProviders
                        // TODO Add more models here
                    ))
                ));
            })
            .build());
    }
}
