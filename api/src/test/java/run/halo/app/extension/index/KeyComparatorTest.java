package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link KeyComparator}.
 *
 * @author guqing
 * @since 2.12.0
 */
class KeyComparatorTest {

    @Test
    void keyComparator() {
        var comparator = KeyComparator.INSTANCE;
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
        var comparator = KeyComparator.INSTANCE;
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
}