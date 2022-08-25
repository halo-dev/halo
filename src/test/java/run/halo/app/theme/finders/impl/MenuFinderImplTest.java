package run.halo.app.theme.finders.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
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

    private MenuFinderImpl menuFinder;

    @BeforeEach
    void setUp() {
        menuFinder = new MenuFinderImpl(client);
    }

    @Test
    void listAsTree() throws JSONException {
        Tuple2<List<Menu>, List<MenuItem>> tuple = testTree();
        Mockito.when(client.list(eq(Menu.class), eq(null), eq(null)))
            .thenReturn(Flux.fromIterable(tuple.getT1()));
        Mockito.when(client.list(eq(MenuItem.class), eq(null), any()))
            .thenReturn(Flux.fromIterable(tuple.getT2()));

        List<MenuVo> menuVos = menuFinder.listAsTree();
        JSONAssert.assertEquals("""
                [
                    {
                        "name": "D",
                        "displayName": "D",
                        "menuItems": [
                            {
                                "name": "E",
                                "priority": 0,
                                "childrenNames": [
                                    "A",
                                    "C"
                                ],
                                "children": [
                                    {
                                        "name": "A",
                                        "parentName": "E",
                                        "priority": 0,
                                        "childrenNames": [
                                            "B"
                                        ],
                                        "children": [
                                            {
                                                "name": "B",
                                                "parentName": "A",
                                                "priority": 0
                                            }
                                        ]
                                    },
                                    {
                                        "name": "C",
                                        "parentName": "E",
                                        "priority": 0
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "name": "X",
                        "displayName": "X",
                        "menuItems": [
                            {
                                "name": "G",
                                "priority": 0
                            }
                        ]
                    },
                    {
                        "name": "Y",
                        "displayName": "Y",
                        "menuItems": [
                            {
                                "name": "F",
                                "priority": 0,
                                "childrenNames": [
                                    "H"
                                ],
                                "children": [
                                    {
                                        "name": "H",
                                        "parentName": "F",
                                        "priority": 0
                                    }
                                ]
                            }
                        ]
                    }
                ]
                """,
            JsonUtils.objectToJson(menuVos),
            true);
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

        MenuItem itemE = menuItem("E", of("A", "C"));
        MenuItem itemG = menuItem("G", null);
        MenuItem itemF = menuItem("F", of("H"));
        MenuItem itemA = menuItem("A", of("B"));
        MenuItem itemB = menuItem("B", null);
        MenuItem itemC = menuItem("C", null);
        MenuItem itemH = menuItem("H", null);
        return Tuples.of(List.of(menuD, menuX, menuY),
            List.of(itemE, itemG, itemF, itemA, itemB, itemC, itemH));
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