package run.halo.app.security.preauth;

import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springdoc.core.fn.builders.content.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcConnectionDetails;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.util.InMemoryResource;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.ExternalUrlChangedEvent;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemState;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.PluginService;
import run.halo.app.security.SuperAdminInitializer;
import run.halo.app.theme.service.ThemeService;

@Component
@RequiredArgsConstructor
public class SystemSetupEndpoint {
    static final String SETUP_TEMPLATE = "setup";
    static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER =
        new PropertyPlaceholderHelper(
            PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX,
            PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX
        );

    private final InitializationStateGetter initializationStateGetter;
    private final SystemConfigurableEnvironmentFetcher systemConfigFetcher;
    private final SuperAdminInitializer superAdminInitializer;
    private final ReactiveExtensionClient client;
    private final PluginService pluginService;
    private final ThemeService themeService;
    private final Validator validator;
    private final ObjectProvider<R2dbcConnectionDetails> connectionDetails;
    private final ExternalUrlSupplier externalUrl;
    private final ApplicationEventPublisher eventPublisher;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> setupPageRouter() {
        final var tag = "SystemV1alpha1Public";
        return SpringdocRouteBuilder.route()
            .GET(path("/system/setup").and(accept(MediaType.TEXT_HTML)), this::setupPage,
                builder -> builder.operationId("JumpToSetupPage")
                    .description("Jump to setup page")
                    .tag(tag)
                    .response(responseBuilder()
                        .content(Builder.contentBuilder()
                            .mediaType(MediaType.TEXT_HTML_VALUE))
                        .implementation(String.class)
                    )
            )
            .POST("/system/setup", contentType(MediaType.APPLICATION_FORM_URLENCODED), this::setup,
                builder -> builder
                    .operationId("SetupSystem")
                    .description("Setup system")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder()
                                .implementation(SetupRequest.class))
                        )
                    )
                    .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.NO_CONTENT.value()))
                        .implementation(Void.class)
                    )
            )
            .before(HaloUtils.noCache(), builder -> builder.operationId("SetNoCacheForSetUpPage"))
            .build();
    }

    private Mono<ServerResponse> setup(ServerRequest request) {
        return initializationStateGetter.userInitialized()
            .filter(initialized -> !initialized)
            .flatMap(ignored -> request.bind(SetupRequest.class)
                .flatMap(setupRequest -> {
                    // validate it
                    var bindingResult = validate(setupRequest, validator, request.exchange());
                    if (bindingResult.hasErrors()) {
                        return handleValidationErrors(bindingResult, request);
                    }
                    return doInitialization(setupRequest).then(handleSetupSuccessfully(request));
                })
            )
            .switchIfEmpty(redirectToConsole());
    }

    private static Mono<ServerResponse> handleSetupSuccessfully(ServerRequest request) {
        if (isHtmlRequest(request)) {
            return redirectToConsole();
        }
        return ServerResponse.noContent().build();
    }

    private Mono<ServerResponse> handleValidationErrors(BindingResult bindingResult,
        ServerRequest request) {
        if (isHtmlRequest(request)) {
            var model = bindingResult.getModel();
            model.put("usingH2database", usingH2database());
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .render(SETUP_TEMPLATE, model);
        }
        return Mono.error(new RequestBodyValidationException(bindingResult));
    }

    private static boolean isHtmlRequest(ServerRequest request) {
        return request.headers().accept().contains(MediaType.TEXT_HTML)
            && !HaloUtils.isXhr(request.headers().asHttpHeaders());
    }

    private static Mono<ServerResponse> redirectToConsole() {
        return ServerResponse.status(HttpStatus.FOUND).location(URI.create("/console")).build();
    }

    private Mono<Void> doInitialization(SetupRequest body) {
        var superUserMono = superAdminInitializer.initialize(
                SuperAdminInitializer.InitializationParam.builder()
                    .username(body.getUsername())
                    .password(body.getPassword())
                    .email(body.getEmail())
                    .build()
            )
            .subscribeOn(Schedulers.boundedElastic());

        var basicConfigMono = Mono.defer(() -> systemConfigFetcher.loadConfigMap()
                .flatMap(configMap -> {
                    mergeToBasicConfig(body, configMap);
                    return client.update(configMap);
                })
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException)
            )
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.fromCallable(() -> {
                eventPublisher.publishEvent(
                    new ExternalUrlChangedEvent(this, URI.create(body.getExternalUrl()).toURL())
                );
                return null;
            }));
        return Mono.when(
                basicConfigMono,
                superUserMono,
                initializeNecessaryData(body.getUsername()),
                pluginService.installPresetPlugins(),
                themeService.installPresetTheme()
            )
            .then(SystemState.upsetSystemState(client, state -> state.setIsSetup(true)));
    }

    private Mono<Void> initializeNecessaryData(String username) {
        return loadPresetExtensions(username)
            .concatMap(client::create)
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private static void mergeToBasicConfig(SetupRequest body, ConfigMap configMap) {
        Map<String, String> data = configMap.getData();
        if (data == null) {
            data = new LinkedHashMap<>();
            configMap.setData(data);
        }
        String basic = data.getOrDefault(SystemSetting.Basic.GROUP, "{}");
        var basicSetting = JsonUtils.jsonToObject(basic, SystemSetting.Basic.class);
        basicSetting.setTitle(body.getSiteTitle());
        basicSetting.setLanguage(body.getLanguage());
        basicSetting.setExternalUrl(body.getExternalUrl());
        data.put(SystemSetting.Basic.GROUP, JsonUtils.objectToJson(basicSetting));
    }

    private Mono<ServerResponse> setupPage(ServerRequest request) {
        return initializationStateGetter.userInitialized()
            .flatMap(initialized -> {
                if (initialized) {
                    return redirectToConsole();
                }
                var setupRequest = new SetupRequest();
                setupRequest.setExternalUrl(
                    externalUrl.getURL(request.exchange().getRequest()).toString()
                );
                var bindingResult = new BeanPropertyBindingResult(setupRequest, "form");
                var model = bindingResult.getModel();
                model.put("usingH2database", usingH2database());
                return ServerResponse.ok().render(SETUP_TEMPLATE, model);
            });
    }

    private boolean usingH2database() {
        var rcd = connectionDetails.getIfUnique();
        if (rcd == null) {
            // If no R2dbcConnectionDetails is available, we assume H2(mem) is used.
            return true;
        }
        var options = rcd.getConnectionFactoryOptions();
        return Optional.ofNullable(options.getValue(DRIVER))
            .map(Object::toString)
            .map("h2"::equalsIgnoreCase)
            .orElse(false);
    }

    @Data
    static class SetupRequest {

        @Schema(requiredMode = REQUIRED, minLength = 4, maxLength = 63)
        @NotBlank
        @Size(min = 4, max = 63)
        @Pattern(regexp = ValidationUtils.NAME_REGEX,
            message = "{validation.error.username.pattern}")
        private String username;

        @Schema(requiredMode = REQUIRED, minLength = 5, maxLength = 257)
        @NotBlank
        @Pattern(regexp = ValidationUtils.PASSWORD_REGEX,
            message = "{validation.error.password.pattern}")
        @Size(min = 5, max = 257)
        private String password;

        @Email
        private String email;

        @NotBlank
        @Size(max = 80)
        private String siteTitle;

        @Pattern(regexp = "^(zh-CN|zh-TW|en|es)$")
        private String language;

        @NotNull
        @URL(regexp = "^(http|https).*")
        private String externalUrl;
    }

    Flux<Unstructured> loadPresetExtensions(String username) {
        return Mono.fromCallable(
                () -> {
                    // read initial-data.yaml to string
                    var classPathResource = new ClassPathResource("initial-data.yaml");
                    String rawContent = StreamUtils.copyToString(classPathResource.getInputStream(),
                        StandardCharsets.UTF_8);
                    // build properties
                    var properties = new Properties();
                    properties.setProperty("username", username);
                    properties.setProperty("timestamp", Instant.now().toString());
                    // replace placeholders
                    var processedContent =
                        PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(rawContent, properties);
                    // load yaml to unstructured
                    var stringResource =
                        new InMemoryResource(processedContent.getBytes(StandardCharsets.UTF_8));
                    var loader = new YamlUnstructuredLoader(stringResource);
                    return loader.load();
                })
            .flatMapMany(Flux::fromIterable)
            .subscribeOn(Schedulers.boundedElastic());
    }
}