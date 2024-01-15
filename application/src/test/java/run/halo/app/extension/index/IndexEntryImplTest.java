package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link IndexEntryImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class IndexEntryImplTest {

    @Test
    void add() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1");
    }

    @Test
    void remove() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1");
        assertThat(entry.entries()).hasSize(1);

        entry.removeEntry("slug-1", "fake-name-1");
        assertThat(entry.indexedKeys()).isEmpty();
        assertThat(entry.entries()).isEmpty();
    }

    @Test
    void removeByIndex() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1", "slug-2");
        assertThat(entry.entries()).hasSize(2);

        entry.remove("fake-name-1");
        assertThat(entry.indexedKeys()).isEmpty();
        assertThat(entry.entries()).isEmpty();
    }

    @Test
    void getByIndexKey() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1", "slug-2");
        assertThat(entry.entries()).hasSize(2);

        assertThat(entry.getByIndexKey("slug-1")).isEqualTo(List.of("fake-name-1"));
    }

    @Test
    void keyOrder() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        spec.setOrder(IndexSpec.OrderType.DESC);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        entry.addEntry(List.of("slug-3"), "fake-name-2");
        entry.addEntry(List.of("slug-4"), "fake-name-3");
        entry.addEntry(List.of("slug-5"), "fake-name-1");
        assertThat(entry.entries())
            .containsSequence(
                Map.entry("slug-5", "fake-name-1"),
                Map.entry("slug-4", "fake-name-3"),
                Map.entry("slug-3", "fake-name-2"),
                Map.entry("slug-2", "fake-name-1"),
                Map.entry("slug-1", "fake-name-1"));

        assertThat(entry.indexedKeys()).containsSequence("slug-4", "slug-3", "slug-2", "slug-1");


        spec.setOrder(IndexSpec.OrderType.ASC);
        var descriptor2 = new IndexDescriptor(spec);
        var entry2 = new IndexEntryImpl(descriptor2);
        entry2.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        entry2.addEntry(List.of("slug-3"), "fake-name-2");
        entry2.addEntry(List.of("slug-4"), "fake-name-3");
        assertThat(entry2.entries())
            .containsSequence(Map.entry("slug-1", "fake-name-1"),
                Map.entry("slug-2", "fake-name-1"),
                Map.entry("slug-3", "fake-name-2"),
                Map.entry("slug-4", "fake-name-3"));
        assertThat(entry2.indexedKeys()).containsSequence("slug-1", "slug-2", "slug-3", "slug-4");
    }

    @Test
    void keyComparator() {
        var comparator = IndexEntryImpl.KeyComparator.INSTANCE;
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
        var comparator = IndexEntryImpl.KeyComparator.INSTANCE;
        String[] strings =
            {"moment-101", "moment-102", "moment-103", "moment-1011", "moment-1013", "moment-1021",
                "moment-1022", "moment-1012", "moment-1023"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"moment-101", "moment-102", "moment-103",
            "moment-1011", "moment-1012", "moment-1013", "moment-1021", "moment-1022",
            "moment-1023"});

        // 测试日期排序
        strings =
            new String[] {"2022-01-15", "2022-02-01", "2021-12-25", "2022-01-01", "2022-01-02"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(
            new String[] {"2021-12-25", "2022-01-01", "2022-01-02", "2022-01-15", "2022-02-01"});

        // 测试字母和数字混合排序
        strings = new String[] {"abc123", "abc45", "abc9", "abc100", "abc20"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(
            new String[] {"abc9", "abc20", "abc45", "abc100", "abc123"});

        // 测试纯字母排序
        strings = new String[] {"xyz", "abc", "def", "abcde", "xyzabc"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"abc", "abcde", "def", "xyz", "xyzabc"});

        // 测试空字符串
        strings = new String[] {"", "abc", "123", "xyz"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"", "123", "abc", "xyz"});

        // 测试相同字符串
        strings = new String[] {"abc", "abc", "abc", "abc"};
        Arrays.sort(strings, comparator);
        assertThat(strings).isEqualTo(new String[] {"abc", "abc", "abc", "abc"});

        // 测试 null 元素
        assertThatThrownBy(() -> Arrays.sort(new String[] {null, "abc", "123", "xyz"}, comparator))
            .isInstanceOf(NullPointerException.class);
    }
}
