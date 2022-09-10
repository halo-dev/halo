package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.CategoryService;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class PostCategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostCategoryServiceImpl postCategoryService;

    @Autowired
    private PostServiceImpl postService;

    @BeforeAll
    public void setUp() {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("分类-1");
        category1.setSlug("category-1");

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("分类-1-1");
        category2.setSlug("category-1-1");
        category2.setParentId(1);

        Category category3 = new Category();
        category3.setId(3);
        category3.setName("分类-1-1-1");
        category3.setSlug("category-1-1-1");
        category3.setParentId(2);

        Category category4 = new Category();
        category4.setId(4);
        category4.setName("分类-1-1-2");
        category4.setSlug("category-1-1-2");
        category4.setParentId(2);

        categoryService.create(category1);
        categoryService.create(category2);
        categoryService.create(category3);
        categoryService.create(category4);

        Post post1 = new Post();
        post1.setId(1);
        post1.setTitle("post1");
        post1.setSlug("post1");

        Post post2 = new Post();
        post2.setId(2);
        post2.setTitle("post2");
        post2.setSlug("post2");
        post2.setStatus(PostStatus.PUBLISHED);

        Post post3 = new Post();
        post3.setId(3);
        post3.setTitle("post3");
        post3.setSlug("post3");
        post3.setStatus(PostStatus.PUBLISHED);

        Post post4 = new Post();
        post4.setId(4);
        post4.setTitle("post4");
        post4.setSlug("post4");

        postService.create(post1);
        postService.create(post2);
        postService.create(post3);
        postService.create(post4);

        PostCategory postCategory1 = new PostCategory();
        postCategory1.setCategoryId(1);
        postCategory1.setPostId(1);

        PostCategory postCategory2 = new PostCategory();
        postCategory2.setCategoryId(2);
        postCategory2.setPostId(2);

        PostCategory postCategory3 = new PostCategory();
        postCategory3.setCategoryId(3);
        postCategory3.setPostId(3);

        PostCategory postCategory4 = new PostCategory();
        postCategory4.setCategoryId(4);
        postCategory4.setPostId(4);

        postCategoryService.create(postCategory1);
        postCategoryService.create(postCategory2);
        postCategoryService.create(postCategory3);
        postCategoryService.create(postCategory4);
    }

    @Test
    void listPostByCategoryIdTest() {
        List<Post> posts = postCategoryService.listPostBy(1);
        assertEquals(List.of(postService.getById(1), postService.getById(2), postService.getById(3),
            postService.getById(4)), posts);
    }

    @Test
    void listPostByCategoryIdAndStatusTest() {
        List<Post> posts = postCategoryService.listPostBy(1, PostStatus.PUBLISHED);
        assertEquals(List.of(postService.getById(2), postService.getById(3)), posts);
    }

    @Test
    void listPostByCategoryIdAndStatusesTest() {
        List<Post> posts =
            postCategoryService.listPostBy(1, Set.of(PostStatus.PUBLISHED, PostStatus.DRAFT));
        assertEquals(List.of(postService.getById(1), postService.getById(2), postService.getById(3),
            postService.getById(4)), posts);
    }

    @Test
    void listPostBySlugIdAndStatusTest() {
        List<Post> posts = postCategoryService.listPostBy("category-1", PostStatus.PUBLISHED);
        assertEquals(List.of(postService.getById(2), postService.getById(3)), posts);
    }

    @Test
    void listPostBySlugIdAndStatusesTest() {
        List<Post> posts = postCategoryService.listPostBy("category-1",
            Set.of(PostStatus.PUBLISHED, PostStatus.DRAFT));
        assertEquals(List.of(postService.getById(1), postService.getById(2), postService.getById(3),
            postService.getById(4)), posts);
    }
}