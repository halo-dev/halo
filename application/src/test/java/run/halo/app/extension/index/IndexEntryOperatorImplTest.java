package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.index.query.IndexViewDataSet;

/**
 * Tests for {@link IndexEntryOperatorImpl}.
 *
 * @author guqing
 * @since 2.17.0
 */
@ExtendWith(MockitoExtension.class)
class IndexEntryOperatorImplTest {

    @Mock
    private IndexEntry indexEntry;

    @InjectMocks
    private IndexEntryOperatorImpl indexEntryOperator;

    private LinkedHashMap<String, List<String>> createIndexedMapAndPile() {
        var entries = new ArrayList<Map.Entry<String, String>>();
        entries.add(Map.entry("apple", "A"));
        entries.add(Map.entry("banana", "B"));
        entries.add(Map.entry("cherry", "C"));
        entries.add(Map.entry("date", "D"));
        entries.add(Map.entry("egg", "E"));
        entries.add(Map.entry("f", "F"));

        var indexedMap = IndexViewDataSet.toKeyObjectMap(entries);
        lenient().when(indexEntry.indexedKeys()).thenReturn(new TreeSet<>(indexedMap.keySet()));
        lenient().when(indexEntry.getObjectNamesBy(anyString())).thenAnswer(invocation -> {
            var key = (String) invocation.getArgument(0);
            return indexedMap.get(key);
        });
        lenient().when(indexEntry.entries()).thenReturn(entries);
        return indexedMap;
    }

    @Test
    void lessThan() {
        final var indexedMap = createIndexedMapAndPile();

        var result = indexEntryOperator.lessThan("banana", false);
        assertThat(result).containsExactly("A");

        result = indexEntryOperator.lessThan("banana", true);
        assertThat(result).containsExactly("A", "B");

        result = indexEntryOperator.lessThan("cherry", true);
        assertThat(result).containsExactly("A", "B", "C");

        // does not exist key
        result = indexEntryOperator.lessThan("z", false);
        var objectIds = indexedMap.values().stream()
            .flatMap(Collection::stream)
            .toArray(String[]::new);
        assertThat(result).contains(objectIds);

        result = indexEntryOperator.lessThan("a", false);
        assertThat(result).isEmpty();
    }

    @Test
    void greaterThan() {
        createIndexedMapAndPile();

        var result = indexEntryOperator.greaterThan("banana", false);
        assertThat(result).containsExactly("C", "D", "E", "F");

        result = indexEntryOperator.greaterThan("banana", true);
        assertThat(result).containsExactly("B", "C", "D", "E", "F");

        result = indexEntryOperator.greaterThan("cherry", true);
        assertThat(result).containsExactly("C", "D", "E", "F");

        result = indexEntryOperator.greaterThan("cherry", false);
        assertThat(result).containsExactly("D", "E", "F");

        // does not exist key
        result = indexEntryOperator.greaterThan("z", false);
        assertThat(result).isEmpty();
    }

    @Test
    void greaterThanForNumberString() {
        var entries = List.of(
            Map.entry("100", "1"),
            Map.entry("101", "2"),
            Map.entry("102", "3"),
            Map.entry("103", "4"),
            Map.entry("110", "5"),
            Map.entry("111", "6"),
            Map.entry("112", "7"),
            Map.entry("120", "8")
        );
        var indexedMap = IndexViewDataSet.toKeyObjectMap(entries);
        when(indexEntry.indexedKeys()).thenReturn(new TreeSet<>(indexedMap.keySet()));
        lenient().when(indexEntry.getObjectNamesBy(anyString())).thenAnswer(invocation -> {
            var key = (String) invocation.getArgument(0);
            return indexedMap.get(key);
        });
        when(indexEntry.entries()).thenReturn(entries);

        var result = indexEntryOperator.greaterThan("102", false);
        assertThat(result).containsExactly("4", "5", "6", "7", "8");

        result = indexEntryOperator.greaterThan("110", false);
        assertThat(result).containsExactly("6", "7", "8");
    }

    @Test
    void range() {
        createIndexedMapAndPile();

        var result = indexEntryOperator.range("banana", "date", true, false);
        assertThat(result).containsExactly("B", "C");

        result = indexEntryOperator.range("banana", "date", false, false);
        assertThat(result).containsExactly("C");

        result = indexEntryOperator.range("banana", "date", true, true);
        assertThat(result).containsExactly("B", "C", "D");

        result = indexEntryOperator.range("apple", "egg", false, true);
        assertThat(result).containsExactly("B", "C", "D", "E");

        // end not exist
        result = indexEntryOperator.range("d", "z", false, false);
        assertThat(result).containsExactly("D", "E", "F");

        // start key > end key
        assertThatThrownBy(() -> indexEntryOperator.range("z", "f", false, false))
            .isInstanceOf(IllegalArgumentException.class);

        // both not exist
        result = indexEntryOperator.range("z", "zz", false, false);
        assertThat(result).isEmpty();
    }

    @Test
    void findTest() {
        createIndexedMapAndPile();

        var result = indexEntryOperator.find("banana");
        assertThat(result).containsExactly("B");

        result = indexEntryOperator.find("date");
        assertThat(result).containsExactly("D");

        result = indexEntryOperator.find("z");
        assertThat(result).isEmpty();
    }

    @Test
    void findInTest() {
        createIndexedMapAndPile();
        var result = indexEntryOperator.findIn(List.of("banana", "date"));
        assertThat(result).containsExactly("B", "D");
    }
}