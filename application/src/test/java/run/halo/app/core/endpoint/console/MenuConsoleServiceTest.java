package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class MenuConsoleServiceTest {

    @Mock
    ReactiveExtensionClient client;

    MenuConsoleService service;

    List<Menu> menus;

    List<MenuItem> menuItems;

    @BeforeEach
    void setUp() {
        service = new MenuConsoleService(client);
        menus = new ArrayList<>();
        menuItems = new ArrayList<>();
        lenient().when(client.fetch(eq(Menu.class), anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(1);
            return menus.stream()
                    .filter(menu -> menu.getMetadata().getName().equals(name))
                    .findFirst()
                    .map(Mono::just)
                    .orElseGet(Mono::empty);
        });
        lenient()
                .when(client.listAll(eq(MenuItem.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenAnswer(invocation -> {
                    ListOptions options = invocation.getArgument(1);
                    var menuName = options.toCondition().toString().replace("spec.menuName = ", "");
                    return Flux.fromIterable(menuItems)
                            .filter(item -> menuName.equals(item.getSpec().getMenuName()));
                });
        lenient().when(client.delete(any(Menu.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        lenient()
                .when(client.delete(any(MenuItem.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void deleteMenuDeletesOwnedMenuItemsBeforeMenu() {
        var menu = menu("primary");
        menus.add(menu);
        menuItems.addAll(List.of(
                menuItem("root", "primary"), menuItem("child", "primary"), menuItem("legacy-item", "secondary")));

        service.deleteMenu("primary").as(StepVerifier::create).expectNext(menu).verifyComplete();

        var captor = ArgumentCaptor.forClass(Extension.class);
        verify(client, org.mockito.Mockito.times(3)).delete(captor.capture());
        assertThat(captor.getAllValues())
                .map(extension -> extension.getMetadata().getName())
                .containsExactly("root", "child", "primary");
        assertThat(captor.getAllValues())
                .map(Extension::getClass)
                .containsExactly(MenuItem.class, MenuItem.class, Menu.class);
    }

    @Test
    void deleteMenuDoesNotDeleteMenuWhenMenuItemDeleteFails() {
        var menu = menu("primary");
        var item = menuItem("root", "primary");
        menus.add(menu);
        menuItems.add(item);
        when(client.delete(item)).thenReturn(Mono.error(new IllegalStateException("failed to delete menu item")));

        service.deleteMenu("primary")
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();

        var captor = ArgumentCaptor.forClass(Extension.class);
        verify(client).delete(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MenuItem.class);
        assertThat(captor.getValue().getMetadata().getName()).isEqualTo("root");
    }

    private static Menu menu(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(1L);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37Z"));

        var spec = new Menu.Spec();
        spec.setDisplayName(name);
        spec.setMenuItems(new LinkedHashSet<>(List.of("legacy-item")));

        var menu = new Menu();
        menu.setMetadata(metadata);
        menu.setSpec(spec);
        return menu;
    }

    private static MenuItem menuItem(String name, String menuName) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(1L);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37Z"));

        var spec = new MenuItem.MenuItemSpec();
        spec.setDisplayName(name);
        spec.setMenuName(menuName);
        spec.setPriority(0);

        var menuItem = new MenuItem();
        menuItem.setMetadata(metadata);
        menuItem.setSpec(spec);
        return menuItem;
    }
}
