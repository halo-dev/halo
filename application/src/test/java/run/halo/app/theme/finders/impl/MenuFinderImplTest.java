package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.event.menu.MenuItemReconciledEvent;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
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

    private MenuFinderImpl menuFinder;

    private MenuTreeCache menuTreeCache;

    @BeforeEach
    void setUp() {
        menuTreeCache = new MenuTreeCache(client, new ConcurrentMapCacheManager());
        menuFinder = new MenuFinderImpl(menuTreeCache, environmentFetcher);
    }

    @Test
    void listAsTree() {
        Tuple2<List<Menu>, List<MenuItem>> tuple = testTree();
        Mockito.when(client.list(eq(Menu.class), eq(null), eq(null))).thenReturn(Flux.fromIterable(tuple.getT1()));
        Mockito.when(client.list(eq(MenuItem.class), eq(null), any())).thenReturn(Flux.fromIterable(tuple.getT2()));

        List<MenuVo> menuVos = menuFinder.listAsTree().collectList().block();
        assertThat(visualizeTree(menuVos)).isEqualTo("""
            D
            └── E
                ├── A
                │   └── B
                └── C
            X
            └── G
            Y
            └── F
                └── H
            """);
    }

    @Test
    void shouldCacheListAsTree() {
        Tuple2<List<Menu>, List<MenuItem>> tuple = testTree();
        Mockito.when(client.list(eq(Menu.class), eq(null), eq(null))).thenReturn(Flux.fromIterable(tuple.getT1()));
        Mockito.when(client.list(eq(MenuItem.class), eq(null), any())).thenReturn(Flux.fromIterable(tuple.getT2()));

        menuFinder.listAsTree().collectList().block();
        menuFinder.listAsTree().collectList().block();

        verify(client, times(1)).list(eq(Menu.class), eq(null), eq(null));
        verify(client, times(1)).list(eq(MenuItem.class), eq(null), any());
    }

    @Test
    void shouldReloadListAsTreeAfterEviction() {
        Tuple2<List<Menu>, List<MenuItem>> tuple = testTree();
        Mockito.when(client.list(eq(Menu.class), eq(null), eq(null))).thenReturn(Flux.fromIterable(tuple.getT1()));
        Mockito.when(client.list(eq(MenuItem.class), eq(null), any())).thenReturn(Flux.fromIterable(tuple.getT2()));

        menuFinder.listAsTree().collectList().block();
        menuTreeCache.onUpdate(new Menu(), new Menu());
        menuFinder.listAsTree().collectList().block();

        verify(client, times(2)).list(eq(Menu.class), eq(null), eq(null));
        verify(client, times(2)).list(eq(MenuItem.class), eq(null), any());
    }

    @Test
    void shouldNotEvictWhenOnlyMenuItemMetadataAnnotationChanges() {
        Tuple2<List<Menu>, List<MenuItem>> tuple = testTree();
        Mockito.when(client.list(eq(Menu.class), eq(null), eq(null))).thenReturn(Flux.fromIterable(tuple.getT1()));
        Mockito.when(client.list(eq(MenuItem.class), eq(null), any())).thenReturn(Flux.fromIterable(tuple.getT2()));

        var oldMenuItem = menuItem("E", of("A", "C"));
        var newMenuItem = menuItem("E", of("A", "C"));
        newMenuItem.getMetadata().setAnnotations(new HashMap<>());
        newMenuItem.getMetadata().getAnnotations().put(MenuItem.REQUEST_TO_UPDATE_ANNO, "now");

        menuFinder.listAsTree().collectList().block();
        menuTreeCache.onUpdate(oldMenuItem, newMenuItem);
        menuFinder.listAsTree().collectList().block();

        verify(client, times(1)).list(eq(Menu.class), eq(null), eq(null));
        verify(client, times(1)).list(eq(MenuItem.class), eq(null), any());
    }

    @Test
    void shouldReloadListAsTreeAfterMenuItemReconciled() {
        Tuple2<List<Menu>, List<MenuItem>> tuple = testTree();
        Mockito.when(client.list(eq(Menu.class), eq(null), eq(null))).thenReturn(Flux.fromIterable(tuple.getT1()));
        Mockito.when(client.list(eq(MenuItem.class), eq(null), any())).thenReturn(Flux.fromIterable(tuple.getT2()));

        menuFinder.listAsTree().collectList().block();
        menuTreeCache.onMenuItemReconciled(new MenuItemReconciledEvent(this, "E"));
        menuFinder.listAsTree().collectList().block();

        verify(client, times(2)).list(eq(Menu.class), eq(null), eq(null));
        verify(client, times(2)).list(eq(MenuItem.class), eq(null), any());
    }

    /** Visualize a tree. */
    String visualizeTree(List<MenuVo> menuVos) {
        StringBuilder stringBuilder = new StringBuilder();
        for (MenuVo menuVo : menuVos) {
            menuVo.print(stringBuilder);
        }
        return stringBuilder.toString();
    }

    Tuple2<List<Menu>, List<MenuItem>> testTree() {
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
        Menu menuD = menu("D", of("E"));
        Menu menuX = menu("X", of("G"));
        Menu menuY = menu("Y", of("F"));

        MenuItem itemE = menuItem("E", of("A", "C", "non-existent-children-name"));
        MenuItem itemG = menuItem("G", null);
        MenuItem itemF = menuItem("F", of("H"));
        MenuItem itemA = menuItem("A", of("B"));
        MenuItem itemB = menuItem("B", null);
        MenuItem itemC = menuItem("C", null);
        MenuItem itemH = menuItem("H", null);
        return Tuples.of(List.of(menuD, menuX, menuY), List.of(itemE, itemG, itemF, itemA, itemB, itemC, itemH));
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
        menu.setMetadata(metadata);

        Menu.Spec spec = new Menu.Spec();
        spec.setDisplayName(name);
        spec.setMenuItems(menuItemNames);
        menu.setSpec(spec);
        return menu;
    }

    MenuItem menuItem(String name, LinkedHashSet<String> childrenNames) {
        MenuItem menuItem = new MenuItem();
        Metadata metadata = new Metadata();
        metadata.setName(name);
        menuItem.setMetadata(metadata);

        MenuItem.MenuItemSpec spec = new MenuItem.MenuItemSpec();
        spec.setPriority(0);
        spec.setDisplayName(name);
        spec.setChildren(childrenNames);
        menuItem.setSpec(spec);

        MenuItem.MenuItemStatus status = new MenuItem.MenuItemStatus();
        menuItem.setStatus(status);
        return menuItem;
    }
}
