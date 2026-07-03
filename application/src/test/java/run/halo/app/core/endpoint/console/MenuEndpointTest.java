package run.halo.app.core.endpoint.console;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class MenuEndpointTest {

    @Mock
    MenuConsoleService service;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var endpoint = new MenuEndpoint(service);
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void shouldDeleteMenu() {
        when(service.deleteMenu("primary")).thenReturn(Mono.just(menu("primary")));

        webClient
                .delete()
                .uri("/menus/primary")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.metadata.name")
                .isEqualTo("primary");

        verify(service).deleteMenu("primary");
    }

    private static Menu menu(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(1L);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37Z"));

        var spec = new Menu.Spec();
        spec.setDisplayName(name);

        var menu = new Menu();
        menu.setMetadata(metadata);
        menu.setSpec(spec);
        return menu;
    }
}
