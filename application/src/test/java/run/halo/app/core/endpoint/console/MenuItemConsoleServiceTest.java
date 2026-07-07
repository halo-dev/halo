package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class MenuItemConsoleServiceTest {

    @Mock
    ReactiveExtensionClient client;

    MenuItemConsoleService service;

    List<MenuItem> menuItems;

    @BeforeEach
    void setUp() {
        service = new MenuItemConsoleService(client);
        menuItems = new ArrayList<>();
        lenient().when(client.fetch(eq(MenuItem.class), anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(1);
            return menuItems.stream()
                    .filter(item -> item.getMetadata().getName().equals(name))
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
        lenient()
                .when(client.update(any(MenuItem.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void listTreeBuildsCanonicalTreeAndPromotesInvalidParents() {
        menuItems.addAll(List.of(
                menuItem("root", "primary", null, 0),
                menuItem("child", "primary", "root", 1),
                menuItem("missing-parent", "primary", "missing", 2),
                menuItem("self-parent", "primary", "self-parent", 3),
                menuItem("cycle-a", "primary", "cycle-b", 4),
                menuItem("cycle-b", "primary", "cycle-a", 5),
                menuItem("cycle-child", "primary", "cycle-a", 6),
                menuItem("other-menu", "other", null, 0)));

        service.listTree("primary")
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    assertThat(names(tree))
                            .containsExactly(
                                    "root", "missing-parent", "self-parent", "cycle-a", "cycle-b", "cycle-child");
                    assertThat(names(tree.get(0).getChildren())).containsExactly("child");
                })
                .verifyComplete();
    }

    @Test
    void updatePositionMovesRootBeforeSibling() {
        menuItems.addAll(List.of(
                menuItem("root-a", "primary", null, 0),
                menuItem("root-b", "primary", null, 1),
                menuItem("root-c", "primary", null, 2),
                menuItem("child", "primary", "root-a", 0)));

        service.updatePosition("root-c", new MenuItemPositionRequest("primary", null, "root-b"))
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    assertThat(names(tree)).containsExactly("root-a", "root-c", "root-b");
                    assertThat(names(tree.get(0).getChildren())).containsExactly("child");
                })
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(client, times(2)).update(captor.capture());
        assertThat(names(captor.getAllValues())).containsExactly("root-b", "root-c");
        assertThat(menuItem("root-c").getSpec().getPriority()).isEqualTo(1);
        assertThat(menuItem("root-b").getSpec().getPriority()).isEqualTo(2);
    }

    @Test
    void updatePositionAppendsUnderParent() {
        menuItems.addAll(List.of(
                menuItem("root-a", "primary", null, 0),
                menuItem("root-b", "primary", null, 1),
                menuItem("root-c", "primary", null, 2),
                menuItem("child", "primary", "root-a", 0)));

        service.updatePosition("root-b", new MenuItemPositionRequest("primary", "root-a", null))
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    assertThat(names(tree)).containsExactly("root-a", "root-c");
                    assertThat(names(tree.get(0).getChildren())).containsExactly("child", "root-b");
                })
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(client, times(2)).update(captor.capture());
        assertThat(names(captor.getAllValues())).containsExactly("root-b", "root-c");
        assertThat(menuItem("root-b").getSpec().getParent()).isEqualTo("root-a");
        assertThat(menuItem("root-b").getSpec().getPriority()).isEqualTo(1);
        assertThat(menuItem("root-c").getSpec().getPriority()).isEqualTo(1);
    }

    @Test
    void updatePositionSkipsUnchangedMove() {
        menuItems.addAll(List.of(
                menuItem("root-a", "primary", null, 0),
                menuItem("root-b", "primary", null, 1),
                menuItem("root-c", "primary", null, 2)));

        service.updatePosition("root-b", new MenuItemPositionRequest("primary", null, "root-c"))
                .as(StepVerifier::create)
                .assertNext(tree -> assertThat(names(tree)).containsExactly("root-a", "root-b", "root-c"))
                .verifyComplete();

        verify(client, never()).update(any(MenuItem.class));
    }

    @Test
    void updatePositionRejectsInvalidRequests() {
        menuItems.addAll(List.of(
                menuItem("root-a", "primary", null, 0),
                menuItem("root-b", "primary", null, 1),
                menuItem("root-c", "primary", null, 2),
                menuItem("child", "primary", "root-a", 0),
                menuItem("other-parent", "other", null, 0),
                menuItem("other-before", "other", null, 1)));

        assertMoveRejected("root-b", new MenuItemPositionRequest("primary", "missing", null));
        assertMoveRejected("root-a", new MenuItemPositionRequest("primary", "root-a", null));
        assertMoveRejected("root-b", new MenuItemPositionRequest("primary", "root-a", "root-c"));
        assertMoveRejected("root-b", new MenuItemPositionRequest("primary", "other-parent", null));
        assertMoveRejected("root-b", new MenuItemPositionRequest("primary", null, "other-before"));
        assertMoveRejected("root-a", new MenuItemPositionRequest("primary", "child", null));
        assertMoveRejected("root-a", new MenuItemPositionRequest("other", null, null));
        verify(client, never()).update(any(MenuItem.class));
    }

    private void assertMoveRejected(String name, MenuItemPositionRequest request) {
        service.updatePosition(name, request)
                .as(StepVerifier::create)
                .expectError(ServerWebInputException.class)
                .verify();
    }

    private MenuItem menuItem(String name) {
        return menuItems.stream()
                .filter(item -> item.getMetadata().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static List<String> names(List<MenuItemTreeNode> nodes) {
        return nodes.stream()
                .map(node -> node.getMenuItem().getMetadata().getName())
                .toList();
    }

    private static List<String> names(Iterable<MenuItem> items) {
        var names = new ArrayList<String>();
        items.forEach(item -> names.add(item.getMetadata().getName()));
        return names;
    }

    private static MenuItem menuItem(String name, String menuName, String parentName, int priority) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37Z"));

        var spec = new MenuItem.MenuItemSpec();
        spec.setDisplayName(name);
        spec.setMenuName(menuName);
        spec.setParent(parentName);
        spec.setPriority(priority);

        var status = new MenuItem.MenuItemStatus();
        status.setDisplayName(name);

        var menuItem = new MenuItem();
        menuItem.setMetadata(metadata);
        menuItem.setSpec(spec);
        menuItem.setStatus(status);
        return menuItem;
    }
}
