package run.halo.app.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.endpoint.WebSocketEndpoint;
import run.halo.app.core.extension.Role;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "halo.work-dir=${java.io.tmpdir}/halo-next-test")
@Import({
    WebFluxConfigTest.WebSocketSupportTest.TestWebSocketConfiguration.class,
    WebFluxConfigTest.ServerWebExchangeContextFilterTest.TestConfig.class,
    WebFluxConfigTest.UrlHandlerFilterTest.TestConfig.class
})
@AutoConfigureWebTestClient
class WebFluxConfigTest {

    private static final Path TEST_WORK_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "halo-next-test");
    private static final Path TEST_THEME_DIR = TEST_WORK_DIR.resolve("themes").resolve("fake-theme");

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    RoleService roleService;

    @LocalServerPort
    int port;

    @Nested
    class WebSocketSupportTest {

        @Test
        void shouldInitializeWebSocketEndpoint() {
            var role = new Role();
            var metadata = new Metadata();
            metadata.setName("fake-role");
            role.setMetadata(metadata);
            role.setRules(List.of(new Role.PolicyRule.Builder()
                    .apiGroups("fake.halo.run")
                    .verbs("watch")
                    .resources("resources")
                    .build()));
            when(roleService.listDependenciesFlux(Set.of("anonymous"))).thenReturn(Flux.just(role));
            var webSocketClient = new ReactorNettyWebSocketClient();
            webSocketClient
                    .execute(
                            URI.create("ws://localhost:" + port + "/apis/fake.halo.run/v1alpha1/resources"),
                            session -> {
                                var send = session.send(Flux.just(session.textMessage("halo")));
                                var receive = session.receive()
                                        .map(WebSocketMessage::getPayloadAsText)
                                        .next()
                                        .doOnNext(message -> assertEquals("HALO", message));
                                return send.and(receive);
                            })
                    .as(StepVerifier::create)
                    .verifyComplete();
        }

        @TestConfiguration
        static class TestWebSocketConfiguration {

            @Bean
            WebSocketEndpoint fakeWebSocketEndpoint() {
                return new FakeWebSocketEndpoint();
            }
        }

        static class FakeWebSocketEndpoint implements WebSocketEndpoint {

            @Override
            public String urlPath() {
                return "/resources";
            }

            @Override
            public GroupVersion groupVersion() {
                return GroupVersion.parseAPIVersion("fake.halo.run/v1alpha1");
            }

            @Override
            public WebSocketHandler handler() {
                return session -> {
                    var messages = session.receive()
                            .map(message -> session.textMessage(
                                    message.getPayloadAsText().toUpperCase()));
                    return session.send(messages).then(session.close());
                };
            }
        }
    }

    @Nested
    class UiPageRequest {

        @WithMockUser
        @ParameterizedTest
        @ValueSource(
                strings = {"/console", "/console/index", "/console/index.html", "/console/dashboard", "/console/fake"})
        void shouldRequestConsoleIndex(String uri) {
            webClient
                    .get()
                    .uri(uri)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .value(StringStartsWith.startsWith("console index"));
        }

        @WithMockUser
        @ParameterizedTest
        @ValueSource(strings = {"/uc", "/uc/index", "/uc/index.html", "/uc/profile", "/uc/fake"})
        void shouldRequestUcIndex(String uri) {
            webClient
                    .get()
                    .uri(uri)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .value(StringStartsWith.startsWith("uc index"));
        }

        @Test
        void shouldRedirectToLoginPageIfUnauthenticated() {
            webClient
                    .get()
                    .uri("/console")
                    .exchange()
                    .expectStatus()
                    .isFound()
                    .expectHeader()
                    .location("/login?authentication_required");
        }

        @Test
        @WithMockUser
        void shouldRequestUiAssetsCorrectly() {
            webClient
                    .get()
                    .uri("/ui-assets/fake.txt")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .value(StringStartsWith.startsWith("fake."));
        }

        @Test
        @WithMockUser
        void shouldResponseNotFoundWhenAssetsNotExist() {
            webClient
                    .get()
                    .uri("/ui-assets/not-found.txt")
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }
    }

    @Nested
    class StaticResourcesTest {

        @AfterEach
        void cleanUp() throws Exception {
            FileSystemUtils.deleteRecursively(TEST_THEME_DIR);
        }

        @Test
        void shouldRespond404WhenThemeResourceNotFound() {
            webClient
                    .get()
                    .uri("/themes/fake-theme/assets/favicon.ico")
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }

        @Test
        void shouldServeThemeUiAssetWithoutAuthentication() throws Exception {
            Files.createDirectories(TEST_THEME_DIR.resolve("ui-plugin").resolve("dist"));
            Files.writeString(
                    TEST_THEME_DIR.resolve("ui-plugin").resolve("dist").resolve("main.js"), "fake theme ui");

            webClient
                    .get()
                    .uri("/themes/fake-theme/ui-plugin/assets/main.js")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .consumeWith(
                            result -> assertArrayEquals("fake theme ui".getBytes(UTF_8), result.getResponseBody()));
        }

        @Test
        void shouldServeThemeUiChunkAsset() throws Exception {
            Files.createDirectories(
                    TEST_THEME_DIR.resolve("ui-plugin").resolve("dist").resolve("chunks"));
            Files.writeString(
                    TEST_THEME_DIR
                            .resolve("ui-plugin")
                            .resolve("dist")
                            .resolve("chunks")
                            .resolve("view.js"),
                    "fake chunk");

            webClient
                    .get()
                    .uri("/themes/fake-theme/ui-plugin/assets/chunks/view.js")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .consumeWith(result -> assertArrayEquals("fake chunk".getBytes(UTF_8), result.getResponseBody()));
        }

        @Test
        void shouldKeepThemePublicAssetRouteUnchanged() throws Exception {
            Files.createDirectories(TEST_THEME_DIR.resolve("templates").resolve("assets"));
            Files.createDirectories(TEST_THEME_DIR.resolve("ui-plugin").resolve("dist"));
            Files.writeString(
                    TEST_THEME_DIR.resolve("templates").resolve("assets").resolve("main.css"), "public asset");
            Files.writeString(
                    TEST_THEME_DIR.resolve("ui-plugin").resolve("dist").resolve("main.css"), "ui asset");

            webClient
                    .get()
                    .uri("/themes/fake-theme/assets/main.css")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .consumeWith(result -> assertArrayEquals("public asset".getBytes(UTF_8), result.getResponseBody()));
        }

        @Test
        void shouldRespond404WhenThemeUiResourceNotFound() {
            webClient
                    .get()
                    .uri("/themes/missing-theme/ui-plugin/assets/main.js")
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }

        @Test
        void shouldRejectThemeUiResourceDirectoryTraversal() {
            webClient
                    .get()
                    .uri("/themes/fake-theme/ui-plugin/assets/%2E%2E/theme.yaml")
                    .exchange()
                    .expectStatus()
                    .is4xxClientError();
        }

        @Test
        void shouldServeThemeScreenshotWithoutAuthentication() throws Exception {
            Files.createDirectories(TEST_THEME_DIR);
            Files.writeString(TEST_THEME_DIR.resolve("screenshot.png"), "fake screenshot");

            webClient
                    .get()
                    .uri("/themes/fake-theme/screenshot.png")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .consumeWith(
                            result -> assertArrayEquals("fake screenshot".getBytes(UTF_8), result.getResponseBody()));
        }

        @Test
        void shouldRespond404WhenThemeScreenshotExtensionIsUnsupported() throws Exception {
            Files.createDirectories(TEST_THEME_DIR);
            Files.writeString(TEST_THEME_DIR.resolve("screenshot.gif"), "fake screenshot");

            webClient
                    .get()
                    .uri("/themes/fake-theme/screenshot.gif")
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }

        @Test
        void shouldRespond404WhenThemeScreenshotDoesNotExist() {
            webClient
                    .get()
                    .uri("/themes/fake-theme/screenshot.png")
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }

        @Test
        void shouldRejectThemeScreenshotDirectoryTraversal() {
            webClient
                    .get()
                    .uri("/themes/%2E%2E/screenshot.png")
                    .exchange()
                    .expectStatus()
                    .is4xxClientError();
        }
    }

    @Nested
    class ServerWebExchangeContextFilterTest {

        @TestConfiguration
        static class TestConfig {

            @Bean
            RouterFunction<ServerResponse> assertServerWebExchangeRoute() {
                return RouterFunctions.route()
                        .GET(
                                "/assert-server-web-exchange",
                                request -> Mono.deferContextual(contextView -> {
                                    var exchange = ServerWebExchangeContextFilter.getExchange(contextView);
                                    assertTrue(exchange.isPresent());
                                    return ServerResponse.ok().build();
                                }))
                        .build();
            }
        }

        @Test
        void shouldGetExchangeFromContextView() {
            webClient
                    .get()
                    .uri("/assert-server-web-exchange")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    class UrlHandlerFilterTest {

        @TestConfiguration
        static class TestConfig {

            @Bean
            RouterFunction<ServerResponse> urlHandlerFilterTestRoute() {
                return RouterFunctions.route()
                        .GET("/fake", request -> ServerResponse.ok().bodyValue("ok"))
                        .build();
            }
        }

        @Test
        void shouldHandleUrlWithTrailingSlash() {
            webClient
                    .get()
                    .uri("/fake/")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .isEqualTo("ok");
        }
    }
}
