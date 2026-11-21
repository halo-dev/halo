package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HaloDocumentEventConfigurationTest {

    @Test
    void executesIndexEventsInSubmissionOrder() throws Exception {
        var configuration = new HaloDocumentEventConfiguration();
        var executor = configuration.haloDocumentEventExecutor();
        executor.afterPropertiesSet();
        var order = new ArrayList<String>();
        var firstStarted = new CountDownLatch(1);
        var releaseFirst = new CountDownLatch(1);
        var secondFinished = new CountDownLatch(1);
        try {
            executor.execute(() -> {
                order.add("first-start");
                firstStarted.countDown();
                await(releaseFirst);
                order.add("first-end");
            });
            assertThat(firstStarted.await(1, TimeUnit.SECONDS)).isTrue();

            executor.execute(() -> {
                order.add("second");
                secondFinished.countDown();
            });
            releaseFirst.countDown();

            assertThat(secondFinished.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(order).containsExactly("first-start", "first-end", "second");
        } finally {
            releaseFirst.countDown();
            executor.shutdown();
            executor.getThreadPoolExecutor().awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for index event");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }
}
