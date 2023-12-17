package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.content.TestPost;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;

/**
 * Tests for {@link CategoryReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryReconcilerTest {
    @Mock
    private ExtensionClient client;

    @Mock
    private CategoryPermalinkPolicy categoryPermalinkPolicy;

    @InjectMocks
    private CategoryReconciler categoryReconciler;

    @Test
    void reconcileStatusPostForCategoryA() throws JSONException {
        reconcileStatusPostPilling("category-A");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatusOrDefault().getPostCount()).isEqualTo(4);
        assertThat(
            captor.getAllValues().get(1).getStatusOrDefault().getVisiblePostCount()).isEqualTo(0);
    }

    @Test
    void reconcileStatusPostForCategoryB() throws JSONException {
        reconcileStatusPostPilling("category-B");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        Category category = captor.getAllValues().get(1);
        assertThat(category.getStatusOrDefault().getPostCount()).isEqualTo(3);
        assertThat(category.getStatusOrDefault().getVisiblePostCount()).isEqualTo(0);
    }

    @Test
    void reconcileStatusPostForCategoryC() throws JSONException {
        reconcileStatusPostPilling("category-C");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatusOrDefault().getPostCount()).isEqualTo(2);
        assertThat(
            captor.getAllValues().get(1).getStatusOrDefault().getVisiblePostCount()).isEqualTo(0);
    }

    @Test
    void reconcileStatusPostForCategoryD() throws JSONException {
        reconcileStatusPostPilling("category-D");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(client, times(3)).update(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatusOrDefault().postCount).isEqualTo(1);
        assertThat(captor.getAllValues().get(1).getStatusOrDefault().visiblePostCount).isEqualTo(0);
    }


    private void reconcileStatusPostPilling(String reconcileCategoryName) {
        categories().forEach(category -> {
            lenient().when(client.fetch(eq(Category.class), eq(category.getMetadata().getName())))
                .thenReturn(Optional.of(category));
        });

        lenient().when(client.list(eq(Post.class), any(), any()))
            .thenReturn(posts());
        lenient().when(client.list(eq(Category.class), any(), any()))
            .thenReturn(categories());

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
        category.setSpec(new Category.CategorySpec());
        category.setStatus(new Category.CategoryStatus());
        return category;
    }

    private List<Post> posts() {
        /*
         * |-A(post-4)
         *   |-B(post-3)
         *   |-|-C(post-2,post-1)
         *   |-D(post-1)
         */
        Post post1 = TestPost.postV1();
        post1.getMetadata().setName("post-1");
        post1.getSpec().setCategories(List.of("category-D", "category-C"));
        post1.getSpec().setVisible(Post.VisibleEnum.PUBLIC);

        Post post2 = TestPost.postV1();
        post2.getMetadata().setName("post-2");
        post2.getSpec().setCategories(List.of("category-C"));
        post2.getSpec().setVisible(Post.VisibleEnum.PUBLIC);

        Post post3 = TestPost.postV1();
        post3.getMetadata().setName("post-3");
        post3.getSpec().setCategories(List.of("category-B"));
        post3.getSpec().setVisible(Post.VisibleEnum.PUBLIC);

        Post post4 = TestPost.postV1();
        post4.getMetadata().setName("post-4");
        post4.getSpec().setCategories(List.of("category-A"));
        post4.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        return List.of(post1, post2, post3, post4);
    }
}