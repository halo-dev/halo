package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.index.query.IndexViewDataSet.createCommentIndexView;
import static run.halo.app.extension.index.query.IndexViewDataSet.createEmployeeIndexView;
import static run.halo.app.extension.index.query.IndexViewDataSet.createPostIndexViewWithNullCell;
import static run.halo.app.extension.index.query.IndexViewDataSet.pileForIndexer;
import static run.halo.app.extension.index.query.QueryIndexViewImpl.PRIMARY_INDEX_NAME;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.index.IndexEntry;
import run.halo.app.extension.index.Indexer;

/**
 * Tests for {@link QueryIndexViewImpl}.
 *
 * @author guqing
 * @since 2.17.0
 */
class QueryIndexViewImplTest {
    final String id = PRIMARY_INDEX_NAME;

    @Test
    void getAllIdsForFieldTest() {
        var indexView = createPostIndexViewWithNullCell();
        var resultSet = indexView.getIdsForField("title");
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103", "104", "105", "106", "107", "108"
        );

        resultSet = indexView.getIdsForField("publishTime");
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "105", "106", "107"
        );
    }

    @Test
    void findIdsForValueEqualTest() {
        var indexView = createEmployeeIndexView();
        var resultSet = indexView.findMatchingIdsWithEqualValues("managerId", id);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueGreaterThanTest() {
        var indexView = createEmployeeIndexView();
        var resultSet = indexView.findMatchingIdsWithGreaterValues(id, "managerId", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );

        indexView = createEmployeeIndexView();
        resultSet = indexView.findMatchingIdsWithGreaterValues(id, "managerId", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "102", "104", "105"
        );
    }

    @Test
    void findIdsForFieldValueGreaterThanTest2() {
        var indexView = createEmployeeIndexView();
        var resultSet = indexView.findMatchingIdsWithGreaterValues("managerId", id, false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );

        indexView = createEmployeeIndexView();
        resultSet = indexView.findMatchingIdsWithGreaterValues("managerId", id, true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueLessThanTest() {
        var indexView = createEmployeeIndexView();
        var resultSet = indexView.findMatchingIdsWithSmallerValues(id, "managerId", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );

        indexView = createEmployeeIndexView();
        resultSet = indexView.findMatchingIdsWithSmallerValues(id, "managerId", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueLessThanTest2() {
        var indexView = createEmployeeIndexView();
        var resultSet = indexView.findMatchingIdsWithSmallerValues("managerId", id, false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );

        indexView = createEmployeeIndexView();
        resultSet = indexView.findMatchingIdsWithSmallerValues("managerId", id, true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "102", "104", "105"
        );
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class SortTest {
        @Mock
        private Indexer indexer;

        @Test
        void testSortByUnsorted() {
            var idEntry = mock(IndexEntry.class);
            when(indexer.getIndexEntry(PRIMARY_INDEX_NAME))
                .thenReturn(idEntry);
            var indexView = new QueryIndexViewImpl(indexer);

            var sort = Sort.unsorted();

            var resultSet = new TreeSet<>(List.of("Item1", "Item2"));
            List<String> sortedList = indexView.sortBy(resultSet, sort);
            assertThat(sortedList).isEqualTo(List.of("Item1", "Item2"));
        }

        @Test
        void testSortBySortedAscending() {
            pileForIndexer(indexer, "field1",
                List.of(Map.entry("key2", "Item2"), Map.entry("key1", "Item1")));

            pileForIndexer(indexer, PRIMARY_INDEX_NAME,
                List.of(Map.entry("Item1", "Item1"), Map.entry("Item2", "Item2")));

            var indexView = new QueryIndexViewImpl(indexer);

            var sort = Sort.by(Sort.Order.asc("field1"));

            List<String> sortedList = indexView.sortBy(indexView.getAllIds(), sort);

            assertThat(sortedList).containsSequence("Item1", "Item2");
        }

        @Test
        void testSortBySortedDescending() {
            pileForIndexer(indexer, "field1",
                List.of(Map.entry("key1", "Item1"), Map.entry("key2", "Item2")));

            pileForIndexer(indexer, PRIMARY_INDEX_NAME,
                List.of(Map.entry("Item1", "Item1"), Map.entry("Item2", "Item2")));

            var indexView = new QueryIndexViewImpl(indexer);

            var sort = Sort.by(Sort.Order.desc("field1"));

            var resultSet = new TreeSet<>(List.of("Item1", "Item2"));
            List<String> sortedList = indexView.sortBy(resultSet, sort);

            assertThat(sortedList).containsExactly("Item2", "Item1");
        }

        @Test
        void testSortByMultipleFields() {
            pileForIndexer(indexer, "field1",
                List.of(Map.entry("k3", "Item3"), Map.entry("k2", "Item2")));

            pileForIndexer(indexer, "field2",
                List.of(Map.entry("k1", "Item1"), Map.entry("k3", "Item3")));

            pileForIndexer(indexer, id,
                List.of(Map.entry("Item1", "Item1"), Map.entry("Item2", "Item2"),
                    Map.entry("Item3", "Item3")));

            var indexView = new QueryIndexViewImpl(indexer);

            var sort = Sort.by(Sort.Order.asc("field1"), Sort.Order.desc("field2"));

            var resultSet = new TreeSet<>(List.of("Item1", "Item2", "Item3"));
            List<String> sortedList = indexView.sortBy(resultSet, sort);

            assertThat(sortedList).containsExactly("Item2", "Item3", "Item1");
        }

        @Test
        void testSortByMultipleFields2() {
            pileForIndexer(indexer, id, List.of());

            pileForIndexer(indexer, "field1", List.of(Map.entry("John", "John"),
                Map.entry("Bob", "Bob"),
                Map.entry("Alice", "Alice")
            ));
            pileForIndexer(indexer, "field2", List.of(Map.entry("David", "David"),
                Map.entry("Eva", "Eva"),
                Map.entry("Frank", "Frank")
            ));
            pileForIndexer(indexer, "field3", List.of(Map.entry("George", "George"),
                Map.entry("Helen", "Helen"),
                Map.entry("Ivy", "Ivy")
            ));

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
            var indexView = new QueryIndexViewImpl(indexer);
            var sort = Sort.by(Sort.Order.desc("field1"), Sort.Order.asc("field2"),
                Sort.Order.asc("field3"));

            var resultSet = new TreeSet<>(
                List.of("Bob", "John", "Eva", "Alice", "Ivy", "David", "Frank", "Helen", "George"));
            List<String> sortedList = indexView.sortBy(resultSet, sort);

            assertThat(sortedList).containsSequence("David", "Eva", "Frank", "George", "Helen",
                "Ivy", "John", "Bob", "Alice");
        }

        /**
         * <p>Result for the following data.</p>
         * <pre>
         *  | id | firstName | lastName | email | hireDate | salary | managerId | departmentId |
         * |----|-----------|----------|-------|----------|--------|-----------|--------------|
         * | 100| Pat       | Fay      | p     | 17       | 2600   | 101       | 50           |
         * | 101| Lee       | Day      | l     | 17       | 2400   | 102       | 40           |
         * | 103| Mary      | Day      | p     | 17       | 2000   | 103       | 50           |
         * | 104| John      | Fay      | j     | 17       | 1800   | 103       | 50           |
         * | 105| Gon       | Fay      | p     | 18       | 1900   | 101       | 40           |
         * | 102| William   | Jay      | w     | 19       | 2200   | 102       | 50           |
         * </pre>
         */
        @Test
        void sortByMultipleFieldsWithFirstSame() {
            var indexView = createEmployeeIndexView();
            var ids = indexView.getAllIds();
            var result = indexView.sortBy(ids, Sort.by(Sort.Order.asc("hireDate"),
                Sort.Order.asc("lastName"))
            );
            assertThat(result).containsSequence("101", "103", "100", "104", "105", "102");
        }

        /**
         * <p>Result for the following data.</p>
         * <pre>
         * | id  | title  | published | publishTime         | owner |
         * |-----|--------|-----------|---------------------|-------|
         * | 100 | title1 | true      | 2024-01-01T00:00:00 | jack  |
         * | 101 | title2 | true      | 2024-01-02T00:00:00 | rose  |
         * | 105 | title6 | true      | 2024-01-05 00:00:00 | tom   |
         * | 107 | title8 | true      | 2024-01-05 12:00:00 | jerry |
         * | 106 | title7 | true      | 2024-01-05 13:00:00 | jerry |
         * | 108 | title9 | false     | null                | jerry |
         * | 104 | title5 | false     | null                | john  |
         * | 103 | title4 | false     | null                | peter |
         * | 102 | title3 | false     | null                | smith |
         * </pre>
         */
        @Test
        void sortByMultipleFieldsForPostDataSet() {
            var indexView = createPostIndexViewWithNullCell();
            var ids = indexView.getAllIds();
            var result = indexView.sortBy(ids, Sort.by(Sort.Order.asc("publishTime"),
                Sort.Order.desc("title"))
            );
            assertThat(result).containsSequence("100", "101", "105", "107", "106", "108", "104",
                "103", "102");
        }

        @Test
        void sortByMultipleFieldsForCommentDataSet() {
            var indexView = createCommentIndexView();
            var ids = indexView.getAllIds();
            var sort = Sort.by(Sort.Order.desc("spec.top"),
                Sort.Order.asc("spec.priority"),
                Sort.Order.desc("spec.creationTime"),
                Sort.Order.asc("metadata.name")
            );
            var result = indexView.sortBy(ids, sort);
            assertThat(result).containsSequence("1", "2", "4", "3", "5", "6", "9", "10", "8", "7",
                "11", "12", "14", "13");
        }
    }
}
