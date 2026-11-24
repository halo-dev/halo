package run.halo.app.theme.router.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.router.ModelConst;

/**
 * Tests for {@link RouteFactory}.
 *
 * @author guqing
 * @since 2.3.0
 */
@ExtendWith(MockitoExtension.class)
class RouteFactoryTest extends RouteFactoryTestSuite {

    @Test
    void configuredPageSize() {
        SystemSetting.Post post = new SystemSetting.Post();
        post.setPostPageSize(1);
        post.setArchivePageSize(2);
        post.setCategoryPageSize(3);
        post.setTagPageSize(null);
        post.setAuthorPageSize(4);
        when(environmentFetcher.fetchPost()).thenReturn(Mono.just(post));

        TestRouteFactory routeFactory = new TestRouteFactory();
        assertThat(routeFactory
                        .configuredPageSize(environmentFetcher, SystemSetting.Post::getTagPageSize)
                        .block())
                .isEqualTo(ModelConst.DEFAULT_PAGE_SIZE);

        assertThat(routeFactory
                        .configuredPageSize(environmentFetcher, SystemSetting.Post::getPostPageSize)
                        .block())
                .isEqualTo(post.getPostPageSize());

        assertThat(routeFactory
                        .configuredPageSize(environmentFetcher, SystemSetting.Post::getCategoryPageSize)
                        .block())
                .isEqualTo(post.getCategoryPageSize());

        assertThat(routeFactory
                        .configuredPageSize(environmentFetcher, SystemSetting.Post::getArchivePageSize)
                        .block())
                .isEqualTo(post.getArchivePageSize());

        assertThat(routeFactory
                        .configuredPageSize(environmentFetcher, SystemSetting.Post::getAuthorPageSize)
                        .block())
                .isEqualTo(post.getAuthorPageSize());
    }

    @Test
    void derivesOwnershipFromPublicPatterns() {
        var routeFactory = new TestRouteFactory();

        assertThat(routeFactory.publicPatterns("/posts/{slug}")).containsExactly("/posts/{slug}");
        assertThat(routeFactory.publicPatterns("/")).containsExactly("/");

        StepVerifier.create(routeFactory.claimedPrefixes("/posts/{slug}"))
                .expectNext(Set.of("posts"))
                .verifyComplete();
        StepVerifier.create(routeFactory.matches("/posts/{slug}", "/posts/hello", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(routeFactory.matches("/posts/{slug}", "/posts/hello", HttpMethod.POST))
                .expectNext(false)
                .verifyComplete();
    }

    static class TestRouteFactory implements RouteFactory {

        @Override
        public RouterFunction<ServerResponse> create(String pattern) {
            return null;
        }
    }
}
