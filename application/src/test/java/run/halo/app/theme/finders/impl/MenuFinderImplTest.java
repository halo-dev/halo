package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * Tests for {@link MenuFinderImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class MenuFinderImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private SystemConfigFetcher environmentFetcher;

    @InjectMocks
    private MenuFinderImpl menuFinder;

    @BeforeEach
    void setUp() {
        Mockito.lenient()
                .when(client.listAll(eq(MenuItem.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenAnswer(invocation -> {
                    ListOptions options = invocation.getArgument(1);
                    var condition = options.toCondition().toString();
                    var menuName = condition.replace("spec.menuName = ", "");
                    return Flux.fromIterable(testMenuItems())
                            .filter(item -> menuName.equals(item.getSpec().getMenuName()));
                });
    }

    @Test
    void getByNameBuildsTreeFromMenuNameAndParent() {
        var menu = menu("D", of("legacy-root"));
        Mockito.when(client.fetch(eq(Menu.class), eq("D"))).thenReturn(Mono.just(menu));

        MenuVo menuVo = menuFinder.getByName("D").block();

        assertThat(menuVo.getSpec().getMenuItems()).containsExactly("legacy-root");
        assertThat(visualizeTree(List.of(menuVo))).isEqualTo("""
            D
            └── E
                ├── A
                │   └── B
                └── C
            """);
    }

    @Test
    void getPrimaryBuildsConfiguredMenuFromNewFields() {
        var primary = new SystemSetting.Menu();
        primary.setPrimary("Y");
        Mockito.when(client.listAll(eq(Menu.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenReturn(Flux.just(menu("X", of("legacy-x")), menu("Y", of("legacy-y"))));
        Mockito.when(environmentFetcher.fetch(eq(SystemSetting.Menu.GROUP), eq(SystemSetting.Menu.class)))
                .thenReturn(Mono.just(primary));

        MenuVo menuVo = menuFinder.getPrimary().block();

        assertThat(menuVo.getMetadata().getName()).isEqualTo("Y");
        assertThat(visualizeTree(List.of(menuVo))).isEqualTo("""
            Y
            └── F
                └── H
            """);
    }

    @Test
    void invalidParentsAreRenderedAsRoots() {
        var menuItems = List.of(
                menuItem("root", "primary", "missing", 0),
                menuItem("child", "primary", "root", 0),
                menuItem("self", "primary", "self", 1),
                menuItem("outside", "primary", "other-menu-item", 2));
        var menuVo = MenuVo.from(menu("primary", of()))
                .withMenuItems(MenuFinderImpl.listToTree(menuItems.stream()
                        .map(run.halo.app.theme.finders.vo.MenuItemVo::from)
                        .toList()));

        assertThat(visualizeTree(List.of(menuVo))).isEqualTo("""
            primary
            ├── root
            │   └── child
            ├── self
            └── outside
            """);
    }

    @Test
    void doesNotFallbackToLegacyFields() {
        var menu = menu("legacy", of("E"));
        Mockito.when(client.fetch(eq(Menu.class), eq("legacy"))).thenReturn(Mono.just(menu));

        MenuVo menuVo = menuFinder.getByName("legacy").block();

        assertThat(menuVo.getMenuItems()).isEmpty();
    }

    /** Visualize a tree. */
    String visualizeTree(List<MenuVo> menuVos) {
        StringBuilder stringBuilder = new StringBuilder();
        for (MenuVo menuVo : menuVos) {
            menuVo.print(stringBuilder);
        }
        return stringBuilder.toString();
    }

    List<MenuItem> testMenuItems() {
        /*
         *  D
         *  ├── E
         *  │   ├── A
         *  │   │   └── B
         *  │   └── C
         *  X── G
         *  Y── F
         *      └── H
         */
        return List.of(
                menuItem("E", "D", null, 0),
                menuItem("G", "X", null, 0),
                menuItem("F", "Y", null, 0),
                menuItem("A", "D", "E", 0),
                menuItem("B", "D", "A", 0),
                menuItem("C", "D", "E", 1),
                menuItem("H", "Y", "F", 0));
    }

    LinkedHashSet<String> of(String... names) {
        LinkedHashSet<String> list = new LinkedHashSet<>();
        Collections.addAll(list, names);
        return list;
    }

    Menu menu(String name, LinkedHashSet<String> menuItemNames) {
        Menu menu = new Menu();
        Metadata metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37.252228Z"));
        menu.setMetadata(metadata);

        Menu.Spec spec = new Menu.Spec();
        spec.setDisplayName(name);
        spec.setMenuItems(menuItemNames);
        menu.setSpec(spec);
        return menu;
    }

    MenuItem menuItem(String name, String menuName, String parentName, int priority) {
        MenuItem menuItem = new MenuItem();
        Metadata metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37.252228Z"));
        menuItem.setMetadata(metadata);

        MenuItem.MenuItemSpec spec = new MenuItem.MenuItemSpec();
        spec.setPriority(priority);
        spec.setDisplayName(name);
        spec.setMenuName(menuName);
        spec.setParent(parentName);
        spec.setChildren(of("legacy-child"));
        menuItem.setSpec(spec);

        MenuItem.MenuItemStatus status = new MenuItem.MenuItemStatus();
        status.setDisplayName(name);
        menuItem.setStatus(status);
        return menuItem;
    }
}
