package run.halo.app.cache;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@Disabled("Due to project test run exclusion")
@ExtendWith(MockitoExtension.class)
class HazelcastStoreTest {

    @InjectMocks
    private HazelcastStore hazelcastStore;

    @Mock
    private HazelcastInstance hazelcastInstance;

    private IMap<Object, Object> haloMap;

    @BeforeEach
    public void initEach() {
        haloMap = hazelcastInstance.getMap("haloMap");
    }

    @Test
    void should_getInternal_For_Key1() {
        final DateTime createAt = DateUtil.date();
        final Date expireAt = DateUtils.addMinutes(createAt, 5);
        final String value = "{ \"data\": {\"name\": \"halo\"}, \"expireAt\": \"" + expireAt + "\", \"createAt\": \"" + createAt + "\"  }";
        when(haloMap.get("key1")).thenReturn(value);

        final Optional<CacheWrapper<String>> optionalWrapperValue1 = hazelcastStore.getInternal("key1");

        final CacheWrapper<String> wrapperValue1 = optionalWrapperValue1.get();
        assertNotNull(optionalWrapperValue1);

        assertEquals("{\"name\": \"halo\"}", wrapperValue1.getData());
        assertEquals(DateUtil.formatDate(createAt), DateUtil.formatDate(wrapperValue1.getCreateAt()));
        assertEquals(DateUtil.formatDate(expireAt), DateUtil.formatDate(wrapperValue1.getExpireAt()));
    }

    @Test
    void putInternal() {
    }

    @Test
    void putInternalIfAbsent() {
    }

    @Test
    void delete() {
    }
}
