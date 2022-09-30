package run.halo.app.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link CounterMeterHandler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CounterMeterHandlerTest {

    @Mock
    private ReactiveExtensionClient client;

    private final String counterName = MeterUtils.nameOf(Post.class, "fake-post");

    @InjectMocks
    private CounterMeterHandler counterMeterHandler;

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        counterMeterHandler = new CounterMeterHandler(client, meterRegistry);
    }

    @Test
    void onApplicationReady() {
        Counter counter = new Counter();
        counter.setMetadata(new Metadata());
        counter.getMetadata().setName(counterName);
        counter.setVisit(1);
        counter.setUpvote(3);
        counter.setTotalComment(5);
        counter.setApprovedComment(2);

        Mockito.when(client.list(eq(Counter.class), any(), any()))
            .thenReturn(Flux.just(counter));

        counterMeterHandler.onApplicationReady(Mockito.mock(ApplicationReadyEvent.class))
            .as(StepVerifier::create)
            .verifyComplete();

        assertThat((int) meterRegistry.find(counterName)
            .tag(MeterUtils.SCENE, value -> value.equals(MeterUtils.VISIT_SCENE))
            .counter().count()).isEqualTo(counter.getVisit());

        assertThat((int) meterRegistry.find(counterName)
            .tag(MeterUtils.SCENE, value -> value.equals(MeterUtils.UPVOTE_SCENE))
            .counter().count()).isEqualTo(counter.getUpvote());
        assertThat((int) meterRegistry.find(counterName)
            .tag(MeterUtils.SCENE, value -> value.equals(MeterUtils.TOTAL_COMMENT_SCENE))
            .counter().count()).isEqualTo(counter.getTotalComment());
        assertThat((int) meterRegistry.find(counterName)
            .tag(MeterUtils.SCENE, value -> value.equals(MeterUtils.APPROVED_COMMENT_SCENE))
            .counter().count()).isEqualTo(counter.getApprovedComment());
    }

    @Test
    void save() {
        MeterUtils.visitCounter(meterRegistry, counterName).increment(2);
        MeterUtils.upvoteCounter(meterRegistry, counterName).increment(3);

        Mockito.when(client.create(any()))
            .thenReturn(Mono.just(CounterMeterHandler.emptyCounter(counterName)));
        Mockito.when(client.fetch(eq(Counter.class), eq(counterName)))
            .thenReturn(Mono.empty());
        Mockito.when(client.update(any(Counter.class)))
            .thenAnswer(a -> {
                ArgumentCaptor<Counter> captor =
                    ArgumentCaptor.forClass(Counter.class);
                Mockito.verify(client, Mockito.times(1)).update(captor.capture());
                Counter value = captor.getValue();
                assertThat(value.getVisit()).isEqualTo(2);
                assertThat(value.getUpvote()).isEqualTo(3);
                assertThat(value.getTotalComment()).isEqualTo(0);
                assertThat(value.getApprovedComment()).isEqualTo(0);
                return Mono.just(value);
            });
        counterMeterHandler.save()
            .as(StepVerifier::create)
            .verifyComplete();
    }
}