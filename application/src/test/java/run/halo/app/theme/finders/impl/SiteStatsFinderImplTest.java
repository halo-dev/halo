package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.CounterStats;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class SiteStatsFinderImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    CounterService counterService;

    SiteStatsFinderImpl finder;

    @BeforeEach
    void setUp() {
        finder = new SiteStatsFinderImpl(client, counterService);
    }

    @Test
    void shouldAggregateStatsFromCounterService() {
        when(counterService.getStats()).thenReturn(Mono.just(new CounterStats(100, 20, 15, 5)));
        var postResult = new ListResult<Post>(1, 1, 7, List.of());
        when(client.listBy(eq(Post.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(postResult));
        var categoryResult = new ListResult<Category>(1, 1, 3, List.of());
        when(client.listBy(eq(Category.class), any(ListOptions.class), any(PageRequest.class)))
                .thenReturn(Mono.just(categoryResult));

        StepVerifier.create(finder.getStats())
                .assertNext(stats -> {
                    assertThat(stats.getVisit()).isEqualTo(100);
                    assertThat(stats.getComment()).isEqualTo(15);
                    assertThat(stats.getUpvote()).isEqualTo(5);
                    assertThat(stats.getPost()).isEqualTo(7);
                    assertThat(stats.getCategory()).isEqualTo(3);
                })
                .verifyComplete();
    }
}
