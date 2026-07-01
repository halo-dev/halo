package run.halo.app.core.extension.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class CategoryHierarchyMigrationTest {

    @Mock
    private ReactiveExtensionClient client;

    private CategoryHierarchyMigration migration;

    @BeforeEach
    void setUp() {
        migration = new CategoryHierarchyMigration(client);
        lenient()
                .when(client.update(any(Category.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void migratesNormalTreeAndPreservesLegacyChildren() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B", "C");
        var child = category("B", "2024-01-02T00:00:00Z", "D");
        var anotherChild = category("C", "2024-01-03T00:00:00Z");
        var grandChild = category("D", "2024-01-04T00:00:00Z");

        var summary = migration
                .migrate(List.of(parent, child, anotherChild, grandChild))
                .block();

        assertThat(child.getSpec().getParent()).isEqualTo("A");
        assertThat(anotherChild.getSpec().getParent()).isEqualTo("A");
        assertThat(grandChild.getSpec().getParent()).isEqualTo("B");
        assertThat(parent.getSpec().getChildren()).containsExactly("B", "C");
        assertThat(child.getSpec().getChildren()).containsExactly("D");
        assertThat(summary.assignedParents()).isEqualTo(3);
        assertThat(summary.updated()).isEqualTo(4);
        assertThat(summary.failures()).isZero();
        assertMigrationLabel(parent, child, anotherChild, grandChild);
        verify(client, times(4)).update(any(Category.class));
    }

    @Test
    void keepsExistingParentValues() {
        var parent = category("A", "2024-01-01T00:00:00Z", "C");
        var existingParent = category("B", "2024-01-02T00:00:00Z");
        var child = category("C", "2024-01-03T00:00:00Z");
        child.getSpec().setParent("B");

        var summary = migration.migrate(List.of(parent, existingParent, child)).block();

        assertThat(child.getSpec().getParent()).isEqualTo("B");
        assertThat(summary.assignedParents()).isZero();
        assertThat(summary.conflictingEdges()).isEqualTo(1);
        assertMigrationLabel(parent, existingParent, child);
    }

    @Test
    void labelDoesNotSkipIncompleteCategory() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z");
        child.getMetadata().setLabels(new HashMap<>());
        child.getMetadata().getLabels().put(Category.HIERARCHY_MIGRATED_LABEL, "true");

        var summary = migration.migrate(List.of(parent, child)).block();

        assertThat(child.getSpec().getParent()).isEqualTo("A");
        assertThat(summary.assignedParents()).isEqualTo(1);
        assertThat(summary.updated()).isEqualTo(2);
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

        var summary = migration.migrate(List.of(parent, child)).block();

        assertThat(summary.assignedParents()).isZero();
        assertThat(summary.updated()).isZero();
        assertThat(summary.skipped()).isEqualTo(2);
    }

    @Test
    void warnsAndContinuesForMissingLegacyChildReferences() {
        var parent = category("A", "2024-01-01T00:00:00Z", "missing");

        var summary = migration.migrate(List.of(parent)).block();

        assertThat(summary.missingReferences()).isEqualTo(1);
        assertThat(summary.updated()).isEqualTo(1);
        assertMigrationLabel(parent);
    }

    @Test
    void choosesOneDeterministicParentForMultiParentReferences() {
        var firstParent = category("A", "2024-01-01T00:00:00Z", "C");
        var secondParent = category("B", "2024-01-02T00:00:00Z", "C");
        var child = category("C", "2024-01-03T00:00:00Z");

        var summary =
                migration.migrate(List.of(secondParent, child, firstParent)).block();

        assertThat(child.getSpec().getParent()).isEqualTo("A");
        assertThat(summary.assignedParents()).isEqualTo(1);
        assertThat(summary.conflictingEdges()).isEqualTo(1);
    }

    @Test
    void skipsCyclicLegacyEdges() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z", "A");

        var summary = migration.migrate(List.of(parent, child)).block();

        assertThat(parent.getSpec().getParent()).isNull();
        assertThat(child.getSpec().getParent()).isEqualTo("A");
        assertThat(summary.assignedParents()).isEqualTo(1);
        assertThat(summary.cyclicEdges()).isEqualTo(1);
    }

    @Test
    void continuesWhenCategoryUpdateFails() {
        var parent = category("A", "2024-01-01T00:00:00Z", "B");
        var child = category("B", "2024-01-02T00:00:00Z");
        doReturn(Mono.error(new RuntimeException("boom"))).when(client).update(same(child));

        var summary = migration.migrate(List.of(parent, child)).block();

        assertThat(child.getSpec().getParent()).isEqualTo("A");
        assertThat(summary.assignedParents()).isEqualTo(1);
        assertThat(summary.updated()).isEqualTo(1);
        assertThat(summary.failures()).isEqualTo(1);
        assertMigrationLabel(parent, child);
        verify(client).update(parent);
        verify(client).update(child);
    }

    private static void assertMigrationLabel(Category... categories) {
        for (Category category : categories) {
            assertThat(category.getMetadata().getLabels())
                    .containsEntry(Category.HIERARCHY_MIGRATED_LABEL, CategoryHierarchyMigration.MIGRATION_LABEL_VALUE);
        }
    }

    private static Category category(String name, String creationTimestamp, String... children) {
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
        return category;
    }
}
