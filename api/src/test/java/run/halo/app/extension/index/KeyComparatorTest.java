package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link KeyComparator}.
 *
 * @author guqing
 * @since 2.12.0
 */
class KeyComparatorTest {
    private final KeyComparator comparator = KeyComparator.INSTANCE;

    @Test
    void keyComparator() {
        String[] strings = {"103", "101", "102", "1011", "1013", "1021", "1022", "1012", "1023"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(
            new String[] {"101", "102", "103", "1011", "1012", "1013", "1021", "1022", "1023"});

        Arrays.sort(strings, comparator.reversed());
        assertThat(strings).isEqualTo(
            new String[] {"1023", "1022", "1021", "1013", "1012", "1011", "103", "102", "101"});

        // but if we use natural order, the result is:
        Arrays.sort(strings, Comparator.naturalOrder());
        assertThat(strings).isEqualTo(
            new String[] {"101", "1011", "1012", "1013", "102", "1021", "1022", "1023", "103"});
    }

    @Test
    void keyComparator2() {
        String[] strings =
            {"moment-101", "moment-102", "moment-103", "moment-1011", "moment-1013", "moment-1021",
                "moment-1022", "moment-1012", "moment-1023"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"moment-101", "moment-102", "moment-103",
            "moment-1011", "moment-1012", "moment-1013", "moment-1021", "moment-1022",
            "moment-1023"});

        // date sort
        strings =
            new String[] {"2022-01-15", "2022-02-01", "2021-12-25", "2022-01-01", "2022-01-02"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(
            new String[] {"2021-12-25", "2022-01-01", "2022-01-02", "2022-01-15", "2022-02-01"});

        // alphabet and number sort
        strings = new String[] {"abc123", "abc45", "abc9", "abc100", "abc20"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(
            new String[] {"abc9", "abc20", "abc45", "abc100", "abc123"});

        // test for pure alphabet sort
        strings = new String[] {"xyz", "abc", "def", "abcde", "xyzabc"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"abc", "abcde", "def", "xyz", "xyzabc"});

        // test for empty string
        strings = new String[] {"", "abc", "123", "xyz"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"", "123", "abc", "xyz"});

        // test for the same string
        strings = new String[] {"abc", "abc", "abc", "abc"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"abc", "abc", "abc", "abc"});

        // test for null element
        strings = new String[] {null, "abc", "123", "xyz"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"123", "abc", "xyz", null});
    }

    @Test
    void complexStringTest() {
        var strings = new String[] {
            "1719560085223",
            "1719564195757",
            "AJHQ9JKT",
            "1719565849173",
            "5InykKCe",
            "123123123",
            "adJhTqEo",
            "123123",
            "Ahvcq7Wn",
            "asda",
            "b5jHcxfe"
        };
        Arrays.sort(strings, comparator);
        assertThat(strings).containsExactly(
            "5InykKCe",
            "123123",
            "123123123",
            "1719560085223",
            "1719564195757",
            "1719565849173",
            "AJHQ9JKT",
            "Ahvcq7Wn",
            "adJhTqEo",
            "asda",
            "b5jHcxfe"
        );
    }

    @Test
    void complexButSkewedStringTest() {
        var strings = new String[] {
            "chu-shi-hua-gong-neng-you-hua-halo-2.9.0-fa-bu",
            "cxcc",
            "d",
            "dddd",
            "ddddd",
            "de-dao",
            "dENMr6tX",
            "dian-shang-ke-fu",
            "dong-tai-she-ji-shi-xi-25jie",
            "eeeeeeee",
            "ejqRrTp4",
            "Fh8Jd09T",
            "g5gZaGvS",
        };
        Arrays.sort(strings, comparator);
        assertThat(strings).containsExactly(
            "Fh8Jd09T",
            "chu-shi-hua-gong-neng-you-hua-halo-2.9.0-fa-bu",
            "cxcc",
            "d",
            "dENMr6tX",
            "dddd",
            "ddddd",
            "de-dao",
            "dian-shang-ke-fu",
            "dong-tai-she-ji-shi-xi-25jie",
            "eeeeeeee",
            "ejqRrTp4",
            "g5gZaGvS"
        );
    }

    @Test
    void mixLetterCaseStringTest() {
        var strings = new String[] {
            "VpLBxBJ7", "AJHQ9JKT", "asda", "Tq5EgH2V", "Fh8Jd09T", "J7KMLQeK", "adJhTqEo",
            "Ahvcq7Wn",
        };
        Arrays.sort(strings, comparator);
        assertThat(strings).containsExactly(
            "AJHQ9JKT", "Ahvcq7Wn", "Fh8Jd09T", "J7KMLQeK", "Tq5EgH2V", "VpLBxBJ7", "adJhTqEo",
            "asda"
        );
    }

    @Test
    void mixLetterCaseAndNumberTest() {
        var strings = new String[] {
            "1719565849173", "1719564195757", "1703040584263",
            "AJHQ9JKT", "Ahvcq7Wn", "Fh8Jd09T", "adJhTqEo",
            "asda", "1703053590063", "1702955288482",
            "zhi-chi-bei-fen-hui-fu-halo-2.8.0-fa-bu",
            "zhi-chi-ge-ren-zhong-xin-halo-2.11.0-fa-bu",
            "J7KMLQeK", "Tq5EgH2V", "VpLBxBJ7",
            "b5jHcxfe", "cao-ni-ma-a-huang-jian-ming", "chu-ji-ying-jian-kai-fa",
            "ddddd", "de-dao", "dian-shang-ke-fu", "eeeeeeee", "ejqRrTp4",
            "halo-maintainer-2023-nian-du-bang-dan", "hello-halo", "hello-world",
            "dong-tai-she-ji-shi-xi-25jie", "halo-nuan-dong-li-yu-quan-chang-qi-zhe-qi", "hello",
            "kai-fang-gong-gong-api-halo-2.5.0-fa-bu",
            "xing-neng-you-hua-yu-gong-neng-gai-jin-halo-2.13-fa-bu", "ye-wu-tuo-zhan-jing-li",
            "ying-qu-jing-mei-zhou-bian-halo-ying-yong-shi-chang-zhu-ti-you-jiang-zheng-ji",
            "zhi-chi-bao-chi-deng-lu-hui-hua-halo-2.16.0-fa-bu",
        };
        Arrays.sort(strings, comparator);
        assertThat(strings).containsExactly(
            "1702955288482",
            "1703040584263",
            "1703053590063",
            "1719564195757",
            "1719565849173",
            "AJHQ9JKT",
            "Ahvcq7Wn",
            "Fh8Jd09T",
            "J7KMLQeK",
            "Tq5EgH2V",
            "VpLBxBJ7",
            "adJhTqEo",
            "asda",
            "b5jHcxfe",
            "cao-ni-ma-a-huang-jian-ming",
            "chu-ji-ying-jian-kai-fa",
            "ddddd",
            "de-dao",
            "dian-shang-ke-fu",
            "dong-tai-she-ji-shi-xi-25jie",
            "eeeeeeee",
            "ejqRrTp4",
            "halo-maintainer-2023-nian-du-bang-dan",
            "halo-nuan-dong-li-yu-quan-chang-qi-zhe-qi",
            "hello",
            "hello-halo",
            "hello-world",
            "kai-fang-gong-gong-api-halo-2.5.0-fa-bu",
            "xing-neng-you-hua-yu-gong-neng-gai-jin-halo-2.13-fa-bu",
            "ye-wu-tuo-zhan-jing-li",
            "ying-qu-jing-mei-zhou-bian-halo-ying-yong-shi-chang-zhu-ti-you-jiang-zheng-ji",
            "zhi-chi-bao-chi-deng-lu-hui-hua-halo-2.16.0-fa-bu",
            "zhi-chi-bei-fen-hui-fu-halo-2.8.0-fa-bu",
            "zhi-chi-ge-ren-zhong-xin-halo-2.11.0-fa-bu");
    }

    @Test
    public void sortingWithComplexStringsTest() {
        List<String> strings = Arrays.asList("abc10", "abc2", "abc1", "abc20", "abc100");
        strings.sort(comparator);
        assertThat(strings).containsExactly("abc1", "abc2", "abc10", "abc20", "abc100");
    }

    @Test
    public void sortingWithDecimalStringsTest() {
        List<String> strings =
            Arrays.asList("1.2", "1.10", "1.1", "1.20", "1.02", "1.22", "1.001", "1.002");
        strings.sort(comparator);
        assertThat(strings).containsExactly("1.001", "1.002", "1.02", "1.1", "1.10", "1.2", "1.20",
            "1.22");
    }

    @Test
    public void treeSetWithComparatorTest() {
        TreeSet<String> set = new TreeSet<>(comparator);
        set.add("abc123");
        set.add("abc1");
        set.add("abc12");
        set.add("abc2");

        assertThat(set).containsExactly("abc1", "abc2", "abc12", "abc123");
    }

    @Test
    public void testTreeMap_WithComparator() {
        TreeMap<String, String> map = new TreeMap<>(comparator);
        map.put("2024-08-29", "date1");
        map.put("2024-08-28", "date2");
        map.put("2024-08-30", "date3");

        assertThat(map.keySet()).containsExactly("2024-08-28", "2024-08-29", "2024-08-30");
        assertThat(map.get("2024-08-29")).isEqualTo("date1");
    }

    @Test
    public void integerPartDifferentTest() {
        // Create strings with different integer parts to cover the compareIntegerPart code
        // block
        String[] strings = {"abc10", "abc2", "abc1", "abc20", "abc10022229"};
        Arrays.sort(strings, comparator);

        String[] expectedOrder = {"abc1", "abc2", "abc10", "abc20", "abc10022229"};
        assertThat(strings).containsExactly(expectedOrder);
    }

    @Test
    public void integerPartDifferentWithDecimalTest() {
        // To specifically test integer part comparison
        String str1 = "abc12.5";
        String str2 = "abc11.5";

        // Compare should return a positive number since "12" > "11"
        assertThat(comparator.compare(str1, str2)).isPositive();

        String str3 = "abc11.5";
        String str4 = "abc12.5";

        // Compare should return a negative number since "11" < "12"
        assertThat(comparator.compare(str3, str4)).isNegative();

        // Test for multiple decimal points
        assertThat(comparator.compare("1.23.4", "1.23")).isGreaterThan(0);
        assertThat(comparator.compare("1.23", "1.23.4")).isLessThan(0);

        assertThat(comparator.compare("1..23", "1.23")).isLessThan(0);
        assertThat(comparator.compare("1.23..", "1.23")).isGreaterThan(0);

        assertThat(comparator.compare("", "1.23")).isLessThan(0);
        assertThat(comparator.compare("1.23", "")).isGreaterThan(0);

        assertThat(comparator.compare("1.23", "1.23")).isZero();
    }

    @Nested
    class ComparatorCharacteristicTest {
        @Test
        public void reflexiveTest() {
            // Reflexive: a == a should always return 0
            assertThat(comparator.compare("test", "test")).isZero();
            assertThat(comparator.compare("", "")).isZero();
            assertThat(comparator.compare("123", "123")).isZero();
            assertThat(comparator.compare(null, null)).isZero();
        }

        @Test
        public void symmetricTest() {
            // Symmetric: a > b implies b < a
            assertThat(comparator.compare("123", "test")).isNegative();
            assertThat(comparator.compare("test", "123")).isPositive();

            assertThat(comparator.compare("1.023", "1.23")).isNegative();
            assertThat(comparator.compare("1.23", "1.023")).isPositive();
        }

        @Test
        public void transitiveTest() {
            // Transitive: a > b and b > c implies a > c
            assertThat(comparator.compare("test2", "test1")).isPositive();
            assertThat(comparator.compare("test1", "test0")).isPositive();
            assertThat(comparator.compare("test2", "test0")).isPositive();
        }

        @RepeatedTest(50)
        public void consistencyTest() {
            // Consistency: a == b should always return 0 if not changed
            assertThat(comparator.compare("123abc", "123abc")).isZero();
            assertThat(comparator.compare("test", "test")).isZero();
            assertThat(comparator.compare("123abc", "123abc"))
                .isEqualTo(comparator.compare("123abc", "123abc"));
        }

        @Test
        public void withNumbersTest() {
            // Numbers should be compared numerically
            assertThat(comparator.compare("item2", "item10")).isNegative();
            assertThat(comparator.compare("item10", "item2")).isPositive();
            assertThat(comparator.compare("item10", "item10")).isZero();
        }

        @Test
        public void mixedContentTest() {
            // Mixed content comparison
            assertThat(comparator.compare("abc123", "abc124")).isNegative();
            assertThat(comparator.compare("abc124", "abc123")).isPositive();
            assertThat(comparator.compare("abc123", "abc123")).isZero();
        }

        @Test
        public void nullHandlingTest() {
            // Null handling
            assertThat(comparator.compare(null, "test")).isPositive();
            assertThat(comparator.compare("test", null)).isNegative();
            assertThat(comparator.compare(null, null)).isZero();
        }

        @Test
        public void lengthDifferenceTest() {
            // Length difference should affect comparison
            assertThat(comparator.compare("test", "testa")).isNegative();
            assertThat(comparator.compare("testa", "test")).isPositive();
        }

        @Test
        public void specialCharactersTest() {
            // Special character comparison
            assertThat(comparator.compare("a#1", "a#2")).isNegative();
            assertThat(comparator.compare("a#2", "a#1")).isPositive();
            assertThat(comparator.compare("a#1", "a#1")).isZero();
        }

        @Test
        public void emptyStringsTest() {
            // Empty string comparison
            assertThat(comparator.compare("", "test")).isNegative();
            assertThat(comparator.compare("test", "")).isPositive();
            assertThat(comparator.compare("", "")).isZero();
        }
    }

    @Nested
    class ComparatorEdgeTest {
        @Test
        public void pureNumbersTest() {
            assertThat(comparator.compare("123", "123")).isEqualTo(0);
            assertThat(comparator.compare("123", "124")).isLessThan(0);
            assertThat(comparator.compare("124", "123")).isGreaterThan(0);
            // Leading zeros
            assertThat(comparator.compare("00123", "123") > 0).isTrue();
            assertThat(comparator.compare("0", "0")).isEqualTo(0);
            assertThat(comparator.compare("0", "0000")).isLessThan(0);
            assertThat(comparator.compare("0x", "0")).isGreaterThan(0);
            assertThat(comparator.compare("0", "1")).isLessThan(0);
            assertThat(comparator.compare("1", "0")).isGreaterThan(0);
            assertThat(comparator.compare("001", "0")).isGreaterThan(0);
            assertThat(comparator.compare("0x5e", "0000")).isLessThan(0);
        }

        @Test
        public void mumbersWithOverflowTest() {
            // Max long value
            String largeNumber1 = "9223372036854775807";
            // One more than max long value
            String largeNumber2 = "9223372036854775808";
            assertThat(comparator.compare(largeNumber1, largeNumber2)).isLessThan(0);
            assertThat(comparator.compare(largeNumber2, largeNumber1)).isGreaterThan(0);

            // large number str comparison
            assertThat(comparator.compare("123456789012345678901234567890",
                "123456789012345678901234567891")).isLessThan(0);
            assertThat(comparator.compare("123456789012345678901234567890",
                "123456789012345678901234567891")).isNotPositive();
            assertThat(comparator.compare("999999999999999999999999999999",
                "999999999999999999999999999998")).isGreaterThan(0);
            assertThat(comparator.compare("999999999999999999999999999999",
                "999999999999999999999999999998")).isNotNegative();
            assertThat(comparator.compare("9999999999999999999999999999999999999999999999",
                "9999999999999999999999999999999999999999999998")).isGreaterThan(0);
            assertThat(comparator.compare("100000000000000000000000000000",
                "100000000000000000000000000000")).isEqualTo(0);

            // This specific case is to test the overflow for a real-world scenario
            assertThat(comparator.compare("5InykKCe", "1710683457874") < 0).isTrue();
            assertThat(comparator.compare("5InykKce", "1717477435943") > 0).isFalse();

            assertThat(comparator.compare("0",
                "9999999999999999999999999999999999999999999998")).isLessThan(0);
            assertThat(comparator.compare("9999999999999999999999999999999999999999999998",
                "0")).isGreaterThan(0);
        }

        @Test
        public void decimalStringsTest() {
            assertThat(comparator.compare("123.45", "123.45")).isEqualTo(0);
            assertThat(comparator.compare("123.45", "123.46")).isLessThan(0);
            assertThat(comparator.compare("123.46", "123.45")).isGreaterThan(0);
            // Decimal equivalence
            assertThat(comparator.compare("123.5", "123.50")).isLessThan(0);
            assertThat(comparator.compare("123.0005", "123.00050")).isLessThan(0);
        }

        @Test
        public void lettersAndNumbersTest() {
            assertThat(comparator.compare("abc123", "abc123")).isEqualTo(0);
            assertThat(comparator.compare("abc123", "abc124")).isLessThan(0);
            assertThat(comparator.compare("abc124", "abc123")).isGreaterThan(0);
            assertThat(comparator.compare("abc123", "abcd123")).isLessThan(0);
        }

        @Test
        public void pureLettersTest() {
            assertThat(comparator.compare("abc", "abc")).isEqualTo(0);
            assertThat(comparator.compare("abc", "abcd")).isLessThan(0);
            assertThat(comparator.compare("abcd", "abc")).isGreaterThan(0);
            // Case sensitivity
            assertThat(comparator.compare("ABC", "abc")).isLessThan(0);
        }

        @Test
        public void dateStringsTest() {
            assertThat(comparator.compare("2024-08-29", "2024-08-29")).isEqualTo(0);
            assertThat(comparator.compare("2024-08-29", "2024-08-30")).isLessThan(0);
            assertThat(comparator.compare("2024-08-30", "2024-08-29")).isGreaterThan(0);

            // Time comparison
            assertThat(comparator.compare("2024-08-29T12:00:00.000Z", "2024-08-29T12:00:00.001Z"))
                .isLessThan(0);
            assertThat(comparator.compare("2024-08-29T12:00:00.001Z", "2024-08-29T12:00:00.000Z"))
                .isGreaterThan(0);
            assertThat(comparator.compare("2024-08-29T12:00:00.000Z", "2024-08-29T12:00:01.000Z"))
                .isLessThan(0);
            assertThat(comparator.compare("2024-08-29T12:00:01.000Z", "2024-08-29T12:00:00.000Z"))
                .isGreaterThan(0);
            assertThat(comparator.compare("2024-08-29T12:00:00.000Z", "2024-08-29T12:01:00.000Z"))
                .isLessThan(0);
            assertThat(comparator.compare("2024-08-29T12:01:00.000Z", "2024-08-29T12:00:00.000Z"))
                .isGreaterThan(0);
            assertThat(comparator.compare("2024-08-29T12:00:00.000Z", "2024-08-29T13:00:00.000Z"))
                .isLessThan(0);
            assertThat(comparator.compare("2024-08-29T13:00:00.000Z", "2024-08-29T12:00:00.000Z"))
                .isGreaterThan(0);
            assertThat(comparator.compare("2024-08-29T12:00:00.000Z", "2024-08-30T12:00:00.000Z"))
                .isLessThan(0);
            assertThat(comparator.compare("2024-08-30T12:00:00.000Z", "2024-08-29T12:00:00.000Z"))
                .isGreaterThan(0);
        }

        @Test
        public void booleanStringsTest() {
            assertThat(comparator.compare("true", "false")).isGreaterThan(0);
            assertThat(comparator.compare("false", "true")).isLessThan(0);
            assertThat(comparator.compare("true", "true")).isEqualTo(0);
            assertThat(comparator.compare("false", "false")).isEqualTo(0);
        }

        @Test
        public void complexMixedStringsTest() {
            assertThat(comparator.compare("abc123xyz456", "abc123xyz456")).isEqualTo(0);
            assertThat(comparator.compare("abc123xyz456", "abc124xyz456")).isLessThan(0);
            assertThat(comparator.compare("abc124xyz456", "abc123xyz456")).isGreaterThan(0);
            assertThat(comparator.compare("abc123xyz456", "abc123xyz457")).isLessThan(0);
        }
    }
}
