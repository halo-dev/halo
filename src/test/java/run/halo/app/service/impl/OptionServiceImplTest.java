package run.halo.app.service.impl;

import com.qiniu.common.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.model.entity.Option;
import run.halo.app.model.properties.QiniuOssProperties;
import run.halo.app.repository.OptionRepository;
import run.halo.app.service.OptionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * OptionService test.
 *
 * @author johnniang
 * @date 3/22/19
 */
class OptionServiceImplTest {

    @Mock
    OptionRepository optionRepository;

    @Mock
    AbstractStringCacheStore cacheStore;

    @InjectMocks
    OptionServiceImpl optionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getQiniuAutoZoneTest() {
        getQiniuZoneTest("", Zone.autoZone());
    }

    @Test
    void getQiniuAutoZoneOfNullOptionTest() {
        getQiniuZoneTest(Zone.autoZone(), null);
    }

    @Test
    void getQiniuZ0ZoneTest() {
        getQiniuZoneTest("z0", Zone.zone0());
    }

    @Test
    void getQiniuZ1ZoneTest() {
        getQiniuZoneTest("z1", Zone.zone1());
    }

    @Test
    void getQiniuZ2ZoneTest() {
        getQiniuZoneTest("z2", Zone.zone2());
    }

    @Test
    void getQiniuAs0ZoneTest() {
        getQiniuZoneTest("as0", Zone.zoneAs0());
    }

    @Test
    void getQiniuNa0ZoneTest() {
        getQiniuZoneTest("na0", Zone.zoneNa0());
    }

    void getQiniuZoneTest(String region, Zone actualZone) {
        getQiniuZoneTest(actualZone, new Option("", region));
    }

    void getQiniuZoneTest(Zone actualZone, Option option) {
        QiniuOssProperties zoneProperty = QiniuOssProperties.OSS_ZONE;

        // Given
//        given(optionRepository.findByKey(zoneProperty.getValue())).willReturn(Optional.ofNullable(option));
        Map<String, Object> optionMap = new HashMap<>(1);
        optionMap.put(zoneProperty.getValue(), Optional.ofNullable(option).map(Option::getValue).orElse(null));
        given(cacheStore.getAny(OptionService.OPTIONS_KEY, Map.class)).willReturn(Optional.of(optionMap));

        // When
        Zone zone = optionService.getQnYunZone();

        // Then
        then(cacheStore).should().getAny(OptionService.OPTIONS_KEY, Map.class);

        assertNotNull(zone);
        assertEquals(actualZone.getRegion(), zone.getRegion());
    }
}
