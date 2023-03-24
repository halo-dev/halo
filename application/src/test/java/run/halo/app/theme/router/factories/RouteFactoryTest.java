package run.halo.app.theme.router.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemSetting;

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
        when(environmentFetcher.fetchPost()).thenReturn(Mono.just(post));

        TestRouteFactory routeFactory = new TestRouteFactory();
        assertThat(
            routeFactory.configuredPageSize(environmentFetcher, SystemSetting.Post::getTagPageSize)
                .block()).isEqualTo(ModelConst.DEFAULT_PAGE_SIZE);

        assertThat(
            routeFactory.configuredPageSize(environmentFetcher, SystemSetting.Post::getPostPageSize)
                .block()).isEqualTo(post.getPostPageSize());

        assertThat(
            routeFactory.configuredPageSize(environmentFetcher,
                SystemSetting.Post::getCategoryPageSize).block())
            .isEqualTo(post.getCategoryPageSize());

        assertThat(
            routeFactory.configuredPageSize(environmentFetcher,
                SystemSetting.Post::getArchivePageSize).block())
            .isEqualTo(post.getArchivePageSize());
    }

    static class TestRouteFactory implements RouteFactory {

        @Override
        public RouterFunction<ServerResponse> create(String pattern) {
            return null;
        }
    }
}