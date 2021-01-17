package run.halo.app.theme;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Zip remote theme fetcher test.
 *
 * @author johnniang
 */
@Slf4j
class ZipThemeFetcherTest {

    @Test
    @Disabled("Disabled due to time consumed")
    void fetch() {
        var themeFetcher = new ZipThemeFetcher();
        var themeProperty = themeFetcher.fetch("https://github.com/halo-dev/halo-theme-hshan/archive/master.zip");
        log.debug("{}", themeProperty);
    }
}