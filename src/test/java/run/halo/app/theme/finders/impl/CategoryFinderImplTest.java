package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.finders.vo.CategoryVo;

/**
 * Tests for {@link CategoryFinderImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryFinderImplTest {

    @Mock
    private ReactiveExtensionClient client;

    private CategoryFinderImpl categoryFinder;

    @BeforeEach
    void setUp() {
        categoryFinder = new CategoryFinderImpl(client);
    }

    @Test
    void getByName() throws JSONException {
        when(client.fetch(eq(Category.class), eq("hello")))
            .thenReturn(Mono.just(category()));
        CategoryVo categoryVo = categoryFinder.getByName("hello");
        JSONAssert.assertEquals("""
                {
                    "name": "hello",
                    "displayName": "displayName-1",
                    "slug": "slug-1",
                    "description": "description-1",
                    "cover": "cover-1",
                    "template": "template-1",
                    "priority": 0,
                    "children": [
                        "C1",
                        "C2"
                    ],
                    "permalink": null,
                    "posts": null
                }
                """,
            JsonUtils.objectToJson(categoryVo),
            true);
    }

    @Test
    void list() {
        ListResult<Category> categories = new ListResult<>(1, 10, 3,
            categories().stream()
                .sorted(CategoryFinderImpl.defaultComparator())
                .toList());
        when(client.list(eq(Category.class), eq(null), any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(categories));
        ListResult<CategoryVo> list = categoryFinder.list(1, 10);
        assertThat(list.getItems()).hasSize(3);
        assertThat(list.get().map(CategoryVo::getName).toList())
            .isEqualTo(List.of("c3", "c2", "hello"));
    }

    private List<Category> categories() {
        Category category2 = JsonUtils.deepCopy(category());
        category2.getMetadata().setName("c2");
        category2.getSpec().setPriority(2);

        Category category3 = JsonUtils.deepCopy(category());
        category3.getMetadata().setName("c3");
        category3.getMetadata().setCreationTimestamp(Instant.now().plusSeconds(20));
        category3.getSpec().setPriority(2);
        return List.of(category2, category(), category3);
    }

    private Category category() {
        final Category category = new Category();

        Metadata metadata = new Metadata();
        metadata.setName("hello");
        metadata.setAnnotations(Map.of("K1", "V1"));
        metadata.setCreationTimestamp(Instant.now());
        category.setMetadata(metadata);

        Category.CategorySpec categorySpec = new Category.CategorySpec();
        categorySpec.setSlug("slug-1");
        categorySpec.setDisplayName("displayName-1");
        categorySpec.setCover("cover-1");
        categorySpec.setDescription("description-1");
        categorySpec.setTemplate("template-1");
        categorySpec.setPriority(0);
        categorySpec.setChildren(List.of("C1", "C2"));
        category.setSpec(categorySpec);
        return category;
    }
}