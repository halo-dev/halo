package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * Tests for {@link MenuQueryEndpoint}.
 *
 * @author guqing
 * @since 2.5.0
 */
@ExtendWith(MockitoExtension.class)
class MenuQueryEndpointTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @InjectMocks
    private MenuQueryEndpoint endpoint;

    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void getPrimaryMenu() {
        Menu menu = new Menu();
        menu.setMetadata(new Metadata());
        menu.getMetadata().setName("fake-primary");
        menu.setSpec(new Menu.Spec());
        LinkedHashSet<String> items = new LinkedHashSet<>();
        items.add("item1");
        items.add("item2");
        menu.getSpec().setMenuItems(items);
        when(client.get(eq(Menu.class), eq("fake-primary"))).thenReturn(Mono.just(menu));

        when(client.fetch(eq(MenuItem.class), eq("item1")))
            .thenReturn(Mono.just(createMenuItem("item1")));
        when(client.fetch(eq(MenuItem.class), eq("item2")))
            .thenReturn(Mono.just(createMenuItem("item2")));

        SystemSetting.Menu menuSetting = new SystemSetting.Menu();
        menuSetting.setPrimary("fake-primary");
        when(environmentFetcher.fetch(eq(SystemSetting.Menu.GROUP), eq(SystemSetting.Menu.class)))
            .thenReturn(Mono.just(menuSetting));

        webClient.get().uri("/menus/-")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.metadata.name").isEqualTo("fake-primary")
            .jsonPath("$.menuItems[0].metadata.name").isEqualTo("item1")
            .jsonPath("$.menuItems[1].metadata.name").isEqualTo("item2");

        // 验证依赖项的方法是否被调用
        verify(client).get(eq(Menu.class), eq("fake-primary"));
        verify(client, times(2)).fetch(eq(MenuItem.class), anyString());
        verify(environmentFetcher).fetch(eq(SystemSetting.Menu.GROUP),
            eq(SystemSetting.Menu.class));
    }

    @NonNull
    private static MenuItem createMenuItem(String name) {
        MenuItem menuItem = new MenuItem();
        menuItem.setMetadata(new Metadata());
        menuItem.getMetadata().setName(name);
        menuItem.setSpec(new MenuItem.MenuItemSpec());
        menuItem.getSpec().setDisplayName(name);
        return menuItem;
    }

    @Test
    void getMenuByName() {
        Menu menu = new Menu();
        menu.setMetadata(new Metadata());
        menu.getMetadata().setName("fake-menu");
        menu.setSpec(new Menu.Spec());
        LinkedHashSet<String> items = new LinkedHashSet<>();
        items.add("item1");
        items.add("item2");

        menu.getSpec().setMenuItems(items);

        when(client.get(eq(Menu.class), eq("test-menu"))).thenReturn(Mono.just(menu));
        when(client.fetch(eq(MenuItem.class), eq("item1")))
            .thenReturn(Mono.just(createMenuItem("item1")));
        when(client.fetch(eq(MenuItem.class), eq("item2")))
            .thenReturn(Mono.just(createMenuItem("item2")));

        webClient.get().uri("/menus/test-menu")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.metadata.name").isEqualTo("fake-menu")
            .jsonPath("$.menuItems[0].metadata.name").isEqualTo("item1")
            .jsonPath("$.menuItems[1].metadata.name").isEqualTo("item2");

        verify(client).get(eq(Menu.class), eq("test-menu"));
        verify(client, times(2)).fetch(eq(MenuItem.class), anyString());
    }

    @Test
    void groupVersion() {
        GroupVersion groupVersion = endpoint.groupVersion();
        assertThat(groupVersion.toString()).isEqualTo("api.halo.run/v1alpha1");
    }
}