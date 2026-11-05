package run.halo.app.core.counter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Counter;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class CounterServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    FakeTicker ticker;

    CounterServiceImpl counterService;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        counterService = new CounterServiceImpl(client, ticker);
    }

    @Test
    void shouldAggregateCounterStats() {
        when(client.listAll(eq(Counter.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenReturn(Flux.just(counter(1, 2, 3, 4), counter(10, null, 5, 6)));

        StepVerifier.create(counterService.getStats())
                .assertNext(stats -> {
                    assertThat(stats.visits()).isEqualTo(11);
                    assertThat(stats.comments()).isEqualTo(2);
                    assertThat(stats.approvedComments()).isEqualTo(8);
                    assertThat(stats.upvotes()).isEqualTo(10);
                })
                .verifyComplete();
    }

    @Test
    void shouldCacheStatsUntilExpired() {
        when(client.listAll(eq(Counter.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenReturn(Flux.just(counter(1, 1, 1, 1)));

        counterService.getStats().block();
        counterService.getStats().block();
        verify(client, times(1)).listAll(eq(Counter.class), any(ListOptions.class), eq(Sort.unsorted()));

        ticker.advance(Duration.ofSeconds(11));
        counterService.getStats().block();
        verify(client, times(2)).listAll(eq(Counter.class), any(ListOptions.class), eq(Sort.unsorted()));
    }

    @Test
    void shouldNotCancelSharedStatsLoadWhenSubscriberCancels() {
        Sinks.Many<Counter> counterSink = Sinks.many().unicast().onBackpressureBuffer();
        when(client.listAll(eq(Counter.class), any(ListOptions.class), eq(Sort.unsorted())))
                .thenReturn(counterSink.asFlux());

        var subscription = counterService.getStats().subscribe();
        subscription.dispose();

        assertThat(counterSink.tryEmitNext(counter(1, 2, 3, 4))).isEqualTo(Sinks.EmitResult.OK);
        assertThat(counterSink.tryEmitComplete()).isEqualTo(Sinks.EmitResult.OK);

        StepVerifier.create(counterService.getStats())
                .assertNext(stats -> {
                    assertThat(stats.visits()).isEqualTo(1);
                    assertThat(stats.comments()).isEqualTo(2);
                    assertThat(stats.approvedComments()).isEqualTo(3);
                    assertThat(stats.upvotes()).isEqualTo(4);
                })
                .verifyComplete();

        verify(client, times(1)).listAll(eq(Counter.class), any(ListOptions.class), eq(Sort.unsorted()));
    }

    static Counter counter(Integer visit, Integer totalComment, Integer approvedComment, Integer upvote) {
        var counter = Counter.emptyCounter("counter-" + System.nanoTime());
        counter.setVisit(visit);
        counter.setTotalComment(totalComment);
        counter.setApprovedComment(approvedComment);
        counter.setUpvote(upvote);
        return counter;
    }

    static class FakeTicker implements Ticker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }
}
