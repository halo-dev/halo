package run.halo.app.theme;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

/**
 * Zip remote theme fetcher test.
 *
 * @author johnniang
 */
@Slf4j
class ZipRemoteThemeFetcherTest {

    @Test
    @Disabled("Disabled due to time consumed")
    void fetch() {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        var themeFetcher = new ZipRemoteThemeFetcher(httpClient);
        var themeProperty = themeFetcher.fetch("https://github.com/halo-dev/halo-theme-hshan/archive/master.zip");
        log.debug("{}", themeProperty);
    }
}