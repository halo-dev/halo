package run.halo.app.event.theme;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.service.ThemeService;

/**
 * Theme updated listener.
 *
 * @author johnniang
 * @date 19-4-29
 */
@Component
public class ThemeUpdatedListener implements ApplicationListener<ThemeUpdatedEvent> {

    private final StringCacheStore cacheStore;

    public ThemeUpdatedListener(StringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    @Async
    public void onApplicationEvent(ThemeUpdatedEvent event) {
        cacheStore.delete(ThemeService.THEMES_CACHE_KEY);
    }
}
