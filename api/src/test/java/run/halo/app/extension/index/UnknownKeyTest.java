package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UnknownKeyTest {

    @ParameterizedTest
    @CsvSource({
        "a, a",
        "b, b",
        ",",
        "abc, abc",
        "2025, 2025",
        "true, true",
        "2025-10-20, 2025-10-20",
        "特殊字符, 特殊字符"
    })
    void shouldEqualsWorkCorrectly(String key1, String key2) {
        assertEquals(new UnknownKey(key1), new UnknownKey(key2));
        assertEquals(new UnknownKey(null), new UnknownKey(null));
    }

    @ParameterizedTest
    @CsvSource(
        {
            "a, a, 0",
            "a, b, -1",
            "b, a, 1",
            "abc, abd, -1",
            "abd, abc, 1",
            "0.1, 0.1, 0",
            "0.1, 0.2, -1",
            "0.2, 0.1, 1",
            "true, true, 0",
            "false, true, -1",
            "true, false, 1",
            "-0.1, -0.1, 0",
            "-1, -1, 0",
            "2025, 2025, 0",
            "2025, 2026, -1",
            "2026, 2025, 1",
            "2025-10-20, 2025-10-20, 0",
            "2025-10-20, 2025-10-21, -1",
            "2025-10-21, 2025-10-20, 1",
            "特殊字符A, 特殊字符B, -1",
            "特殊字符B, 特殊字符A, 1"
        }
    )
    void shouldCompareCorrectly(String key1, String key2, int expected) {
        var compare = new UnknownKey(key1).compareTo(new UnknownKey(key2));
        assertTrue(Objects.equals(expected, compare) || expected * compare > 0);
    }

}