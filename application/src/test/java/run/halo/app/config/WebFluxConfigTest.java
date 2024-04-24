package run.halo.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Set;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.endpoint.WebSocketEndpoint;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;

@SpringBootTest(properties = "halo.console.location=classpath:/console/", webEnvironment =
    SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(WebFluxConfigTest.WebSocketSupportTest.TestWebSocketConfiguration.class)
@AutoConfigureWebTestClient
class WebFluxConfigTest {

    @Autowired
    WebTestClient webClient;

    @SpyBean
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

        @Test
        void shouldRequestConsoleIndex() {
            List.of(
                    "/console",
                    "/console/index",
                    "/console/index.html",
                    "/console/dashboard",
                    "/console/fake"
                )
                .forEach(uri -> webClient.get().uri(uri)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class).value(StringStartsWith.startsWith("console index"))
                );
        }

        @Test
        void shouldRequestConsoleAssetsCorrectly() {
            webClient.get().uri("/console/assets/fake.txt")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(StringStartsWith.startsWith("fake."));
        }

        @Test
        void shouldResponseNotFoundWhenAssetsNotExist() {
            webClient.get().uri("/console/assets/not-found.txt")
                .exchange()
                .expectStatus().isNotFound();
        }
    }

}