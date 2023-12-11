package run.halo.app.search;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Component
public class IndicesInitializer {

    private final IndicesService indicesService;

    public IndicesInitializer(IndicesService indicesService) {
        this.indicesService = indicesService;
    }

    @Async
    @EventListener
    public void whenSchemeInitialized(ApplicationStartedEvent event) {
        log.info("Initialize post indices...");
        var watch = new StopWatch("PostIndicesWatch");
        watch.start("rebuild");
        indicesService.rebuildPostIndices().block(Duration.ofMinutes(5));
        log.info("Initialized post indices. Usage: {}", watch);
    }

}
