package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import io.seruco.encoding.base62.Base62;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Base62}.
 *
 * @author guqing
 * @since 2.0.0
 */
class Base62UtilsTest {

    @Test
    void encode() {
        getNaiveTestSet().forEach(
            (str, encoded) -> assertThat(Base62Utils.encode(str)).isEqualTo(encoded));
    }

    @Test
    void decodeToString() {
        getNaiveTestSet().forEach(
            (str, encoded) -> assertThat(Base62Utils.decodeToString(encoded)).isEqualTo(str));
    }

    public static Map<String, String> getNaiveTestSet() {
        Map<String, String> testSet = new HashMap<>();

        testSet.put("", "");
        testSet.put("a", "1Z");
        testSet.put("Hello", "5TP3P3v");
        testSet.put("Hello world!", "T8dgcjRGuYUueWht");
        testSet.put("Just a test", "7G0iTmJjQFG2t6K");
        testSet.put("!!!!!!!!!!!!!!!!!", "4A7f43EVXQoS6Am897ZKbAn");
        testSet.put("0123456789", "18XU2xYejWO9d3");
        testSet.put("The quick brown fox jumps over the lazy dog",
            "83UM8dOjD4xrzASgmqLOXTgTagvV1jPegUJ39mcYnwHwTlzpdfKXvpp4RL");
        testSet.put("Sphinx of black quartz, judge my vow",
            "1Ul5yQGNM8YFBp3sz19dYj1kTp95OW7jI8pTcTP5JhYjIaFmx");

        return testSet;
    }
}
