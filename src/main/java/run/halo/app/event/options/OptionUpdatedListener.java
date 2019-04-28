package run.halo.app.event.options;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.service.OptionService;

/**
 * Option updated listener.
 *
 * @author johnniang
 * @date 19-4-29
 */
@Component
public class OptionUpdatedListener implements ApplicationListener<OptionUpdatedEvent> {

    private final StringCacheStore cacheStore;

    public OptionUpdatedListener(StringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    @Async
    public void onApplicationEvent(OptionUpdatedEvent event) {
        cacheStore.delete(OptionService.OPTIONS_KEY);
    }
}
