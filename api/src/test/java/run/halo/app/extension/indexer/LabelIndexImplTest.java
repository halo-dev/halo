package run.halo.app.extension.indexer;

import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.jupiter.api.Test;

class LabelIndexImplTest {

    @Test
    void stringPrefixTest() {
        var map = new ConcurrentSkipListMap<String, String>();
        map.put("a@b", "1");
        map.put("a@c", "2");
        map.put("a@cdefg", "2");
        map.put("a@d", "3");
        map.put("b@d", "4");

        var subMap = map.subMap("a@", "a@" + Character.MAX_VALUE);
        subMap.sequencedKeySet().forEach(System.out::println);
    }
}