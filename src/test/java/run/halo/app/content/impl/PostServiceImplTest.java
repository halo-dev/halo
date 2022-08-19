package run.halo.app.content.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.content.ContentService;
import run.halo.app.content.PostQuery;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link PostServiceImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {
    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private ContentService contentService;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(contentService, client);
    }

    @Test
    void listPredicate() {
        PostQuery postQuery = new PostQuery();
        postQuery.setCategories(Set.of("category1", "category2"));

        Post post = TestPost.postV1();
        post.getSpec().setTags(null);
        post.getStatusOrDefault().setContributors(null);
        post.getSpec().setCategories(List.of("category1"));
        boolean test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        post.getSpec().setTags(List.of("tag1"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        // Do not include tags
        postQuery.setTags(Set.of("tag2"));
        post.getSpec().setTags(List.of("tag1"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isFalse();

        postQuery.setTags(Set.of());
        post.getSpec().setTags(List.of());
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        postQuery.setLabelSelector(List.of("hello"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isFalse();

        postQuery.setLabelSelector(List.of("hello"));
        post.getMetadata().setLabels(Map.of("hello", "world"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();
    }
}