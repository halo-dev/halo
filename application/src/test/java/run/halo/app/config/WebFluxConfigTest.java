package run.halo.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Set;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
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

@SpringBootTest(properties = "halo.console.location=classpath:/console/", webEnvironment =
    SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    WebFluxConfigTest.WebSocketSupportTest.TestWebSocketConfiguration.class,
    WebFluxConfigTest.ServerWebExchangeContextFilterTest.TestConfig.class
})
@AutoConfigureWebTestClient
class WebFluxConfigTest {

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
            webSocketClient.execute(
                    URI.create("ws://localhost:" + port + "/apis/fake.halo.run/v1alpha1/resources"),
                    session -> {
                        var send = session.send(Flux.just(session.textMessage("halo")));
                        var receive = session.receive().map(WebSocketMessage::getPayloadAsText)
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
                            message.getPayloadAsText().toUpperCase())
                        );
                    return session.send(messages).then(session.close());
                };
            }
        }

    }

    @Nested
    class ConsoleRequest {

        @WithMockUser
        @ParameterizedTest
        @ValueSource(strings = {
            "/console",
            "/console/index",
            "/console/index.html",
            "/console/dashboard",
            "/console/fake"
        })
        void shouldRequestConsoleIndex(String uri) {
            webClient.get().uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(StringStartsWith.startsWith("console index"));
        }

        @Test
        void shouldRedirectToLoginPageIfUnauthenticated() {
            webClient.get().uri("/console")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login?authentication_required");
        }

        @Test
        @WithMockUser
        void shouldRequestConsoleAssetsCorrectly() {
            webClient.get().uri("/console/assets/fake.txt")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(StringStartsWith.startsWith("fake."));
        }

        @Test
        @WithMockUser
        void shouldResponseNotFoundWhenAssetsNotExist() {
            webClient.get().uri("/console/assets/not-found.txt")
                .exchange()
                .expectStatus().isNotFound();
        }
    }

    @Nested
    class StaticResourcesTest {

        @Test
        void shouldRespond404WhenThemeResourceNotFound() {
            webClient.get().uri("/themes/fake-theme/assets/favicon.ico")
                .exchange()
                .expectStatus().isNotFound();
        }
    }


    @Nested
    class ServerWebExchangeContextFilterTest {

        @TestConfiguration
        static class TestConfig {

            @Bean
            RouterFunction<ServerResponse> assertServerWebExchangeRoute() {
                return RouterFunctions.route()
                    .GET("/assert-server-web-exchange",
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
            webClient.get().uri("/assert-server-web-exchange")
                .exchange()
                .expectStatus().isOk();
        }

    }
}