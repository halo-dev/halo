package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.index.IndexerFactory;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

/**
 * Tests for {@link CategoryReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@DirtiesContext
@SpringBootTest
class CategoryReconcilerIntegrationTest {
    private final List<Post> storedPosts = posts();
    private final List<Category> storedCategories = categories();

    @Autowired
    private SchemeManager schemeManager;

    @SpyBean
    private ExtensionClient client;

    @Autowired
    private ReactiveExtensionClient reactiveClient;

    @Autowired
    private ReactiveExtensionStoreClient storeClient;

    @Autowired
    private IndexerFactory indexerFactory;

    @Autowired
    private CategoryReconciler categoryReconciler;

    Mono<Extension> deleteImmediately(Extension extension) {
        var name = extension.getMetadata().getName();
        var scheme = schemeManager.get(extension.getClass());
        // un-index
        var indexer = indexerFactory.getIndexer(extension.groupVersionKind());
        indexer.unIndexRecord(extension.getMetadata().getName());

        // delete from db
        var storeName = ExtensionStoreUtil.buildStoreName(scheme, name);
        return storeClient.delete(storeName, extension.getMetadata().getVersion())
            .thenReturn(extension);
    }

    @BeforeEach
    void setUp() {
        Flux.fromIterable(storedPosts)
            .flatMap(post -> reactiveClient.create(post))
            .as(StepVerifier::create)
            .expectNextCount(storedPosts.size())
            .verifyComplete();

        Flux.fromIterable(storedCategories)
            .flatMap(category -> reactiveClient.create(category))
            .as(StepVerifier::create)
            .expectNextCount(storedCategories.size())
            .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        Flux.fromIterable(storedPosts)
            .flatMap(this::deleteImmediately)
            .as(StepVerifier::create)
            .expectNextCount(storedPosts.size())
            .verifyComplete();

        Flux.fromIterable(storedCategories)
            .flatMap(this::deleteImmediately)
            .as(StepVerifier::create)
            .expectNextCount(storedCategories.size())
            .verifyComplete();
    }

    @Test
    void reconcileStatusPostForCategoryA() {
        reconcileStatusPostPilling("category-A");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        var values = captor.getAllValues();
        assertThat(values.get(2).getStatusOrDefault().getPostCount()).isEqualTo(4);
        assertThat(
            captor.getAllValues().get(2).getStatusOrDefault().getVisiblePostCount()).isEqualTo(0);
    }

    @Test
    void reconcileStatusPostForCategoryB() {
        reconcileStatusPostPilling("category-B");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        Category category = captor.getAllValues().get(2);
        assertThat(category.getStatusOrDefault().getPostCount()).isEqualTo(3);
        assertThat(category.getStatusOrDefault().getVisiblePostCount()).isEqualTo(0);
    }

    @Test
    void reconcileStatusPostForCategoryC() {
        reconcileStatusPostPilling("category-C");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        var value = captor.getAllValues().get(2);
        assertThat(value.getStatusOrDefault().getPostCount()).isEqualTo(2);
        assertThat(value.getStatusOrDefault().getVisiblePostCount()).isEqualTo(0);
    }

    @Test
    void reconcileStatusPostForCategoryD() {
        reconcileStatusPostPilling("category-D");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        var value = captor.getAllValues().get(2);
        assertThat(value.getStatusOrDefault().postCount).isEqualTo(1);
        assertThat(value.getStatusOrDefault().visiblePostCount).isEqualTo(0);
    }

    private void reconcileStatusPostPilling(String reconcileCategoryName) {
        Reconciler.Result result =
            categoryReconciler.reconcile(new Reconciler.Request(reconcileCategoryName));
        assertThat(result.reEnqueue()).isTrue();
        assertThat(result.retryAfter()).isEqualTo(Duration.ofMinutes(1));
    }

    private List<Category> categories() {
        /*
         * |-A(post-4)
         *   |-B(post-3)
         *   |-|-C(post-2,post-1)
         *   |-D(post-1)
         */
        Category categoryA = category("category-A");
        categoryA.getSpec().setChildren(List.of("category-B", "category-D"));

        Category categoryB = category("category-B");
        categoryB.getSpec().setChildren(List.of("category-C"));

        Category categoryC = category("category-C");
        Category categoryD = category("category-D");
        return List.of(categoryA, categoryB, categoryC, categoryD);
    }

    private Category category(String name) {
        Category category = new Category();
        Metadata metadata = new Metadata();
        metadata.setName(name);
        category.setMetadata(metadata);
        category.getMetadata().setFinalizers(Set.of(CategoryReconciler.FINALIZER_NAME));
        category.setSpec(new Category.CategorySpec());
        category.setStatus(new Category.CategoryStatus());

        category.getSpec().setDisplayName("display-name");
        category.getSpec().setSlug("slug");
        category.getSpec().setPriority(0);
        return category;
    }

    private List<Post> posts() {
        /*
         * |-A(post-4)
         *   |-B(post-3)
         *   |-|-C(post-2,post-1)
         *   |-D(post-1)
         */
        Post post1 = fakePost();
        post1.getMetadata().setName("post-1");
        post1.getSpec().setCategories(List.of("category-D", "category-C"));
        post1.getSpec().setVisible(Post.VisibleEnum.PUBLIC);

        Post post2 = fakePost();
        post2.getMetadata().setName("post-2");
        post2.getSpec().setCategories(List.of("category-C"));
        post2.getSpec().setVisible(Post.VisibleEnum.PUBLIC);

        Post post3 = fakePost();
        post3.getMetadata().setName("post-3");
        post3.getSpec().setCategories(List.of("category-B"));
        post3.getSpec().setVisible(Post.VisibleEnum.PUBLIC);

        Post post4 = fakePost();
        post4.getMetadata().setName("post-4");
        post4.getSpec().setCategories(List.of("category-A"));
        post4.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        return List.of(post1, post2, post3, post4);
    }

    Post fakePost() {
        var post = TestPost.postV1();
        post.getSpec().setAllowComment(true);
        post.getSpec().setDeleted(false);
        post.getSpec().setExcerpt(new Post.Excerpt());
        post.getSpec().getExcerpt().setAutoGenerate(false);
        post.getSpec().setPinned(false);
        post.getSpec().setPriority(0);
        post.getSpec().setPublish(false);
        post.getSpec().setSlug("fake-post");
        return post;
    }
}