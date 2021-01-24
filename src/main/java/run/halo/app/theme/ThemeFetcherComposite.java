package run.halo.app.theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import run.halo.app.handler.theme.config.support.ThemeProperty;

/**
 * Theme fetcher composite.
 *
 * @author johnniang
 */
public class ThemeFetcherComposite implements ThemeFetcher {

    /**
     * Theme fetcher container.
     */
    private final List<ThemeFetcher> themeFetchers = new ArrayList<>(4);

    /**
     * Fallback theme fetcher.
     */
    private final ThemeFetcher fallbackFetcher = new GitThemeFetcher();

    public ThemeFetcherComposite addFetcher(ThemeFetcher fetcher) {
        this.themeFetchers.add(fetcher);
        return this;
    }

    public ThemeFetcherComposite addFetcher(ThemeFetcher... fetchers) {
        if (fetchers != null) {
            Collections.addAll(this.themeFetchers, fetchers);
        }
        return this;
    }

    public List<ThemeFetcher> getFetchers() {
        return Collections.unmodifiableList(this.themeFetchers);
    }

    public void clear() {
        this.themeFetchers.clear();
    }

    @Override
    public boolean support(Object source) {
        return getThemeFetcher(source).isPresent();
    }

    @Override
    public ThemeProperty fetch(Object source) {
        final var themeFetcher = getThemeFetcher(source).orElse(fallbackFetcher);
        return themeFetcher.fetch(source);
    }

    @NonNull
    private Optional<ThemeFetcher> getThemeFetcher(Object source) {
        return themeFetchers.stream()
                .filter(fetcher -> fetcher.support(source))
                .findFirst();
    }
}
