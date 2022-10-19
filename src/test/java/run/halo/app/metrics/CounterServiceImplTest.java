package run.halo.app.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link CounterServiceImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CounterServiceImplTest {

    @Mock
    private ReactiveExtensionClient client;
    private CounterServiceImpl counterService;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
        counterService = new CounterServiceImpl(simpleMeterRegistry, client);
        String counterName = MeterUtils.nameOf(Post.class, "fake-post");
        MeterUtils.visitCounter(simpleMeterRegistry,
            counterName).increment();

        MeterUtils.approvedCommentCounter(simpleMeterRegistry, counterName)
            .increment(2);

        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        counter.getMetadata().setName(counterName);
        counter.setVisit(2);

        lenient().when(client.delete(any())).thenReturn(Mono.just(counter));
        lenient().when(client.fetch(eq(Counter.class), eq(counterName)))
            .thenReturn(Mono.just(counter));
    }

    @Test
    void getByName() {
        run.halo.app.core.extension.Counter counter =
            counterService.getByName(MeterUtils.nameOf(Post.class, "fake-post"));
        assertThat(counter.getVisit()).isEqualTo(1);
        assertThat(counter.getUpvote()).isEqualTo(0);
        assertThat(counter.getTotalComment()).isEqualTo(0);
        assertThat(counter.getApprovedComment()).isEqualTo(2);
    }

    @Test
    void deleteByName() {
        String counterName = MeterUtils.nameOf(Post.class, "fake-post");
        counterService.deleteByName(counterName)
            .as(StepVerifier::create)
            .consumeNextWith(counter -> {
                verify(client, times(1)).delete(any(Counter.class));
                assertThat(counterService.getByName(counterName).getVisit()).isEqualTo(0);
            })
            .verifyComplete();
    }
}