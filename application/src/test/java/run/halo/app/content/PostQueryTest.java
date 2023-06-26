package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.content.Post;

/**
 * Tests for {@link PostQuery}.
 *
 * @author guqing
 * @since 2.6.0
 */
@ExtendWith(MockitoExtension.class)
class PostQueryTest {

    @Test
    void toPredicate() {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.put("category", List.of("category1", "category2"));
        MockServerRequest request = MockServerRequest.builder()
            .queryParams(multiValueMap)
            .exchange(mock(ServerWebExchange.class))
            .build();
        PostQuery postQuery = new PostQuery(request);

        Post post = TestPost.postV1();
        post.getSpec().setTags(null);
        post.getStatusOrDefault().setContributors(null);
        post.getSpec().setCategories(List.of("category1"));
        boolean test = postQuery.toPredicate().test(post);
        assertThat(test).isTrue();

        post.getSpec().setTags(List.of("tag1"));
        test = postQuery.toPredicate().test(post);
        assertThat(test).isTrue();

        // Do not include tags
        multiValueMap.put("tag", List.of("tag2"));
        post.getSpec().setTags(List.of("tag1"));
        post.getSpec().setCategories(null);
        test = postQuery.toPredicate().test(post);
        assertThat(test).isFalse();

        multiValueMap.put("tag", List.of());
        multiValueMap.remove("category");
        request = MockServerRequest.builder()
            .exchange(mock(ServerWebExchange.class))
            .queryParams(multiValueMap).build();
        postQuery = new PostQuery(request);
        post.getSpec().setTags(List.of());
        test = postQuery.toPredicate().test(post);
        assertThat(test).isTrue();

        multiValueMap.put("labelSelector", List.of("hello"));
        test = postQuery.toPredicate().test(post);
        assertThat(test).isFalse();

        post.getMetadata().setLabels(Map.of("hello", "world"));
        test = postQuery.toPredicate().test(post);
        assertThat(test).isTrue();
    }
}
