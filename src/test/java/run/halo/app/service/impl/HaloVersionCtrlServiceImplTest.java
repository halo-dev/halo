package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.dto.VersionInfoDTO;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.HaloVersionCtrlService;
import run.halo.app.utils.VmUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class HaloVersionCtrlServiceImplTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    HaloVersionCtrlService versionCtrlService;

    @Test
    void testIsInLocal() {
        assertFalse(this.versionCtrlService.isInLocal("Tag Name"));
        Path DIR = VmUtils.CURR_JAR_DIR;
        Path repo = DIR.resolve(HaloVersionCtrlServiceImpl.REPO_DIR);
        Path test = null, tempFile = null;
        final boolean exists = Files.exists(repo);
        try {
            if (!exists) {
                Files.createDirectories(repo);
            }
            test = Files.createTempDirectory(repo, "test");
            assertFalse(versionCtrlService.isInLocal(test.getFileName().toString()));
            tempFile = Files.createTempFile(test, "halo", ".jar");
            assertTrue(versionCtrlService.isInLocal(test.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile);
                }
                if (test != null) {
                    Files.deleteIfExists(test);
                }
                if (!exists) {
                    Files.deleteIfExists(repo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    void testGetLatestReleaseInfo() throws RestClientException {

        final VersionInfoDTO latestReleaseInfo = versionCtrlService.getLatestReleaseInfo();

        assertNotNull(latestReleaseInfo);
        assertFalse(StringUtils.isBlank(latestReleaseInfo.getDownloadUrl()));
    }

    @Test
    void testDownload() throws RestClientException {

        // 仅仅分片下载 10 bytes以测试下载通道是否顺畅
        assertDoesNotThrow(() -> {
                final VersionInfoDTO latestReleaseInfo = versionCtrlService.getLatestReleaseInfo();
                final String downloadUrl = latestReleaseInfo.getDownloadUrl();

                final byte[] data = restTemplate.execute(downloadUrl, HttpMethod.GET,

                    req -> req.getHeaders()
                        .set("Range", String.format("bytes=%d-%d", 0, 9)),
                    resp -> {
                        try {
                            return resp.getBody().readAllBytes();
                        } catch (Exception e) {
                            throw new ServiceException("下载失败, url: " + downloadUrl);
                        }
                    });
                assertNotNull(data);
                assertEquals(10, data.length);
            }
        );

    }

    @Test
    void testGetCurVersion() {
        assertEquals(HaloConst.HALO_VERSION, versionCtrlService.getCurVersion());
    }
}