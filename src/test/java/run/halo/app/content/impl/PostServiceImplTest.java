package run.halo.app.content.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import run.halo.app.content.PostQuery;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
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

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void listPredicate() {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.put("category", List.of("category1", "category2"));
        PostQuery postQuery = new PostQuery(multiValueMap);

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
        multiValueMap.put("tag", List.of("tag2"));
        post.getSpec().setTags(List.of("tag1"));
        post.getSpec().setCategories(null);
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isFalse();

        multiValueMap.put("tag", List.of());
        multiValueMap.remove("category");
        postQuery = new PostQuery(multiValueMap);
        post.getSpec().setTags(List.of());
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();

        multiValueMap.put("labelSelector", List.of("hello"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isFalse();

        post.getMetadata().setLabels(Map.of("hello", "world"));
        test = postService.postListPredicate(postQuery).test(post);
        assertThat(test).isTrue();
    }

    @Test
    void draftPost() {

    }
}