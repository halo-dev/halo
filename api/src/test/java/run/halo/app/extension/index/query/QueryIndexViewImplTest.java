package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/**
 * Tests for {@link QueryIndexViewImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
class QueryIndexViewImplTest {

    @Test
    void getAllIdsForFieldTest() {
        var indexView = IndexViewDataSet.createPostIndexViewWithNullCell();
        var resultSet = indexView.getAllIdsForField("title");
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103", "104", "105", "106", "107", "108"
        );

        resultSet = indexView.getAllIdsForField("publishTime");
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "105", "106", "107"
        );
    }

    @Test
    void findIdsForFieldValueEqualTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueEqual("managerId", "id");
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueGreaterThanTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueGreaterThan("id", "managerId", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );

        indexView = IndexViewDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueGreaterThan("id", "managerId", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "102", "104", "105"
        );
    }

    @Test
    void findIdsForFieldValueGreaterThanTest2() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueGreaterThan("managerId", "id", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );

        indexView = IndexViewDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueGreaterThan("managerId", "id", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueLessThanTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueLessThan("id", "managerId", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );

        indexView = IndexViewDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueLessThan("id", "managerId", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueLessThanTest2() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueLessThan("managerId", "id", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );

        indexView = IndexViewDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueLessThan("managerId", "id", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "102", "104", "105"
        );
    }

    @Nested
    class SortTest {
        @Test
        void testSortByUnsorted() {
            Collection<Map.Entry<String, String>> entries = List.of(
                Map.entry("Item1", "Item1"),
                Map.entry("Item2", "Item2")
            );
            var indexView = new QueryIndexViewImpl(Map.of("field1", entries));
            var sort = Sort.unsorted();

            List<String> sortedList = indexView.sortBy(sort);
            assertThat(sortedList).isEqualTo(List.of("Item1", "Item2"));
        }

        @Test
        void testSortBySortedAscending() {
            var indexEntries = new HashMap<String, Collection<Map.Entry<String, String>>>();
            indexEntries.put("field1",
                List.of(Map.entry("key2", "Item2"), Map.entry("key1", "Item1")));
            var indexView = new QueryIndexViewImpl(indexEntries);
            var sort = Sort.by(Sort.Order.asc("field1"));

            List<String> sortedList = indexView.sortBy(sort);

            assertThat(sortedList).containsExactly("Item1", "Item2");
        }

        @Test
        void testSortBySortedDescending() {
            var indexEntries = new HashMap<String, Collection<Map.Entry<String, String>>>();
            indexEntries.put("field1",
                List.of(Map.entry("key1", "Item1"), Map.entry("key2", "Item2")));
            var indexView = new QueryIndexViewImpl(indexEntries);
            var sort = Sort.by(Sort.Order.desc("field1"));

            List<String> sortedList = indexView.sortBy(sort);

            assertThat(sortedList).containsExactly("Item2", "Item1");
        }

        @Test
        void testSortByMultipleFields() {
            var indexEntries = new LinkedHashMap<String, Collection<Map.Entry<String, String>>>();
            indexEntries.put("field1", List.of(Map.entry("k3", "Item3"), Map.entry("k2", "Item2")));
            indexEntries.put("field2", List.of(Map.entry("k1", "Item1"), Map.entry("k3", "Item3")));
            var indexView = new QueryIndexViewImpl(indexEntries);
            var sort = Sort.by(Sort.Order.asc("field1"), Sort.Order.desc("field2"));

            List<String> sortedList = indexView.sortBy(sort);

            assertThat(sortedList).containsExactly("Item2", "Item3", "Item1");
        }

        @Test
        void testSortByWithMissingFieldInMap() {
            var indexEntries = new LinkedHashMap<String, Collection<Map.Entry<String, String>>>();
            var indexView = new QueryIndexViewImpl(indexEntries);
            var sort = Sort.by(Sort.Order.asc("missingField"));

            assertThatThrownBy(() -> indexView.sortBy(sort))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Field name missingField is not indexed");
        }

        @Test
        void testSortByMultipleFields2() {
            var indexEntries = new LinkedHashMap<String, Collection<Map.Entry<String, String>>>();

            var entry1 = List.of(Map.entry("John", "John"),
                Map.entry("Bob", "Bob"),
                Map.entry("Alice", "Alice")
            );
            var entry2 = List.of(Map.entry("David", "David"),
                Map.entry("Eva", "Eva"),
                Map.entry("Frank", "Frank")
            );
            var entry3 = List.of(Map.entry("George", "George"),
                Map.entry("Helen", "Helen"),
                Map.entry("Ivy", "Ivy")
            );

            indexEntries.put("field1", entry1);
            indexEntries.put("field2", entry2);
            indexEntries.put("field3", entry3);

            /*
             * <pre>
             * Row Key | field1 | field2 | field3
             * -------|-------|-------|-------
             * John   | John  |       |
             * Bob    | Bob   |       |
             * Alice  | Alice |       |
             * David  |       | David |
             * Eva    |       | Eva   |
             * Frank  |       | Frank |
             * George |       |       | George
             * Helen  |       |       | Helen
             * Ivy    |       |       | Ivy
             * </pre>
             */
            var indexView = new QueryIndexViewImpl(indexEntries);
            // "John", "Bob", "Alice", "David", "Eva", "Frank", "George", "Helen", "Ivy"
            var sort = Sort.by(Sort.Order.desc("field1"), Sort.Order.asc("field2"),
                Sort.Order.asc("field3"));

            List<String> sortedList = indexView.sortBy(sort);

            assertThat(sortedList).containsSequence("John", "Bob", "Alice", "David", "Eva", "Frank",
                "George", "Helen", "Ivy");
        }
    }
}
