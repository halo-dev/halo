package run.halo.app.core.endpoint.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoryConsoleServiceTest {

    @Mock
    private ReactiveExtensionClient client;

    private CategoryConsoleService service;

    @BeforeEach
    void setUp() {
        service = new CategoryConsoleService(client);
    }

    @Test
    void listTreeBuildsOrderedTreeAndTreatsInvalidParentsAsRoots() {
        var root = category("root", null, 0, "2024-01-01T00:00:00Z");
        var missingParent = category("missing-parent", "missing", 0, "2024-01-02T00:00:00Z");
        var selfParent = category("self-parent", "self-parent", 0, "2024-01-03T00:00:00Z");
        var cyclicA = category("cyclic-a", "cyclic-b", 0, "2024-01-04T00:00:00Z");
        var cyclicB = category("cyclic-b", "cyclic-a", 0, "2024-01-05T00:00:00Z");
        var child = category("child", "cyclic-a", 0, "2024-01-06T00:00:00Z");

        mockList(root, missingParent, selfParent, cyclicA, cyclicB, child);

        service.listTree()
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    assertThat(tree.stream()
                                    .map(node ->
                                            node.getCategory().getMetadata().getName())
                                    .toList())
                            .containsExactly("root", "missing-parent", "self-parent", "cyclic-a", "cyclic-b");
                    var cyclicANode = tree.stream()
                            .filter(node ->
                                    node.getCategory().getMetadata().getName().equals("cyclic-a"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(cyclicANode.getChildren())
                            .extracting(node -> node.getCategory().getMetadata().getName())
                            .containsExactly("child");
                })
                .verifyComplete();
    }

    @Test
    void updatePositionMovesCategoryBeforeSiblingAndPersistsChangedCategoriesOnly() {
        var parent = category("parent", null, 0, "2024-01-01T00:00:00Z");
        var child = category("child", "parent", 0, "2024-01-02T00:00:00Z");
        var sibling = category("sibling", "parent", 1, "2024-01-03T00:00:00Z");
        var moved = category("moved", "parent", 2, "2024-01-04T00:00:00Z");

        mockList(parent, child, sibling, moved);
        when(client.update(any(Category.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        service.updatePosition("moved", new CategoryPositionRequest("parent", "sibling"))
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    var parentNode = tree.get(0);
                    assertThat(parentNode.getChildren())
                            .extracting(node -> node.getCategory().getMetadata().getName())
                            .containsExactly("child", "moved", "sibling");
                    assertThat(moved.getSpec().getParent()).isEqualTo("parent");
                    assertThat(moved.getSpec().getPriority()).isEqualTo(1);
                    assertThat(sibling.getSpec().getPriority()).isEqualTo(2);
                })
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(2)).update(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(category -> category.getMetadata().getName())
                .containsExactly("sibling", "moved");
    }

    @Test
    void updatePositionMovesCategoryToRootAndRecomputesOriginalSiblings() {
        var root = category("root", null, 0, "2024-01-01T00:00:00Z");
        var rootSibling = category("root-sibling", null, 1, "2024-01-02T00:00:00Z");
        var child = category("child", "root", 0, "2024-01-03T00:00:00Z");
        var childSibling = category("child-sibling", "root", 1, "2024-01-04T00:00:00Z");

        mockList(root, rootSibling, child, childSibling);
        when(client.update(any(Category.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        service.updatePosition("child", new CategoryPositionRequest(null, "root-sibling"))
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    assertThat(tree)
                            .extracting(node -> node.getCategory().getMetadata().getName())
                            .containsExactly("root", "child", "root-sibling");
                    assertThat(child.getSpec().getParent()).isNull();
                    assertThat(child.getSpec().getPriority()).isEqualTo(1);
                    assertThat(rootSibling.getSpec().getPriority()).isEqualTo(2);
                    assertThat(childSibling.getSpec().getParent()).isEqualTo("root");
                    assertThat(childSibling.getSpec().getPriority()).isZero();
                })
                .verifyComplete();
    }

    @Test
    void updatePositionAppendsCategoryToTargetSiblings() {
        var root = category("root", null, 0, "2024-01-01T00:00:00Z");
        var moved = category("moved", null, 1, "2024-01-02T00:00:00Z");
        var child = category("child", "root", 0, "2024-01-03T00:00:00Z");

        mockList(root, moved, child);
        when(client.update(any(Category.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        service.updatePosition("moved", new CategoryPositionRequest("root", null))
                .as(StepVerifier::create)
                .assertNext(tree -> {
                    assertThat(tree)
                            .extracting(node -> node.getCategory().getMetadata().getName())
                            .containsExactly("root");
                    assertThat(tree.get(0).getChildren())
                            .extracting(node -> node.getCategory().getMetadata().getName())
                            .containsExactly("child", "moved");
                    assertThat(moved.getSpec().getParent()).isEqualTo("root");
                    assertThat(moved.getSpec().getPriority()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void updatePositionRejectsMissingCategory() {
        mockList(category("root", null, 0, "2024-01-01T00:00:00Z"));

        service.updatePosition("missing", new CategoryPositionRequest(null, null))
                .as(StepVerifier::create)
                .expectError(NotFoundException.class)
                .verify();

        verify(client, never()).update(any(Category.class));
    }

    @Test
    void updatePositionRejectsInvalidParent() {
        mockList(category("root", null, 0, "2024-01-01T00:00:00Z"));

        service.updatePosition("root", new CategoryPositionRequest("missing", null))
                .as(StepVerifier::create)
                .expectError(ServerWebInputException.class)
                .verify();

        verify(client, never()).update(any(Category.class));
    }

    @Test
    void updatePositionRejectsBeforeNameOutsideTargetSiblings() {
        var root = category("root", null, 0, "2024-01-01T00:00:00Z");
        var child = category("child", "root", 0, "2024-01-02T00:00:00Z");
        var rootSibling = category("root-sibling", null, 1, "2024-01-03T00:00:00Z");

        mockList(root, child, rootSibling);

        service.updatePosition("child", new CategoryPositionRequest("root", "root-sibling"))
                .as(StepVerifier::create)
                .expectError(ServerWebInputException.class)
                .verify();

        verify(client, never()).update(any(Category.class));
    }

    @Test
    void updatePositionRejectsCycle() {
        var root = category("root", null, 0, "2024-01-01T00:00:00Z");
        var child = category("child", "root", 0, "2024-01-02T00:00:00Z");

        mockList(root, child);

        service.updatePosition("root", new CategoryPositionRequest("child", null))
                .as(StepVerifier::create)
                .expectError(ServerWebInputException.class)
                .verify();

        verify(client, never()).update(any(Category.class));
    }

    private void mockList(Category... categories) {
        when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(List.of(categories)));
    }

    private static Category category(String name, String parentName, int priority, String creationTimestamp) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setCreationTimestamp(Instant.parse(creationTimestamp));
        category.setMetadata(metadata);

        var spec = new Category.CategorySpec();
        spec.setDisplayName(name);
        spec.setSlug(name);
        spec.setPriority(priority);
        spec.setParent(parentName);
        category.setSpec(spec);
        return category;
    }
}
