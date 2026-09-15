package run.halo.app.core.user.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;
import run.halo.app.core.attachment.AttachmentLister;
import run.halo.app.core.attachment.endpoint.AttachmentEndpoint;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.handlers.HaloErrorWebExceptionHandler;
import run.halo.app.infra.exception.handlers.ProblemDetailErrorAttributes;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ThemeResolver;
import run.halo.app.theme.engine.ThemeTemplateAvailabilityProvider;

class AttachmentUploadI18nIntegrationTest {

    @ParameterizedTest
    @CsvSource({
        "zh, 无法解析远程主机，或其地址不允许访问。",
        "en, The remote host cannot be resolved or resolves to a restricted address.",
        "es, No se puede resolver el servidor remoto o su dirección está restringida."
    })
    void shouldTranslateRealDnsFailureOverHttp(String language, String detail) throws Exception {
        var client = mock(ReactiveExtensionClient.class);
        var extensionGetter = mock(ExtensionGetter.class);
        var service = new DefaultAttachmentService(client, extensionGetter);
        var endpoint = new AttachmentEndpoint(service, mock(AttachmentLister.class));
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("file:src/main/resources/config/i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        try (var context = new GenericApplicationContext()) {
            context.registerBean(ThemeResolver.class, () -> mock(ThemeResolver.class));
            context.registerBean(
                    ThemeTemplateAvailabilityProvider.class, () -> mock(ThemeTemplateAvailabilityProvider.class));
            context.refresh();
            var properties = new WebProperties();
            var errorHandler = new HaloErrorWebExceptionHandler(
                    new ProblemDetailErrorAttributes(messages),
                    properties.getResources(),
                    properties.getError(),
                    context);
            var defaults = HandlerStrategies.withDefaults();
            errorHandler.setMessageReaders(defaults.messageReaders());
            errorHandler.setMessageWriters(defaults.messageWriters());
            errorHandler.afterPropertiesSet();
            var router = RouterFunctions.route()
                    .path("/apis/api.console.halo.run/v1alpha1", () -> endpoint.endpoint())
                    .build();
            var handler = WebHttpHandlerBuilder.webHandler(RouterFunctions.toWebHandler(router))
                    .exceptionHandler(errorHandler)
                    .build();
            var server = HttpServer.create()
                    .host("127.0.0.1")
                    .port(0)
                    .handle(new ReactorHttpHandlerAdapter(handler))
                    .bindNow();
            try {
                WebTestClient.bindToServer()
                        .baseUrl("http://127.0.0.1:" + server.port())
                        .responseTimeout(Duration.ofSeconds(30))
                        .build()
                        .post()
                        .uri("/apis/api.console.halo.run/v1alpha1/attachments/-/upload-from-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .header("Accept-Language", language)
                        .bodyValue(
                                Map.of("url", "https://halo-i18n-test.invalid/test.png", "policyName", "test-policy"))
                        .exchange()
                        .expectStatus()
                        .isBadRequest()
                        .expectHeader()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .expectBody()
                        .jsonPath("$.status")
                        .isEqualTo(400)
                        .jsonPath("$.detail")
                        .isEqualTo(detail);
                verifyNoInteractions(client, extensionGetter);
            } finally {
                server.disposeNow();
            }
        }
    }
}
