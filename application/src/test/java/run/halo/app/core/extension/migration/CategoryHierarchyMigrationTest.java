package run.halo.app.core.extension.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class CategoryHierarchyMigrationTest {

    @Mock
    private ReactiveExtensionClient client;

    private CategoryHierarchyMigration migration;
    private final Map<String, Category> storedCategories = new HashMap<>();

    @BeforeEach
    void setUp() {
        migration = new CategoryHierarchyMigration(client);
        lenient()
                .when(client.fetch(eq(Category.class), anyString()))
                .thenAnswer(invocation -> Mono.justOrEmpty(storedCategories.get(invocation.getArgument(1)))
                        .map(JsonUtils::deepCopy));
        lenient().when(client.update(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            var stored = storedCategories.get(category.getMetadata().getName());
            stored.setSpec(category.getSpec());
            stored.setMetadata(category.getMetadata());
            stored.setStatus(category.getStatus());
            return Mono.just(category);
        });
    }

    @Test
    void migratesNormalTreeAndPreservesLegacyChildren() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B", "C");
        var child = category("B", "2024-01-02T00:00:00Z", "D");
        var anotherChild = category("C", "2024-01-03T00:00:00Z");
        var grandChild = category("D", "2024-01-04T00:00:00Z");

        migration
                .migrate(List.of(parent, child, anotherChild, grandChild))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(child.getSpec().getParent()).isEqualTo("A");
                    assertThat(anotherChild.getSpec().getParent()).isEqualTo("A");
                    assertThat(grandChild.getSpec().getParent()).isEqualTo("B");
                    assertThat(parent.getSpec().getChildren()).containsExactly("B", "C");
                    assertThat(child.getSpec().getChildren()).containsExactly("D");
                    assertThat(summary.assignedParents()).isEqualTo(3);
                    assertThat(summary.updated()).isEqualTo(4);
                    assertThat(summary.failures()).isZero();
                    assertMigrationLabel(parent, child, anotherChild, grandChild);
                })
                .verifyComplete();
        verify(client, times(4)).update(any(Category.class));
    }

    @Test
    void keepsExistingParentValues() {
        var parent = category("A", "2024-01-01T00:00:00Z", "C");
        var existingParent = category("B", "2024-01-02T00:00:00Z");
        var child = category("C", "2024-01-03T00:00:00Z");
        child.getSpec().setParent("B");

        migration
                .migrate(List.of(parent, existingParent, child))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(child.getSpec().getParent()).isEqualTo("B");
                    assertThat(summary.assignedParents()).isZero();
                    assertThat(summary.conflictingEdges()).isEqualTo(1);
                    assertMigrationLabel(parent, existingParent, child);
                })
                .verifyComplete();
    }

    @Test
    void preservesMigratedCategoryMovedToRoot() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z");
        child.getMetadata().setLabels(new HashMap<>());
        child.getMetadata().getLabels().put(Category.HIERARCHY_MIGRATED_LABEL, "true");

        migration
                .migrate(List.of(parent, child))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(child.getSpec().getParent()).isNull();
                    assertThat(summary.assignedParents()).isZero();
                    assertThat(summary.updated()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void rerunIsIdempotentWhenParentAndLabelsAlreadyExist() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z");
        child.getSpec().setParent("A");
        parent.getMetadata().setLabels(new HashMap<>());
        parent.getMetadata().getLabels().put(Category.HIERARCHY_MIGRATED_LABEL, "true");
        child.getMetadata().setLabels(new HashMap<>());
        child.getMetadata().getLabels().put(Category.HIERARCHY_MIGRATED_LABEL, "true");

        migration
                .migrate(List.of(parent, child))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(summary.assignedParents()).isZero();
                    assertThat(summary.updated()).isZero();
                    assertThat(summary.skipped()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void warnsAndContinuesForMissingLegacyChildReferences() {
        var parent = category("A", "2024-01-01T00:00:00Z", "missing");

        migration
                .migrate(List.of(parent))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(summary.missingReferences()).isEqualTo(1);
                    assertThat(summary.updated()).isEqualTo(1);
                    assertMigrationLabel(parent);
                })
                .verifyComplete();
    }

    @Test
    void choosesOneDeterministicParentForMultiParentReferences() {
        var firstParent = category("A", "2024-01-01T00:00:00Z", "C");
        var secondParent = category("B", "2024-01-02T00:00:00Z", "C");
        var child = category("C", "2024-01-03T00:00:00Z");

        migration
                .migrate(List.of(secondParent, child, firstParent))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(child.getSpec().getParent()).isEqualTo("A");
                    assertThat(summary.assignedParents()).isEqualTo(1);
                    assertThat(summary.conflictingEdges()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void skipsCyclicLegacyEdges() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z", "A");

        migration
                .migrate(List.of(parent, child))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(parent.getSpec().getParent()).isNull();
                    assertThat(child.getSpec().getParent()).isEqualTo("A");
                    assertThat(summary.assignedParents()).isEqualTo(1);
                    assertThat(summary.cyclicEdges()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void continuesWhenCategoryUpdateFails() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z");
        doReturn(Mono.error(new RuntimeException("boom")))
                .when(client)
                .update(argThat(
                        (Category category) -> "B".equals(category.getMetadata().getName())));

        migration
                .migrate(List.of(parent, child))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(child.getSpec().getParent()).isNull();
                    assertThat(summary.assignedParents()).isEqualTo(1);
                    assertThat(summary.updated()).isEqualTo(1);
                    assertThat(summary.failures()).isEqualTo(1);
                    assertMigrationLabel(parent);
                    assertThat(child.getMetadata().getLabels()).isNull();
                })
                .verifyComplete();
        verify(client).update(parent);
        verify(client)
                .update(argThat(
                        (Category category) -> "B".equals(category.getMetadata().getName())));
    }

    @Test
    void retriesWithLatestVersionAndPreservesConcurrentChanges() {
        var category = category("A", "2024-01-01T00:00:00Z");
        category.getMetadata().setVersion(1L);
        doAnswer(invocation -> {
                    category.getMetadata().setVersion(2L);
                    category.getSpec().setDisplayName("concurrent edit");
                    return Mono.error(new OptimisticLockingFailureException("conflict"));
                })
                .when(client)
                .update(argThat((Category item) ->
                        Long.valueOf(1L).equals(item.getMetadata().getVersion())));

        migration
                .migrate(List.of(category))
                .as(StepVerifier::create)
                .assertNext(summary -> {
                    assertThat(summary.failures()).isZero();
                    assertThat(summary.updated()).isEqualTo(1);
                    assertThat(category.getMetadata().getVersion()).isEqualTo(2L);
                    assertThat(category.getSpec().getDisplayName()).isEqualTo("concurrent edit");
                    assertMigrationLabel(category);
                })
                .verifyComplete();
        verify(client, times(2)).fetch(Category.class, "A");
    }

    private static void assertMigrationLabel(Category... categories) {
        for (Category category : categories) {
            assertThat(category.getMetadata().getLabels())
                    .containsEntry(Category.HIERARCHY_MIGRATED_LABEL, CategoryHierarchyMigration.MIGRATION_LABEL_VALUE);
        }
    }

    private Category category(String name, String creationTimestamp, String... children) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse(creationTimestamp));
        category.setMetadata(metadata);

        var spec = new Category.CategorySpec();
        spec.setDisplayName(name);
        spec.setSlug(name);
        spec.setPriority(0);
        spec.setChildren(List.of(children));
        category.setSpec(spec);
        storedCategories.put(name, category);
        return category;
    }
}
