package run.halo.app.theme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import run.halo.app.exception.ThemePropertyMissingException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.zip.ZipInputStream;

import static run.halo.app.utils.FileUtils.unzip;

/**
 * Zip theme fetcher.
 *
 * @author johnniang
 */
@Slf4j
@Service
public class ZipThemeFetcher implements ThemeFetcher {

    private final HttpClient httpClient;

    public ZipThemeFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofMinutes(5))
                .build();
    }

    @Override
    public boolean support(Object source) {
        if (source instanceof String) {
            return ((String) source).endsWith(".zip");
        }
        return false;
    }

    @Override
    public ThemeProperty fetch(Object source) {
        final var themeZipLink = source.toString();

        // build http request
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(themeZipLink))
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();

        try {
            // request from remote
            log.info("Fetching theme from {}", themeZipLink);
            var inputStreamResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            var inputStream = inputStreamResponse.body();

            // unzip zip archive
            try (var zipInputStream = new ZipInputStream(inputStream)) {
                var tempDirectory = FileUtils.createTempDirectory();
                log.info("Unzip theme to {}", tempDirectory);
                unzip(zipInputStream, tempDirectory);

                // locate theme property location
                var themePropertyPath = ThemeMetaLocator.INSTANCE.locateProperty(tempDirectory)
                        .orElseThrow(() -> new ThemePropertyMissingException("主题配置文件缺失！请确认后重试。"));

                return ThemePropertyScanner.INSTANCE.fetchThemeProperty(themePropertyPath.getParent()).orElseThrow();
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("主题拉取失败！（" + e.getMessage() + "）", e);
        }
    }

}
