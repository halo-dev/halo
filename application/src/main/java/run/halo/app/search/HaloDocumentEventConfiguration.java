package run.halo.app.search;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Executes index mutations in event order while keeping event publication non-blocking. */
@Configuration(proxyBeanMethods = false)
class HaloDocumentEventConfiguration {

    static final String EVENT_EXECUTOR = "haloDocumentEventExecutor";

    @Bean(name = EVENT_EXECUTOR)
    ThreadPoolTaskExecutor haloDocumentEventExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("halo-document-index-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
