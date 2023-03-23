package run.halo.app.search;

import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import run.halo.app.infra.SchemeInitializedEvent;

@Slf4j
@Component
public class IndicesInitializer {

    private final IndicesService indicesService;

    public IndicesInitializer(IndicesService indicesService) {
        this.indicesService = indicesService;
    }

    @Async
    @EventListener(SchemeInitializedEvent.class)
    public void whenSchemeInitialized(SchemeInitializedEvent event) throws InterruptedException {
        var latch = new CountDownLatch(1);
        log.info("Initialize post indices...");
        var watch = new StopWatch("PostIndicesWatch");
        watch.start("rebuild");
        indicesService.rebuildPostIndices()
            .doFinally(signalType -> latch.countDown())
            .subscribe();
        latch.await();
        watch.stop();
        log.info("Initialized post indices. Usage: {}", watch);
    }

}
