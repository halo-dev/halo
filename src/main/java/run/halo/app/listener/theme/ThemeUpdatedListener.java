package run.halo.app.listener.theme;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.event.theme.ThemeUpdatedEvent;
import run.halo.app.service.ThemeService;

/**
 * Theme updated listener.
 *
 * @author johnniang
 * @date 19-4-29
 */
@Component
public class ThemeUpdatedListener {

    private final StringCacheStore cacheStore;

    public ThemeUpdatedListener(StringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @EventListener
    public void onApplicationEvent(ThemeUpdatedEvent event) {
        cacheStore.delete(ThemeService.THEMES_CACHE_KEY);
    }

    @EventListener
    public void onOptionUpdatedEvent(OptionUpdatedEvent optionUpdatedEvent) {
        cacheStore.delete(ThemeService.THEMES_CACHE_KEY);
    }
}
