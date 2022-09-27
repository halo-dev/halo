package run.halo.app.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.Post;

/**
 * Tests for {@link CounterServiceImpl}.
 *
 * @author guqing
 * @since 2.0.0
 */
class CounterServiceImplTest {

    private CounterServiceImpl counterService;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
        counterService = new CounterServiceImpl(simpleMeterRegistry);
        String counterName = MeterUtils.nameOf(Post.class, "fake-post");
        MeterUtils.visitCounter(simpleMeterRegistry,
            counterName).increment();

        MeterUtils.approvedCommentCounter(simpleMeterRegistry, counterName)
            .increment(2);
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
}