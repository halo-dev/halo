package run.halo.app.core.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler.Request;

@ExtendWith(MockitoExtension.class)
class CategoryReconcilerTest {

    @Mock
    private ExtensionClient client;

    @Mock
    private CategoryPermalinkPolicy categoryPermalinkPolicy;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CategoryReconciler reconciler;

    @BeforeEach
    void setUp() {
        reconciler = new CategoryReconciler(client, categoryPermalinkPolicy, categoryService, eventPublisher);
    }

    @Test
    void shouldPropagateHiddenStateThroughParentReferenceDescendants() {
        var parent = category("parent", null);
        var child = category("child", "parent");
        child.getSpec().setPreventParentPostCascadeQuery(true);
        var grandChild = category("grand-child", "child");

        when(client.fetch(Category.class, "parent")).thenReturn(Optional.of(parent));
        when(categoryPermalinkPolicy.pattern()).thenReturn("/categories/{slug}");
        when(categoryPermalinkPolicy.permalink(parent)).thenReturn("/categories/parent");
        when(categoryService.isCategoryHidden("parent")).thenReturn(Mono.just(true));
        when(categoryService.listDescendants("parent")).thenReturn(Flux.just(child, grandChild));

        reconciler.reconcile(new Request("parent"));

        assertThat(parent.getSpec().isHideFromList()).isTrue();
        assertThat(child.getSpec().isHideFromList()).isTrue();
        assertThat(grandChild.getSpec().isHideFromList()).isTrue();
        verify(client).update(parent);
        verify(client).update(child);
        verify(client).update(grandChild);
        verify(categoryService).listDescendants("parent");
    }

    private static Category category(String name, String parentName) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        category.setMetadata(metadata);
        var spec = new Category.CategorySpec();
        spec.setDisplayName(name);
        spec.setSlug(name);
        spec.setPriority(0);
        spec.setParent(parentName);
        category.setSpec(spec);
        return category;
    }
}
