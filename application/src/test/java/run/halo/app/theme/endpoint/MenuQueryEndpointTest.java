package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.finders.MenuFinder;
import run.halo.app.theme.finders.vo.MenuItemVo;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * Tests for {@link MenuQueryEndpoint}.
 *
 * @author guqing
 * @since 2.5.0
 */
@ExtendWith(MockitoExtension.class)
class MenuQueryEndpointTest {

    @Mock
    private MenuFinder menuFinder;

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
        Metadata metadata = new Metadata();
        metadata.setName("fake-primary");
        MenuVo menuVo = MenuVo.builder()
            .metadata(metadata)
            .spec(new Menu.Spec())
            .menuItems(List.of(MenuItemVo.from(createMenuItem("item1"))))
            .build();
        when(menuFinder.getByName(eq("fake-primary")))
            .thenReturn(Mono.just(menuVo));

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
            .jsonPath("$.menuItems[0].metadata.name").isEqualTo("item1");

        verify(menuFinder).getByName(eq("fake-primary"));
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
        Metadata metadata = new Metadata();
        metadata.setName("test-menu");
        MenuVo menuVo = MenuVo.builder()
            .metadata(metadata)
            .spec(new Menu.Spec())
            .menuItems(List.of(MenuItemVo.from(createMenuItem("item2"))))
            .build();
        when(menuFinder.getByName(eq("test-menu")))
            .thenReturn(Mono.just(menuVo));

        webClient.get().uri("/menus/test-menu")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.metadata.name").isEqualTo("test-menu")
            .jsonPath("$.menuItems[0].metadata.name").isEqualTo("item2");

        verify(menuFinder).getByName(eq("test-menu"));
    }

    @Test
    void groupVersion() {
        GroupVersion groupVersion = endpoint.groupVersion();
        assertThat(groupVersion.toString()).isEqualTo("api.halo.run/v1alpha1");
    }
}