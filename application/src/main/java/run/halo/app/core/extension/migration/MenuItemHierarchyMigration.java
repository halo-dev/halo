package run.halo.app.core.extension.migration;

import static org.springframework.util.StringUtils.hasText;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExtensionInitializedEvent;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
@RequiredArgsConstructor
class MenuItemHierarchyMigration {

    private static final String TRUE = Boolean.TRUE.toString();
    private static final String CLONE_GENERATE_NAME = "menu-item-";

    private final ReactiveExtensionClient client;

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    Mono<Void> onApplicationEvent(ExtensionInitializedEvent ignored) {
        return migrate()
                .doOnNext(summary -> log.info(
                        "Menu item hierarchy migration finished: menus={}, updated={}, clonesCreated={}, "
                                + "clonesReused={}, warnings={}, failures={}",
                        summary.getMenus(),
                        summary.getUpdated(),
                        summary.getClonesCreated(),
                        summary.getClonesReused(),
                        summary.getWarnings(),
                        summary.getFailures()))
                .then()
                .onErrorResume(t -> {
                    log.error("Failed to migrate menu item hierarchy, continuing startup.", t);
                    return Mono.empty();
                });
    }

    Mono<MigrationSummary> migrate() {
        var listOptions = ListOptions.builder().build();
        return Mono.zip(
                        client.listAll(Menu.class, listOptions, Sort.unsorted()).collectList(),
                        client.listAll(MenuItem.class, listOptions, Sort.unsorted())
                                .collectList())
                .flatMap(tuple -> migrate(tuple.getT1(), tuple.getT2()));
    }

    Mono<MigrationSummary> migrate(List<Menu> menus, List<MenuItem> menuItems) {
        var context = new MigrationContext(menus, menuItems);
        return Flux.fromIterable(context.rootPaths())
                .concatMap(path -> migratePath(context, path, false))
                .then(labelAssignedMenuItems(context))
                .then(Mono.fromSupplier(context::getSummary));
    }

    private Mono<MenuItem> migratePath(MigrationContext context, LegacyPath path, boolean forceClone) {
        var original = context.getItem(path.getItemName());
        if (original == null) {
            context.warn(
                    "Skipping missing legacy MenuItem reference '{}' from menu '{}' at path {}.",
                    path.getItemName(),
                    path.getMenuName(),
                    path.getOriginalPath());
            return Mono.empty();
        }

        var useOriginal = !forceClone && context.canUseOriginal(original, path);
        Mono<MenuItem> migrated =
                useOriginal ? migrateOriginal(context, original, path) : migrateClone(context, original, path);
        return migrated.flatMap(target -> migrateChildren(context, original, target, path, forceClone || !useOriginal)
                .thenReturn(target));
    }

    private Mono<Void> migrateChildren(
            MigrationContext context, MenuItem original, MenuItem target, LegacyPath currentPath, boolean forceClone) {
        return Flux.fromIterable(legacyChildren(original))
                .concatMap(childName -> {
                    if (currentPath.contains(childName)) {
                        context.warn(
                                "Skipping cyclic legacy MenuItem edge '{} -> {}' from menu '{}' at path {}.",
                                original.getMetadata().getName(),
                                childName,
                                currentPath.getMenuName(),
                                currentPath.getOriginalPath());
                        return Mono.empty();
                    }
                    var childPath = currentPath.child(target.getMetadata().getName(), childName);
                    return migratePath(context, childPath, forceClone);
                })
                .then();
    }

    private Mono<MenuItem> migrateOriginal(MigrationContext context, MenuItem item, LegacyPath path) {
        context.recordOriginalUse(item, path);
        return updateIfChanged(context, item, () -> {
            var changed = false;
            var spec = ensureSpec(item);
            if (!hasText(spec.getMenuName())) {
                spec.setMenuName(path.getMenuName());
                changed = true;
            }
            if (!hasText(spec.getParent()) && hasText(path.getParentName())) {
                spec.setParent(path.getParentName());
                changed = true;
            }
            return markMigrated(item) || changed;
        });
    }

    private Mono<MenuItem> migrateClone(MigrationContext context, MenuItem original, LegacyPath path) {
        var existingClone = context.findClone(path);
        if (existingClone != null) {
            context.recordCloneReused();
            return updateIfChanged(context, existingClone, () -> {
                var changed = false;
                var spec = ensureSpec(existingClone);
                if (!hasText(spec.getMenuName())) {
                    spec.setMenuName(path.getMenuName());
                    changed = true;
                }
                if (!hasText(spec.getParent()) && hasText(path.getParentName())) {
                    spec.setParent(path.getParentName());
                    changed = true;
                }
                return markMigrated(existingClone) || changed;
            });
        }

        var clone = cloneMenuItem(original, path);
        return client.create(clone)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
                .doOnNext(created -> {
                    context.addItem(created);
                    context.recordCloneCreated();
                })
                .onErrorResume(t -> {
                    context.failure(
                            "Failed to create clone for legacy MenuItem '{}' in menu '{}' at path {}.",
                            t,
                            original.getMetadata().getName(),
                            path.getMenuName(),
                            path.getOriginalPath());
                    return Mono.empty();
                });
    }

    private MenuItem cloneMenuItem(MenuItem original, LegacyPath path) {
        var clone = JsonUtils.deepCopy(original);
        var metadata = new Metadata();
        metadata.setName("");
        metadata.setGenerateName(CLONE_GENERATE_NAME);
        metadata.setLabels(new HashMap<>(labelsOf(original)));

        var annotations = new HashMap<>(annotationsOf(original));
        annotations.put(MenuItem.ORIGINAL_MENU_ITEM_ANNO, original.getMetadata().getName());
        annotations.put(MenuItem.MIGRATION_MENU_NAME_ANNO, path.getMenuName());
        annotations.put(MenuItem.MIGRATION_PARENT_NAME_ANNO, Objects.toString(path.getParentName(), ""));
        annotations.put(MenuItem.MIGRATION_PATH_ANNO, path.pathAsJson());
        metadata.setAnnotations(annotations);
        clone.setMetadata(metadata);

        var spec = ensureSpec(clone);
        spec.setMenuName(path.getMenuName());
        spec.setParent(path.getParentName());
        markMigrated(clone);
        return clone;
    }

    private Mono<MenuItem> updateIfChanged(MigrationContext context, MenuItem item, ChangeDetector detector) {
        if (!detector.changed()) {
            return Mono.just(item);
        }
        return client.update(item)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
                .doOnNext(updated -> {
                    context.addItem(updated);
                    context.recordUpdated();
                })
                .onErrorResume(t -> {
                    context.failure(
                            "Failed to update MenuItem '{}'.",
                            t,
                            item.getMetadata().getName());
                    return Mono.just(item);
                });
    }

    private Mono<Void> labelAssignedMenuItems(MigrationContext context) {
        return Flux.fromIterable(context.items())
                .filter(item -> {
                    var spec = item.getSpec();
                    return spec != null
                            && hasText(spec.getMenuName())
                            && !TRUE.equals(labelsOf(item).get(MenuItem.HIERARCHY_MIGRATED_LABEL));
                })
                .concatMap(item -> updateIfChanged(context, item, () -> markMigrated(item)))
                .then();
    }

    private static MenuItem.MenuItemSpec ensureSpec(MenuItem item) {
        if (item.getSpec() == null) {
            item.setSpec(new MenuItem.MenuItemSpec());
        }
        return item.getSpec();
    }

    private static boolean markMigrated(MenuItem item) {
        var metadata = ensureMetadata(item);
        if (metadata.getLabels() == null) {
            metadata.setLabels(new HashMap<>());
        }
        var labels = metadata.getLabels();
        if (TRUE.equals(labels.get(MenuItem.HIERARCHY_MIGRATED_LABEL))) {
            return false;
        }
        labels.put(MenuItem.HIERARCHY_MIGRATED_LABEL, TRUE);
        return true;
    }

    private static MetadataOperator ensureMetadata(MenuItem item) {
        if (item.getMetadata() == null) {
            item.setMetadata(new Metadata());
        }
        return item.getMetadata();
    }

    private static Map<String, String> labelsOf(MenuItem item) {
        var metadata = ensureMetadata(item);
        if (metadata.getLabels() == null) {
            return Map.of();
        }
        return metadata.getLabels();
    }

    private static Map<String, String> annotationsOf(MenuItem item) {
        var metadata = ensureMetadata(item);
        if (metadata.getAnnotations() == null) {
            return Map.of();
        }
        return metadata.getAnnotations();
    }

    private static List<String> legacyChildren(MenuItem item) {
        var spec = item.getSpec();
        if (spec == null || spec.getChildren() == null) {
            return List.of();
        }
        return spec.getChildren().stream().filter(Objects::nonNull).toList();
    }

    @FunctionalInterface
    private interface ChangeDetector {
        boolean changed();
    }

    @Value
    static class MigrationSummary {
        int menus;
        int updated;
        int clonesCreated;
        int clonesReused;
        int warnings;
        int failures;
    }

    @Value
    private static class LegacyPath {
        String menuName;

        @Nullable
        String parentName;

        String itemName;
        List<String> originalPath;

        LegacyPath child(String parentName, String childName) {
            var nextPath = new ArrayList<>(originalPath);
            nextPath.add(childName);
            return new LegacyPath(menuName, parentName, childName, nextPath);
        }

        boolean contains(String itemName) {
            return originalPath.contains(itemName);
        }

        String pathAsJson() {
            return JsonUtils.objectToJson(originalPath);
        }
    }

    private static class MigrationContext {
        private final List<Menu> menus;
        private final Map<String, MenuItem> itemsByName;
        private final Map<String, LegacyPath> originalUses = new HashMap<>();
        private final MutableMigrationSummary summary;

        MigrationContext(List<Menu> menus, List<MenuItem> items) {
            this.menus = menus.stream().sorted(menuComparator()).toList();
            this.itemsByName = new HashMap<>();
            items.forEach(this::addItem);
            this.summary = new MutableMigrationSummary(this.menus.size());
        }

        List<LegacyPath> rootPaths() {
            var paths = new ArrayList<LegacyPath>();
            for (var menu : menus) {
                var menuName = menu.getMetadata().getName();
                var spec = menu.getSpec();
                if (spec == null || spec.getMenuItems() == null) {
                    continue;
                }
                spec.getMenuItems().stream()
                        .filter(Objects::nonNull)
                        .forEach(itemName -> paths.add(new LegacyPath(menuName, null, itemName, List.of(itemName))));
            }
            return paths;
        }

        @Nullable
        MenuItem getItem(String name) {
            return itemsByName.get(name);
        }

        Iterable<MenuItem> items() {
            return List.copyOf(itemsByName.values());
        }

        void addItem(MenuItem item) {
            if (item.getMetadata() != null && hasText(item.getMetadata().getName())) {
                itemsByName.put(item.getMetadata().getName(), item);
            }
        }

        boolean canUseOriginal(MenuItem item, LegacyPath path) {
            var name = item.getMetadata().getName();
            var spec = item.getSpec();
            if (spec != null
                    && hasText(spec.getMenuName())
                    && !Objects.equals(spec.getMenuName(), path.getMenuName())) {
                return false;
            }
            return !originalUses.containsKey(name);
        }

        void recordOriginalUse(MenuItem item, LegacyPath path) {
            originalUses.put(item.getMetadata().getName(), path);
        }

        @Nullable
        MenuItem findClone(LegacyPath path) {
            var parentName = Objects.toString(path.getParentName(), "");
            var pathJson = path.pathAsJson();
            var matches = itemsByName.values().stream()
                    .filter(item -> {
                        var annotations = annotationsOf(item);
                        return Objects.equals(annotations.get(MenuItem.ORIGINAL_MENU_ITEM_ANNO), path.getItemName())
                                && Objects.equals(
                                        annotations.get(MenuItem.MIGRATION_MENU_NAME_ANNO), path.getMenuName())
                                && Objects.equals(annotations.get(MenuItem.MIGRATION_PARENT_NAME_ANNO), parentName)
                                && Objects.equals(annotations.get(MenuItem.MIGRATION_PATH_ANNO), pathJson);
                    })
                    .sorted(cloneComparator())
                    .toList();
            if (matches.size() > 1) {
                warn(
                        "Found {} duplicate MenuItem clones for original '{}' in menu '{}' at path {}; reusing '{}'.",
                        matches.size(),
                        path.getItemName(),
                        path.getMenuName(),
                        path.getOriginalPath(),
                        matches.get(0).getMetadata().getName());
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        void warn(String message, Object... args) {
            summary.warnings++;
            log.warn("Menu item hierarchy migration: " + message, args);
        }

        void failure(String message, Throwable throwable, Object... args) {
            summary.failures++;
            var logArgs = Arrays.copyOf(args, args.length + 1);
            logArgs[args.length] = throwable;
            log.warn("Menu item hierarchy migration: " + message, logArgs);
        }

        void recordUpdated() {
            summary.updated++;
        }

        void recordCloneCreated() {
            summary.clonesCreated++;
        }

        void recordCloneReused() {
            summary.clonesReused++;
        }

        MigrationSummary getSummary() {
            return new MigrationSummary(
                    summary.menus,
                    summary.updated,
                    summary.clonesCreated,
                    summary.clonesReused,
                    summary.warnings,
                    summary.failures);
        }

        private static Comparator<Menu> menuComparator() {
            return Comparator.comparing(
                            (Menu menu) -> menu.getMetadata().getCreationTimestamp(),
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(
                            menu -> menu.getMetadata().getName(), Comparator.nullsLast(Comparator.naturalOrder()));
        }

        private static Comparator<MenuItem> cloneComparator() {
            return Comparator.comparing(
                            (MenuItem item) -> creationTimestamp(item), Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(
                            item -> item.getMetadata().getName(), Comparator.nullsLast(Comparator.naturalOrder()));
        }

        @Nullable
        private static Instant creationTimestamp(MenuItem item) {
            return item.getMetadata() == null ? null : item.getMetadata().getCreationTimestamp();
        }
    }

    private static class MutableMigrationSummary {
        private final int menus;
        private int updated;
        private int clonesCreated;
        private int clonesReused;
        private int warnings;
        private int failures;

        MutableMigrationSummary(int menus) {
            this.menus = menus;
        }
    }
}
