package run.halo.app.core.extension.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class MenuItemHierarchyMigrationTest {

    @Mock
    private ReactiveExtensionClient client;

    private final List<MenuItem> createdItems = new ArrayList<>();

    private MenuItemHierarchyMigration migration;

    @BeforeEach
    void setUp() {
        migration = new MenuItemHierarchyMigration(client);
        var sequence = new AtomicInteger();
        Mockito.lenient()
                .when(client.update(any(MenuItem.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        Mockito.lenient().when(client.create(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem item = invocation.getArgument(0);
            item.getMetadata().setName(item.getMetadata().getGenerateName() + sequence.incrementAndGet());
            item.getMetadata().setCreationTimestamp(Instant.parse("2022-08-05T04:19:37.252228Z"));
            createdItems.add(item);
            return Mono.just(item);
        });
    }

    @Test
    void shouldMigrateLegacyTreeWithoutChangingLegacyFields() {
        var root = menuItem("root", children("child"));
        var child = menuItem("child", children("grandchild"));
        var grandchild = menuItem("grandchild", null);

        var summary = migration
                .migrate(List.of(menu("primary", children("root"))), List.of(root, child, grandchild))
                .block();

        assertThat(summary.getUpdated()).isEqualTo(3);
        assertThat(root.getSpec().getMenuName()).isEqualTo("primary");
        assertThat(root.getSpec().getParent()).isNull();
        assertThat(child.getSpec().getMenuName()).isEqualTo("primary");
        assertThat(child.getSpec().getParent()).isEqualTo("root");
        assertThat(grandchild.getSpec().getParent()).isEqualTo("child");
        assertThat(root.getSpec().getChildren()).containsExactly("child");
        assertThat(child.getMetadata().getLabels()).containsEntry(MenuItem.HIERARCHY_MIGRATED_LABEL, "true");
    }

    @Test
    void shouldNotOverwriteExistingNewFields() {
        var root = menuItem("root", null);
        root.getSpec().setMenuName("custom");
        root.getSpec().setParent("keep-parent");
        var sameMenu = menuItem("same-menu", null);
        sameMenu.getSpec().setMenuName("primary");
        sameMenu.getSpec().setParent("keep-parent");

        migration
                .migrate(List.of(menu("primary", children("root", "same-menu"))), List.of(root, sameMenu))
                .block();

        assertThat(root.getSpec().getMenuName()).isEqualTo("custom");
        assertThat(root.getSpec().getParent()).isEqualTo("keep-parent");
        assertThat(sameMenu.getSpec().getMenuName()).isEqualTo("primary");
        assertThat(sameMenu.getSpec().getParent()).isEqualTo("keep-parent");
        assertThat(createdItems).hasSize(1);
        assertThat(createdItems.get(0).getSpec().getMenuName()).isEqualTo("primary");
        assertThat(createdItems.get(0).getSpec().getParent()).isNull();
    }

    @Test
    void shouldCloneSharedMenuItemAcrossMenus() {
        var shared = menuItem("shared", null);

        var summary = migration
                .migrate(
                        List.of(
                                menu("menu-a", children("shared"), "2022-08-05T04:19:37Z"),
                                menu("menu-b", children("shared"), "2022-08-05T04:19:38Z")),
                        List.of(shared))
                .block();

        assertThat(summary.getClonesCreated()).isEqualTo(1);
        assertThat(shared.getSpec().getMenuName()).isEqualTo("menu-a");
        assertThat(createdItems.get(0).getSpec().getMenuName()).isEqualTo("menu-b");
        assertThat(createdItems.get(0).getMetadata().getAnnotations())
                .containsEntry(MenuItem.ORIGINAL_MENU_ITEM_ANNO, "shared")
                .containsEntry(MenuItem.MIGRATION_MENU_NAME_ANNO, "menu-b")
                .containsEntry(MenuItem.MIGRATION_PARENT_NAME_ANNO, "")
                .containsEntry(MenuItem.MIGRATION_PATH_ANNO, "[\"shared\"]");
    }

    @Test
    void shouldCloneWholeSubtreeForMultiParentConflict() {
        var rootA = menuItem("root-a", children("shared"));
        var rootB = menuItem("root-b", children("shared"));
        var shared = menuItem("shared", children("leaf"));
        var leaf = menuItem("leaf", null);

        var summary = migration
                .migrate(List.of(menu("primary", children("root-a", "root-b"))), List.of(rootA, rootB, shared, leaf))
                .block();

        assertThat(summary.getClonesCreated()).isEqualTo(2);
        assertThat(shared.getSpec().getParent()).isEqualTo("root-a");
        var clonedShared = createdItems.stream()
                .filter(item ->
                        "shared".equals(item.getMetadata().getAnnotations().get(MenuItem.ORIGINAL_MENU_ITEM_ANNO)))
                .findFirst()
                .orElseThrow();
        var clonedLeaf = createdItems.stream()
                .filter(item ->
                        "leaf".equals(item.getMetadata().getAnnotations().get(MenuItem.ORIGINAL_MENU_ITEM_ANNO)))
                .findFirst()
                .orElseThrow();
        assertThat(clonedShared.getSpec().getParent()).isEqualTo("root-b");
        assertThat(clonedLeaf.getSpec().getParent())
                .isEqualTo(clonedShared.getMetadata().getName());
    }

    @Test
    void shouldSkipMissingReferencesCyclesAndLeaveUnreachableOrphansAlone() {
        var root = menuItem("root", children("missing", "root", "child"));
        var child = menuItem("child", null);
        var orphan = menuItem("orphan", null);
        var assignedOrphan = menuItem("assigned-orphan", null);
        assignedOrphan.getSpec().setMenuName("custom");

        var summary = migration
                .migrate(List.of(menu("primary", children("root"))), List.of(root, child, orphan, assignedOrphan))
                .block();

        assertThat(summary.getWarnings()).isEqualTo(2);
        assertThat(child.getSpec().getMenuName()).isEqualTo("primary");
        assertThat(orphan.getSpec().getMenuName()).isNull();
        assertThat(orphan.getMetadata().getLabels()).isNull();
        assertThat(assignedOrphan.getMetadata().getLabels()).containsEntry(MenuItem.HIERARCHY_MIGRATED_LABEL, "true");
    }

    @Test
    void shouldReuseExistingAnnotatedCloneOnRetry() {
        var shared = menuItem("shared", null);
        var existingClone = menuItem("menu-item-existing", null);
        existingClone.getSpec().setMenuName("menu-b");
        existingClone.getMetadata().setAnnotations(new HashMap<>());
        existingClone.getMetadata().getAnnotations().put(MenuItem.ORIGINAL_MENU_ITEM_ANNO, "shared");
        existingClone.getMetadata().getAnnotations().put(MenuItem.MIGRATION_MENU_NAME_ANNO, "menu-b");
        existingClone.getMetadata().getAnnotations().put(MenuItem.MIGRATION_PARENT_NAME_ANNO, "");
        existingClone
                .getMetadata()
                .getAnnotations()
                .put(MenuItem.MIGRATION_PATH_ANNO, JsonUtils.objectToJson(List.of("shared")));

        var summary = migration
                .migrate(
                        List.of(
                                menu("menu-a", children("shared"), "2022-08-05T04:19:37Z"),
                                menu("menu-b", children("shared"), "2022-08-05T04:19:38Z")),
                        List.of(shared, existingClone))
                .block();

        assertThat(summary.getClonesReused()).isEqualTo(1);
        assertThat(createdItems).isEmpty();
        assertThat(existingClone.getMetadata().getLabels()).containsEntry(MenuItem.HIERARCHY_MIGRATED_LABEL, "true");
    }

    private Menu menu(String name, LinkedHashSet<String> itemNames) {
        return menu(name, itemNames, "2022-08-05T04:19:37Z");
    }

    private Menu menu(String name, LinkedHashSet<String> itemNames, String createdAt) {
        var menu = new Menu();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse(createdAt));
        menu.setMetadata(metadata);

        var spec = new Menu.Spec();
        spec.setDisplayName(name);
        spec.setMenuItems(itemNames);
        menu.setSpec(spec);
        return menu;
    }

    private MenuItem menuItem(String name, LinkedHashSet<String> childNames) {
        var item = new MenuItem();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse("2022-08-05T04:19:37Z"));
        item.setMetadata(metadata);

        var spec = new MenuItem.MenuItemSpec();
        spec.setDisplayName(name);
        spec.setChildren(childNames);
        item.setSpec(spec);
        return item;
    }

    private LinkedHashSet<String> children(String... names) {
        var set = new LinkedHashSet<String>();
        Collections.addAll(set, names);
        return set;
    }
}
