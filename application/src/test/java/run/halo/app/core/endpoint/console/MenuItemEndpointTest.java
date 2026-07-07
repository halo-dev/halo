package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class MenuItemEndpointTest {

    @Mock
    MenuItemConsoleService service;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var endpoint = new MenuItemEndpoint(service);
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void shouldListMenuItemTree() {
        when(service.listTree("primary")).thenReturn(Mono.just(List.of(node("root", node("child")))));

        webClient
                .get()
                .uri("/menuitems/-/tree?menuName=primary")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].menuItem.metadata.name")
                .isEqualTo("root")
                .jsonPath("$[0].children[0].menuItem.metadata.name")
                .isEqualTo("child");

        verify(service).listTree("primary");
    }

    @Test
    void shouldUpdateMenuItemPosition() {
        when(service.updatePosition(eq("root"), org.mockito.ArgumentMatchers.any(MenuItemPositionRequest.class)))
                .thenReturn(Mono.just(List.of(node("root"))));

        webClient
                .put()
                .uri("/menuitems/root/position")
                .bodyValue(new MenuItemPositionRequest("primary", null, "next"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].menuItem.metadata.name")
                .isEqualTo("root");

        var captor = ArgumentCaptor.forClass(MenuItemPositionRequest.class);
        verify(service).updatePosition(eq("root"), captor.capture());
        assertThat(captor.getValue().menuName()).isEqualTo("primary");
        assertThat(captor.getValue().beforeName()).isEqualTo("next");
    }

    private static MenuItemTreeNode node(String name, MenuItemTreeNode... children) {
        return new MenuItemTreeNode(menuItem(name), List.of(children));
    }

    private static MenuItem menuItem(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37Z"));

        var spec = new MenuItem.MenuItemSpec();
        spec.setDisplayName(name);
        spec.setMenuName("primary");

        var menuItem = new MenuItem();
        menuItem.setMetadata(metadata);
        menuItem.setSpec(spec);
        return menuItem;
    }
}
