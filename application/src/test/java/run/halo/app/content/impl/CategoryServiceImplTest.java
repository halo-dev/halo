package run.halo.app.content.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private ReactiveExtensionClient client;

    private CategoryServiceImpl categoryService;

    private Map<String, Category> categories;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(client);
    }

    @Test
    void listChildrenReturnsSelfAndDescendantsButStopsAtIndependentSubtree() {
        mockCategories(
                category("root", null, false, false),
                category("child", "root", false, false),
                category("grandchild", "child", false, false),
                category("independent", "root", true, false),
                category("independent-child", "independent", false, false));

        categoryService
                .listChildren("root")
                .map(category -> category.getMetadata().getName())
                .as(StepVerifier::create)
                .expectNext("root", "child", "grandchild")
                .verifyComplete();
    }

    @Test
    void listDescendantsDoesNotStopAtIndependentSubtree() {
        mockCategories(
                category("root", null, false, false),
                category("child", "root", false, false),
                category("grandchild", "child", false, false),
                category("independent", "root", true, false),
                category("independent-child", "independent", false, false));

        categoryService
                .listDescendants("root")
                .map(category -> category.getMetadata().getName())
                .as(StepVerifier::create)
                .expectNext("child", "grandchild", "independent", "independent-child")
                .verifyComplete();
    }

    @Test
    void getParentByNameReadsTargetParentReference() {
        mockCategories(category("root", null, false, false), category("child", "root", false, false));

        categoryService
                .getParentByName("child")
                .map(category -> category.getMetadata().getName())
                .as(StepVerifier::create)
                .expectNext("root")
                .verifyComplete();
    }

    @Test
    void getParentByNameReturnsEmptyWhenParentReferenceIsMissing() {
        mockCategories(category("child", "missing", false, false));

        categoryService.getParentByName("child").as(StepVerifier::create).verifyComplete();
    }

    @Test
    void isCategoryHiddenWalksParentReferences() {
        mockCategories(
                category("root", null, false, true),
                category("child", "root", false, false),
                category("grandchild", "child", false, false));

        categoryService
                .isCategoryHidden("grandchild")
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isCategoryHiddenReturnsFalseWhenParentReferenceIsMissing() {
        mockCategories(category("child", "missing", false, false));

        categoryService
                .isCategoryHidden("child")
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isCategoryHiddenStopsOnParentCycles() {
        mockCategories(category("A", "B", false, false), category("B", "A", false, false));

        categoryService
                .isCategoryHidden("A")
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    private void mockCategories(Category... values) {
        categories = new LinkedHashMap<>();
        for (Category category : values) {
            categories.put(category.getMetadata().getName(), category);
        }
        lenient()
                .when(client.fetch(eq(Category.class), anyString()))
                .thenAnswer(invocation -> Mono.justOrEmpty(categories.get(invocation.getArgument(1))));
        lenient()
                .when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
                .thenAnswer(invocation -> Flux.fromIterable(childrenOf(parentNameFrom(invocation.getArgument(1)))));
    }

    private List<Category> childrenOf(String parentName) {
        return categories.values().stream()
                .filter(category -> parentName.equals(category.getSpec().getParent()))
                .toList();
    }

    private static String parentNameFrom(ListOptions options) {
        var query = options.getFieldSelector().query();
        assertThat(recordComponentValue(query, "indexName")).isEqualTo("spec.parent");
        return (String) recordComponentValue(query, "key");
    }

    private static Object recordComponentValue(Object record, String name) {
        try {
            var method = record.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return method.invoke(record);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Category category(
            String name, String parentName, boolean preventParentPostCascadeQuery, boolean hideFromList) {
        var category = new Category();
        var metadata = new Metadata();
        metadata.setName(name);
        category.setMetadata(metadata);
        var spec = new Category.CategorySpec();
        spec.setDisplayName(name);
        spec.setSlug(name);
        spec.setPriority(0);
        spec.setParent(parentName);
        spec.setPreventParentPostCascadeQuery(preventParentPostCascadeQuery);
        spec.setHideFromList(hideFromList);
        category.setSpec(spec);
        return category;
    }
}
