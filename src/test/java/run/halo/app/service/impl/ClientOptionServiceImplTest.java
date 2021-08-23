package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static run.halo.app.service.OptionService.OPTIONS_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.model.properties.AliOssProperties;
import run.halo.app.model.properties.CommentProperties;


/**
 * @author LIlGG
 * @date 2021/8/2
 */
@SpringBootTest
@ActiveProfiles("test")
class ClientOptionServiceImplTest {

    @Autowired
    AbstractStringCacheStore cacheStore;

    @Autowired
    ClientOptionServiceImpl clientOptionService;

    Map<String, Object> map = new HashMap<>();

    {
        map.put(AliOssProperties.OSS_DOMAIN.getValue(), "1");
        map.put(CommentProperties.CONTENT_PLACEHOLDER.getValue(), "2");
        map.put(CommentProperties.GRAVATAR_SOURCE.getValue(), "3");
    }

    @BeforeEach
    void setUp() {
        cacheStore.putAny(OPTIONS_KEY, map);
    }

    @Test
    void listOptionsTest() {
        Map<String, Object> options = clientOptionService.listOptions();

        assertFalse(options.containsKey(AliOssProperties.OSS_DOMAIN.getValue()));
    }

    @Test
    void listOptionsKeyTest() {
        List<String> keys = new ArrayList<>();
        keys.add(AliOssProperties.OSS_DOMAIN.getValue());
        keys.add(CommentProperties.CONTENT_PLACEHOLDER.getValue());
        keys.add(CommentProperties.GRAVATAR_SOURCE.getValue());
        Map<String, Object> options = clientOptionService.listOptions(keys);

        assertFalse(options.containsKey(AliOssProperties.OSS_DOMAIN.getValue()));
    }

    @Test
    void getByKeyTest() {
        assertNull(clientOptionService.getByKey(AliOssProperties.OSS_DOMAIN.getValue())
            .orElse(null));
    }
}
