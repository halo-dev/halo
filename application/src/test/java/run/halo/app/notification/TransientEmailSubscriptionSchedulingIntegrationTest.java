package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.support.TestPropertySourceUtils;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ReactiveExtensionClient;

class TransientEmailSubscriptionSchedulingIntegrationTest {
    @Test
    void inFlightCleanupLeavesSingleSchedulerThreadAvailableAndIsCancelledOnShutdown() throws Exception {
        var subscribed = new CountDownLatch(1);
        var cancelled = new CountDownLatch(1);
        var client = mock(ReactiveExtensionClient.class);
        when(client.listTopNames(eq(Subscription.class), any(), any(), anyInt()))
                .thenReturn(Flux.<String>never()
                        .doOnSubscribe(ignored -> subscribed.countDown())
                        .doOnCancel(cancelled::countDown));

        try (var context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context, "halo.notification.transient-email-cleanup-cron=0 0 0 1 1 *");
            context.register(SchedulingConfiguration.class);
            context.registerBean(ReactiveExtensionClient.class, () -> client);
            context.registerBean(TransientEmailNotificationCoordinator.class);
            context.registerBean(TransientEmailSubscriptionCleanupTask.class);
            context.registerBean("taskScheduler", ThreadPoolTaskScheduler.class, () -> {
                var scheduler = new ThreadPoolTaskScheduler();
                scheduler.setPoolSize(1);
                return scheduler;
            });
            context.refresh();

            var tasks =
                    context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks();
            assertThat(tasks).hasSize(1);
            var scheduler = context.getBean(ThreadPoolTaskScheduler.class);
            // Exercise Spring's registered cron runnable without waiting for the wall-clock trigger.
            scheduler.submit(tasks.iterator().next().getTask().getRunnable());
            assertThat(subscribed.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(scheduler.submit(() -> "other scheduled work").get(2, TimeUnit.SECONDS))
                    .isEqualTo("other scheduled work");
            assertThat(cancelled.getCount()).isEqualTo(1);
        }

        assertThat(cancelled.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    static class SchedulingConfiguration {}
}
