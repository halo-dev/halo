package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import run.halo.app.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Github api test.
 *
 * @author johnniang
 * @date 19-5-21
 */
@Ignore
@Slf4j
public class GithubTest {

    private final static String API_URL = "https://api.github.com/repos/halo-dev/halo-admin/releases/latest";
    private final static String HALO_ADMIN_REGEX = "halo-admin-\\d+\\.\\d+(\\.\\d+)?(-\\S*)?\\.zip";
    private final Path tempPath;
    private final RestTemplate restTemplate;

    public GithubTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        tempPath = Files.createTempDirectory("git-test");
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(5000)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getLatestReleaseTest() throws Throwable {
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(API_URL, Map.class);
        log.debug("Response: " + responseEntity);
        Object assetsObject = responseEntity.getBody().get("assets");
        log.debug("Assets class: " + assetsObject.getClass());
        log.debug("Assets: " + assetsObject);
        if (assetsObject instanceof List) {
            List assets = (List) assetsObject;
            Map assetMap = (Map) assets.stream().filter(aAsset -> {
                if (!(aAsset instanceof Map)) {
                    return false;
                }
                Map aAssetMap = (Map) aAsset;
                Object name = aAssetMap.getOrDefault("name", "");
                return name.toString().matches(HALO_ADMIN_REGEX);
            })
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Halo admin has no assets available"));

            Object name = assetMap.getOrDefault("name", "");
            Object browserDownloadUrl = assetMap.getOrDefault("browser_download_url", "");
            // Download the assets
            ResponseEntity<byte[]> downloadResponseEntity = restTemplate.getForEntity(browserDownloadUrl.toString(), byte[].class);
            log.debug("Download response entity status: " + downloadResponseEntity.getStatusCode());

            Path downloadedPath = Files.write(tempPath.resolve(name.toString()), downloadResponseEntity.getBody());

            log.debug("Downloaded path: " + downloadedPath.toString());
        }
    }

    @Test
    public void nameMatchTEst() {
        String name = "halo-admin-1.0.0-beta.1.zip";

        Assert.assertTrue(name.matches(HALO_ADMIN_REGEX));

        name = "halo-admin-1.0.zip";
        Assert.assertTrue(name.matches(HALO_ADMIN_REGEX));

        name = "halo-admin-1.0.0.zip";
        Assert.assertTrue(name.matches(HALO_ADMIN_REGEX));

        name = "halo-admin-v1.0.0-beta.zip";
        Assert.assertFalse(name.matches(HALO_ADMIN_REGEX));

        name = "halo-admin.zip";
        Assert.assertFalse(name.matches(HALO_ADMIN_REGEX));
    }
}
