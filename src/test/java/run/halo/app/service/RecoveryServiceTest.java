package run.halo.app.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import run.halo.app.utils.JsonUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author johnniang
 * @date 19-4-26
 */
@Slf4j
class RecoveryServiceTest {

    @Test
    void getMigrationFileContent() throws IOException, URISyntaxException {
        String migrationContent = getMigrationContent();

        assertNotNull(migrationContent);
        assertTrue(migrationContent.length() > 0);
    }

    @Test
    void resolveMigrationContent() throws IOException, URISyntaxException {
        String migrationContent = getMigrationContent();

        Object migrationObject = JsonUtils.jsonToObject(migrationContent, Object.class);

        log.debug(migrationObject.getClass().toString());

        if (migrationObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> migrationMap = (Map<String, Object>) migrationObject;

            migrationMap.forEach((key, value) -> log.debug("Key: [{}], value type: [{}], value: [{}]", key, value.getClass().getTypeName(), value));
        }
    }

    String getMigrationContent() throws IOException, URISyntaxException {
        URL migrationUrl = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + "migration-test.json");

        Path path = Paths.get(migrationUrl.toURI());

        return new String(Files.readAllBytes(path));
    }
}