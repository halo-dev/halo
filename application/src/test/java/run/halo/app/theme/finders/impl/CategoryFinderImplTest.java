package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.data.domain.Sort;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.finders.vo.CategoryTreeVo;
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

    @Mock
    private CategoryService categoryService;

    private CategoryFinderImpl categoryFinder;

    @BeforeEach
    void setUp() {
        categoryFinder = new CategoryFinderImpl(client, categoryService);
        lenient().when(categoryService.isCategoryHidden(any())).thenReturn(Mono.just(false));
    }

    @Test
    void getByName() throws JSONException {
        when(client.fetch(eq(Category.class), eq("hello")))
            .thenReturn(Mono.just(category()));
        CategoryVo categoryVo = categoryFinder.getByName("hello").block();
        categoryVo.getMetadata().setCreationTimestamp(null);
        JSONAssert.assertEquals("""
                {
                     "metadata": {
                         "name": "hello",
                         "annotations": {
                             "K1": "V1"
                         }
                     },
                     "spec": {
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
                         "preventParentPostCascadeQuery": false,
                         "hideFromList": false
                     }
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
        when(client.listBy(eq(Category.class), any(ListOptions.class), any(PageRequest.class)))
            .thenReturn(Mono.just(categories));
        ListResult<CategoryVo> list = categoryFinder.list(1, 10).block();
        assertThat(list.getItems()).hasSize(3);
        assertThat(list.get().map(categoryVo -> categoryVo.getMetadata().getName()).toList())
            .isEqualTo(List.of("c3", "c2", "hello"));
    }

    @Test
    void listAsTree() {
        when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
            .thenReturn(Flux.fromIterable(categoriesForTree()));
        List<CategoryTreeVo> treeVos = categoryFinder.listAsTree().collectList().block();
        assertThat(treeVos).hasSize(1);
    }

    @Test
    void listSubTreeByName() {
        when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
            .thenReturn(Flux.fromIterable(categoriesForTree()));
        List<CategoryTreeVo> treeVos = categoryFinder.listAsTree("E").collectList().block();
        assertThat(treeVos.get(0).getMetadata().getName()).isEqualTo("E");
        assertThat(treeVos.get(0).getChildren()).hasSize(2);
        assertThat(treeVos.get(0).getChildren().get(0).getMetadata().getName()).isEqualTo("A");
        assertThat(treeVos.get(0).getChildren().get(1).getMetadata().getName()).isEqualTo("C");
    }

    /**
     * Test for {@link CategoryFinderImpl#listAsTree()}.
     *
     * @see <a href="https://github.com/halo-dev/halo/issues/2532">Fix #2532</a>
     */
    @Test
    void listAsTreeMore() {
        when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
            .thenReturn(Flux.fromIterable(moreCategories()));
        List<CategoryTreeVo> treeVos = categoryFinder.listAsTree().collectList().block();
        String s = visualizeTree(treeVos);
        assertThat(s).isEqualTo("""
            全部 (7)
            ├── FIT2CLOUD (4)
            │   ├── DataEase (0)
            │   ├── Halo (2)
            │   ├── MeterSphere (0)
            │   └── JumpServer (0)
            └── 默认分类 (3)
            """);
    }

    @Nested
    class CategoryPostCountTest {

        /**
         * <p>Structure below.</p>
         * <pre>
         * 全部 (35)
         * ├── FIT2CLOUD (15)
         * │   ├── DataEase (10)
         * │   │   ├── SubNode1 (4)
         * │   │   │   ├── Leaf1 (2)
         * │   │   │   ├── Leaf2 (2)
         * │   │   ├── SubNode2 (6)  (independent)
         * │   │       ├── IndependentChild1 (3)
         * │   │       ├── IndependentChild2 (3)
         * │   ├── IndependentNode (5)  (independent)
         * │       ├── IndependentChild3 (2)
         * │       ├── IndependentChild4 (3)
         * ├── AnotherRootChild (20)
         * │   ├── Child1 (8)
         * │   │   ├── SubChild1 (3)
         * │   │   │   ├── DeepNode1 (1)
         * │   │   │   ├── DeepNode2 (1)
         * │   │   │   │   ├── DeeperNode (1)
         * │   │   ├── SubChild2 (5)
         * │   │       ├── DeepNode3 (2)  (independent)
         * │   │           ├── DeepNode4 (1)
         * │   │           ├── DeepNode5 (1)
         * │   ├── Child2 (12)
         * │       ├── IndependentSubNode (12)  (independent)
         * │           ├── SubNode3 (6)
         * │           ├── SubNode4 (6)
         * </pre>
         */
        private List<Category> categories;

        @BeforeEach
        void setUp() throws IOException {
            var file = ResourceUtils.getFile("classpath:categories/independent-post-count.json");
            var json = Files.readString(file.toPath());
            categories = JsonUtils.jsonToObject(json, new TypeReference<>() {
            });
        }

        @Test
        void computePostCountFromTree() {
            when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(categories));
            var treeVos = categoryFinder.listAsTree("全部")
                .collectList().block();
            assertThat(treeVos).hasSize(1);
            String s = visualizeTree(treeVos.get(0).getChildren());
            assertThat(s).isEqualTo("""
                全部 (84)
                ├── AnotherRootChild (51)
                │   ├── Child1 (19)
                │   │   ├── SubChild1 (6)
                │   │   │   ├── DeepNode1 (1)
                │   │   │   └── DeepNode2 (2)
                │   │   │       └── DeeperNode (1)
                │   │   └── SubChild2 (5)
                │   │       └── DeepNode3 (4) (Independent)
                │   │           ├── DeepNode4 (1)
                │   │           └── DeepNode5 (1)
                │   └── Child2 (12)
                │       └── IndependentSubNode (24) (Independent)
                │           ├── SubNode3 (6)
                │           └── SubNode4 (6)
                └── FIT2CLOUD (33)
                    ├── DataEase (18)
                    │   ├── SubNode1 (8)
                    │   │   ├── Leaf1 (2)
                    │   │   └── Leaf2 (2)
                    │   └── SubNode2 (12) (Independent)
                    │       ├── IndependentChild1 (3)
                    │       └── IndependentChild2 (3)
                    └── IndependentNode (10) (Independent)
                        ├── IndependentChild3 (2)
                        └── IndependentChild4 (3)
                """);
        }

        @Test
        void getBreadcrumbsTest() {
            when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(categories));
            // first level
            var breadcrumbs = categoryFinder.getBreadcrumbs("全部").collectList().block();
            assertThat(toNames(breadcrumbs)).containsSequence("全部");

            // second level
            breadcrumbs = categoryFinder.getBreadcrumbs("AnotherRootChild").collectList().block();
            assertThat(toNames(breadcrumbs)).containsSequence("全部", "AnotherRootChild");

            // more levels
            breadcrumbs = categoryFinder.getBreadcrumbs("DeepNode5").collectList().block();
            assertThat(toNames(breadcrumbs)).containsSequence("全部", "AnotherRootChild", "Child1",
                "SubChild2", "DeepNode3", "DeepNode5");

            breadcrumbs = categoryFinder.getBreadcrumbs("IndependentChild4").collectList().block();
            assertThat(toNames(breadcrumbs)).containsSequence("全部", "FIT2CLOUD",
                "IndependentNode",
                "IndependentChild4");

            breadcrumbs = categoryFinder.getBreadcrumbs("SubNode4").collectList().block();
            assertThat(toNames(breadcrumbs)).containsSequence("全部", "AnotherRootChild", "Child2",
                "IndependentSubNode", "SubNode4");

            // not exist
            breadcrumbs = categoryFinder.getBreadcrumbs("not-exist").collectList().block();
            assertThat(toNames(breadcrumbs)).isEmpty();
        }

        @Test
        void getBreadcrumbsForHiddenTest() {
            Map<String, Category> categoryMap = categories.stream()
                .collect(
                    Collectors.toMap(item -> item.getMetadata().getName(), Function.identity()));
            var category = categoryMap.get("IndependentNode");
            category.getSpec().setHideFromList(true);
            when(client.listAll(eq(Category.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.fromIterable(categoryMap.values()));

            when(categoryService.isCategoryHidden(eq("IndependentChild4")))
                .thenReturn(Mono.just(true));

            var breadcrumbs = categoryFinder.getBreadcrumbs("IndependentChild4")
                .collectList().block();
            assertThat(toNames(breadcrumbs)).containsSequence("全部", "FIT2CLOUD",
                "IndependentNode",
                "IndependentChild4");
        }

        static List<String> toNames(List<CategoryVo> categories) {
            if (categories == null) {
                return List.of();
            }
            return categories.stream()
                .map(category -> category.getMetadata().getName())
                .toList();
        }
    }

    private List<Category> categoriesForTree() {
        /*
         *  D
         *  ├── E
         *  │   ├── A
         *  │   │   └── B
         *  │   └── C
         *  └── G
         *  ├── F
         *      └── H
         */
        Category d = category();
        d.getMetadata().setName("D");
        d.getSpec().setChildren(List.of("E", "G", "F"));

        Category e = category();
        e.getMetadata().setName("E");
        e.getSpec().setChildren(List.of("A", "C"));

        Category a = category();
        a.getMetadata().setName("A");
        a.getSpec().setChildren(List.of("B"));

        Category b = category();
        b.getMetadata().setName("B");
        b.getSpec().setChildren(null);

        Category c = category();
        c.getMetadata().setName("C");
        c.getSpec().setChildren(null);

        Category g = category();
        g.getMetadata().setName("G");
        g.getSpec().setChildren(null);

        Category f = category();
        f.getMetadata().setName("F");
        f.getSpec().setChildren(List.of("H"));

        Category h = category();
        h.getMetadata().setName("H");
        h.getSpec().setChildren(null);
        return List.of(d, e, a, b, c, g, f, h);
    }

    /**
     * Visualize a tree.
     */
    String visualizeTree(List<CategoryTreeVo> categoryTreeVos) {
        Category.CategorySpec categorySpec = new Category.CategorySpec();
        categorySpec.setSlug("/");
        categorySpec.setDisplayName("全部");
        Integer postCount = categoryTreeVos.stream()
            .map(CategoryTreeVo::getPostCount)
            .filter(Objects::nonNull)
            .reduce(Integer::sum)
            .orElse(0);
        CategoryTreeVo root = CategoryTreeVo.builder()
            .spec(categorySpec)
            .postCount(postCount)
            .children(categoryTreeVos)
            .metadata(new Metadata())
            .build();
        StringBuilder stringBuilder = new StringBuilder();
        root.print(stringBuilder, "", "");
        return stringBuilder.toString();
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

    private List<Category> moreCategories() {
        // see also https://github.com/halo-dev/halo/issues/2643
        String s = """
            [
               {
                  "spec":{
                     "displayName":"默认分类",
                     "slug":"default",
                     "description":"这是你的默认分类，如不需要，删除即可。",
                     "cover":"",
                     "template":"",
                     "priority":1,
                     "children":[
                     ]
                  },
                  "status":{
                     "permalink":"/categories/default",
                     "postCount":3,
                     "visiblePostCount":3
                  },
                  "apiVersion":"content.halo.run/v1alpha1",
                  "kind":"Category",
                  "metadata":{
                     "name":"76514a40-6ef1-4ed9-b58a-e26945bde3ca",
                     "version":16,
                     "creationTimestamp":"2022-10-08T06:17:47.589181Z"
                  }
               },
               {
                  "spec":{
                     "displayName":"MeterSphere",
                     "slug":"metersphere",
                     "description":"",
                     "cover":"",
                     "template":"",
                     "priority":2,
                     "children":[
                     ]
                  },
                  "status":{
                     "permalink":"/categories/metersphere",
                     "postCount":0,
                     "visiblePostCount":0
                  },
                  "apiVersion":"content.halo.run/v1alpha1",
                  "kind":"Category",
                  "metadata":{
                     "finalizers":[
                        "category-protection"
                     ],
                     "name":"acf09686-d5a7-4227-ba8c-3aeff063f12f",
                     "version":13,
                     "creationTimestamp":"2022-10-08T06:32:36.650974Z"
                  }
               },
               {
                  "spec":{
                     "displayName":"DataEase",
                     "slug":"dataease",
                     "description":"",
                     "cover":"",
                     "template":"",
                     "priority":0,
                     "children":[
                     ]
                  },
                  "status":{
                     "permalink":"/categories/dataease",
                     "postCount":0,
                     "visiblePostCount":0
                  },
                  "apiVersion":"content.halo.run/v1alpha1",
                  "kind":"Category",
                  "metadata":{
                     "finalizers":[
                        "category-protection"
                     ],
                     "name":"bd95f914-22fc-4de5-afcc-a9ffba2f6401",
                     "version":13,
                     "creationTimestamp":"2022-10-08T06:32:53.353838Z"
                  }
               },
               {
                  "spec":{
                     "displayName":"FIT2CLOUD",
                     "slug":"fit2cloud",
                     "description":"",
                     "cover":"",
                     "template":"",
                     "priority":0,
                     "children":[
                        "bd95f914-22fc-4de5-afcc-a9ffba2f6401",
                        "e1150fd9-4512-453c-9186-f8de9c156c3d",
                        "acf09686-d5a7-4227-ba8c-3aeff063f12f",
                        "ed064d5e-2b6f-4123-8114-78d0c6f2c4e2",
                        "non-existent-children-name"
                     ]
                  },
                  "status":{
                     "permalink":"/categories/fit2cloud",
                     "postCount":2,
                     "visiblePostCount":2
                  },
                  "apiVersion":"content.halo.run/v1alpha1",
                  "kind":"Category",
                  "metadata":{
                     "finalizers":[
                        "category-protection"
                     ],
                     "name":"c25c17ae-4a7b-43c5-a424-76950b9622cd",
                     "version":14,
                     "creationTimestamp":"2022-10-08T06:32:27.802025Z"
                  }
               },
               {
                  "spec":{
                     "displayName":"Halo",
                     "slug":"halo",
                     "description":"",
                     "cover":"",
                     "template":"",
                     "priority":1,
                     "children":[
                     ]
                  },
                  "status":{
                     "permalink":"/categories/halo",
                     "postCount":2,
                     "visiblePostCount":2
                  },
                  "apiVersion":"content.halo.run/v1alpha1",
                  "kind":"Category",
                  "metadata":{
                     "finalizers":[
                        "category-protection"
                     ],
                     "name":"e1150fd9-4512-453c-9186-f8de9c156c3d",
                     "version":15,
                     "creationTimestamp":"2022-10-08T06:32:42.991788Z"
                  }
               },
               {
                  "spec":{
                     "displayName":"JumpServer",
                     "slug":"jumpserver",
                     "description":"",
                     "cover":"",
                     "template":"",
                     "priority":3,
                     "children":[
                     ]
                  },
                  "status":{
                     "permalink":"/categories/jumpserver",
                     "postCount":0,
                     "visiblePostCount":0
                  },
                  "apiVersion":"content.halo.run/v1alpha1",
                  "kind":"Category",
                  "metadata":{
                     "finalizers":[
                        "category-protection"
                     ],
                     "name":"ed064d5e-2b6f-4123-8114-78d0c6f2c4e2",
                     "version":13,
                     "creationTimestamp":"2022-10-08T06:33:00.557435Z"
                  }
               }
            ]
            """;
        return JsonUtils.jsonToObject(s, new TypeReference<>() {
        });
    }
}
