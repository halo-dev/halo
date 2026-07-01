package run.halo.app.core.extension.migration;

import static run.halo.app.extension.MetadataUtil.nullSafeLabels;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExtensionInitializedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
class CategoryHierarchyMigration {

    static final String MIGRATION_LABEL_VALUE = "true";

    private static final int UPDATE_RETRIES = 3;

    private final ReactiveExtensionClient client;

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    Mono<Void> onApplicationEvent(ExtensionInitializedEvent ignored) {
        return migrate()
                .doOnNext(summary -> log.info("Category hierarchy migration completed: {}", summary))
                .onErrorResume(error -> {
                    log.error("Failed to migrate category hierarchy. Startup will continue.", error);
                    return Mono.empty();
                })
                .then();
    }

    Mono<MigrationSummary> migrate() {
        return client.listAll(Category.class, new ListOptions(), Sort.unsorted())
                .collectList()
                .flatMap(this::migrate);
    }

    Mono<MigrationSummary> migrate(List<Category> categories) {
        var categoriesByName = categoriesByName(categories);
        var summary = new MigrationSummary(categories.size(), categoriesByName.size());
        if (categoriesByName.isEmpty()) {
            return Mono.just(summary);
        }

        Map<String, String> selectedParents = existingParentMap(categoriesByName);
        Map<String, String> assignedParents = new LinkedHashMap<>();
        sortedCategories(categoriesByName)
                .forEach(parent ->
                        selectLegacyEdges(parent, categoriesByName, selectedParents, assignedParents, summary));

        return Flux.fromIterable(categoriesByName.values())
                .concatMap(category -> updateIfNecessary(category, assignedParents, summary))
                .then(Mono.just(summary));
    }

    private Mono<Category> updateIfNecessary(
            Category category, Map<String, String> assignedParents, MigrationSummary summary) {
        var categoryName = category.getMetadata().getName();
        var changed = false;
        var assignedParent = assignedParents.get(categoryName);
        if (StringUtils.hasText(assignedParent) && !StringUtils.hasText(parentName(category))) {
            ensureSpec(category).setParent(assignedParent);
            changed = true;
        }
        ensureMetadata(category);
        var labels = nullSafeLabels(category);
        if (!MIGRATION_LABEL_VALUE.equals(labels.get(Category.HIERARCHY_MIGRATED_LABEL))) {
            labels.put(Category.HIERARCHY_MIGRATED_LABEL, MIGRATION_LABEL_VALUE);
            changed = true;
        }
        if (!changed) {
            summary.skipped++;
            return Mono.empty();
        }
        return client.update(category)
                .retryWhen(Retry.backoff(UPDATE_RETRIES, Duration.ofMillis(100))
                        .filter(CategoryHierarchyMigration::isRetryableUpdateFailure))
                .doOnNext(updated -> summary.updated++)
                .onErrorResume(error -> {
                    summary.failures++;
                    log.warn("Failed to update Category {} during hierarchy migration.", categoryName, error);
                    return Mono.empty();
                });
    }

    private void selectLegacyEdges(
            Category parent,
            Map<String, Category> categoriesByName,
            Map<String, String> selectedParents,
            Map<String, String> assignedParents,
            MigrationSummary summary) {
        var parentName = parent.getMetadata().getName();
        var children = legacyChildren(parent);
        if (children.isEmpty()) {
            return;
        }
        for (String childName : children) {
            if (!StringUtils.hasText(childName)) {
                summary.missingReferences++;
                log.warn("Skipped blank legacy child reference in Category {}.", parentName);
                continue;
            }
            var child = categoriesByName.get(childName);
            if (child == null) {
                summary.missingReferences++;
                log.warn(
                        "Skipped missing legacy child reference during category hierarchy migration. parent={}, child={}",
                        parentName,
                        childName);
                continue;
            }
            var existingParent = parentName(child);
            if (StringUtils.hasText(existingParent)) {
                if (!Objects.equals(existingParent, parentName)) {
                    summary.conflictingEdges++;
                    log.warn(
                            "Skipped legacy Category edge because child already has parent. parent={}, child={}, "
                                    + "existingParent={}",
                            parentName,
                            childName,
                            existingParent);
                }
                continue;
            }
            var selectedParent = selectedParents.get(childName);
            if (StringUtils.hasText(selectedParent)) {
                if (!Objects.equals(selectedParent, parentName)) {
                    summary.conflictingEdges++;
                    log.warn(
                            "Skipped conflicting legacy Category edge. selectedParent={}, parent={}, child={}",
                            selectedParent,
                            parentName,
                            childName);
                }
                continue;
            }
            if (wouldCreateCycle(childName, parentName, selectedParents)) {
                summary.cyclicEdges++;
                log.warn(
                        "Skipped cyclic legacy Category edge during hierarchy migration. parent={}, child={}",
                        parentName,
                        childName);
                continue;
            }
            selectedParents.put(childName, parentName);
            assignedParents.put(childName, parentName);
            summary.assignedParents++;
        }
    }

    private static boolean wouldCreateCycle(String childName, String parentName, Map<String, String> selectedParents) {
        if (Objects.equals(childName, parentName)) {
            return true;
        }
        Set<String> visited = new HashSet<>();
        String current = parentName;
        while (StringUtils.hasText(current)) {
            if (Objects.equals(childName, current)) {
                return true;
            }
            if (!visited.add(current)) {
                return false;
            }
            current = selectedParents.get(current);
        }
        return false;
    }

    private static List<Category> sortedCategories(Map<String, Category> categoriesByName) {
        return categoriesByName.values().stream()
                .sorted(Comparator.comparing(
                                CategoryHierarchyMigration::creationTimestamp,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(
                                CategoryHierarchyMigration::categoryName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private static Map<String, Category> categoriesByName(List<Category> categories) {
        Map<String, Category> result = new LinkedHashMap<>();
        for (Category category : categories) {
            var name = categoryName(category);
            if (StringUtils.hasText(name)) {
                result.putIfAbsent(name, category);
            }
        }
        return result;
    }

    private static Map<String, String> existingParentMap(Map<String, Category> categoriesByName) {
        Map<String, String> result = new HashMap<>();
        categoriesByName.forEach((name, category) -> {
            var parentName = parentName(category);
            if (StringUtils.hasText(parentName)) {
                result.put(name, parentName);
            }
        });
        return result;
    }

    private static List<String> legacyChildren(Category category) {
        var spec = category.getSpec();
        if (spec == null || spec.getChildren() == null) {
            return List.of();
        }
        return spec.getChildren();
    }

    private static @Nullable String parentName(Category category) {
        var spec = category.getSpec();
        return spec == null ? null : spec.getParent();
    }

    private static Category.CategorySpec ensureSpec(Category category) {
        if (category.getSpec() == null) {
            category.setSpec(new Category.CategorySpec());
        }
        return category.getSpec();
    }

    private static void ensureMetadata(Category category) {
        if (category.getMetadata() == null) {
            category.setMetadata(new Metadata());
        }
    }

    private static @Nullable Instant creationTimestamp(Category category) {
        var metadata = category.getMetadata();
        return metadata == null ? null : metadata.getCreationTimestamp();
    }

    private static @Nullable String categoryName(Category category) {
        MetadataOperator metadata = category.getMetadata();
        return metadata == null ? null : metadata.getName();
    }

    private static boolean isRetryableUpdateFailure(Throwable throwable) {
        return throwable instanceof OptimisticLockingFailureException || throwable instanceof DataAccessException;
    }

    static class MigrationSummary {
        private final int totalCategories;
        private final int namedCategories;
        private int assignedParents;
        private int updated;
        private int skipped;
        private int missingReferences;
        private int conflictingEdges;
        private int cyclicEdges;
        private int failures;

        MigrationSummary(int totalCategories, int namedCategories) {
            this.totalCategories = totalCategories;
            this.namedCategories = namedCategories;
        }

        int totalCategories() {
            return totalCategories;
        }

        int namedCategories() {
            return namedCategories;
        }

        int assignedParents() {
            return assignedParents;
        }

        int updated() {
            return updated;
        }

        int skipped() {
            return skipped;
        }

        int missingReferences() {
            return missingReferences;
        }

        int conflictingEdges() {
            return conflictingEdges;
        }

        int cyclicEdges() {
            return cyclicEdges;
        }

        int failures() {
            return failures;
        }

        @Override
        public String toString() {
            return "MigrationSummary{"
                    + "totalCategories="
                    + totalCategories
                    + ", namedCategories="
                    + namedCategories
                    + ", assignedParents="
                    + assignedParents
                    + ", updated="
                    + updated
                    + ", skipped="
                    + skipped
                    + ", missingReferences="
                    + missingReferences
                    + ", conflictingEdges="
                    + conflictingEdges
                    + ", cyclicEdges="
                    + cyclicEdges
                    + ", failures="
                    + failures
                    + '}';
        }
    }
}
