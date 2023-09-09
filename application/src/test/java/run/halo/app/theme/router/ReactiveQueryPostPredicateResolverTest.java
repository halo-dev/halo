package run.halo.app.theme.router;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link ReactiveQueryPostPredicateResolver}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(SpringExtension.class)
class ReactiveQueryPostPredicateResolverTest {

    private ReactiveQueryPostPredicateResolver postPredicateResolver;

    @BeforeEach
    void setUp() {
        postPredicateResolver = new DefaultQueryPostPredicateResolver();
    }

    @Test
    void getPredicateWithoutAuth() {
        postPredicateResolver.getPredicate()
            .as(StepVerifier::create)
            .consumeNextWith(predicate -> {
                Post post = new Post();
                post.setMetadata(new Metadata());
                post.getMetadata().setName("fake-post");

                post.setSpec(new Post.PostSpec());
                post.getSpec().setDeleted(false);
                post.getMetadata().setLabels(Map.of(Post.PUBLISHED_LABEL, "true"));
                post.getSpec().setVisible(Post.VisibleEnum.PRIVATE);
                assertThat(predicate.test(post)).isFalse();

                post.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
                assertThat(predicate.test(post)).isTrue();

                post.getMetadata().setLabels(Map.of(Post.PUBLISHED_LABEL, "false"));
                assertThat(predicate.test(post)).isFalse();
            })
            .verifyComplete();
    }

    @Test
    @WithMockUser(username = "halo")
    void getPredicateWithAuth() {
        postPredicateResolver.getPredicate()
            .as(StepVerifier::create)
            .consumeNextWith(predicate -> {
                Post post = new Post();
                post.setMetadata(new Metadata());
                post.getMetadata().setName("fake-post");

                post.setSpec(new Post.PostSpec());
                post.getSpec().setDeleted(false);
                post.getSpec().setOwner("halo");
                post.getMetadata().setLabels(Map.of(Post.PUBLISHED_LABEL, "true"));
                post.getSpec().setVisible(Post.VisibleEnum.PRIVATE);
                assertThat(predicate.test(post)).isTrue();

                post.getSpec().setOwner("guqing");
                assertThat(predicate.test(post)).isFalse();

                post.getSpec().setOwner("halo");
                post.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
                assertThat(predicate.test(post)).isTrue();

                post.getSpec().setDeleted(true);
                assertThat(predicate.test(post)).isFalse();

                post.getSpec().setVisible(Post.VisibleEnum.INTERNAL);
                assertThat(predicate.test(post)).isFalse();
            })
            .verifyComplete();
    }
}