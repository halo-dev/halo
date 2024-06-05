package run.halo.app.extension.index.query;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import run.halo.app.extension.index.IndexEntry;
import run.halo.app.extension.index.Indexer;
import run.halo.app.extension.index.KeyComparator;

public class IndexViewDataSet {

    /**
     * Create a {@link QueryIndexView} for employee to test.
     * <pre>
     * | id | firstName | lastName | email | hireDate | salary | managerId | departmentId |
     * |----|-----------|----------|-------|----------|--------|-----------|--------------|
     * | 100| Pat       | Fay      | p     | 17       | 2600   | 101       | 50           |
     * | 101| Lee       | Day      | l     | 17       | 2400   | 102       | 40           |
     * | 102| William   | Jay      | w     | 19       | 2200   | 102       | 50           |
     * | 103| Mary      | Day      | p     | 17       | 2000   | 103       | 50           |
     * | 104| John      | Fay      | j     | 17       | 1800   | 103       | 50           |
     * | 105| Gon       | Fay      | p     | 18       | 1900   | 101       | 40           |
     * </pre>
     *
     * @return a {@link QueryIndexView} for employee to test
     */
    public static QueryIndexView createEmployeeIndexView() {
        final var idEntry = List.of(
            Map.entry("100", "100"),
            Map.entry("101", "101"),
            Map.entry("102", "102"),
            Map.entry("103", "103"),
            Map.entry("104", "104"),
            Map.entry("105", "105")
        );
        final var firstNameEntry = List.of(
            Map.entry("Pat", "100"),
            Map.entry("Lee", "101"),
            Map.entry("William", "102"),
            Map.entry("Mary", "103"),
            Map.entry("John", "104"),
            Map.entry("Gon", "105")
        );
        final var lastNameEntry = List.of(
            Map.entry("Fay", "100"),
            Map.entry("Day", "101"),
            Map.entry("Jay", "102"),
            Map.entry("Day", "103"),
            Map.entry("Fay", "104"),
            Map.entry("Fay", "105")
        );
        final var emailEntry = List.of(
            Map.entry("p", "100"),
            Map.entry("l", "101"),
            Map.entry("w", "102"),
            Map.entry("p", "103"),
            Map.entry("j", "104"),
            Map.entry("p", "105")
        );
        final var hireDateEntry = List.of(
            Map.entry("17", "100"),
            Map.entry("17", "101"),
            Map.entry("19", "102"),
            Map.entry("17", "103"),
            Map.entry("17", "104"),
            Map.entry("18", "105")
        );
        final var salaryEntry = List.of(
            Map.entry("2600", "100"),
            Map.entry("2400", "101"),
            Map.entry("2200", "102"),
            Map.entry("2000", "103"),
            Map.entry("1800", "104"),
            Map.entry("1900", "105")
        );
        final var managerIdEntry = List.of(
            Map.entry("101", "100"),
            Map.entry("102", "101"),
            Map.entry("102", "102"),
            Map.entry("103", "103"),
            Map.entry("103", "104"),
            Map.entry("101", "105")
        );
        final var departmentIdEntry = List.of(
            Map.entry("50", "100"),
            Map.entry("40", "101"),
            Map.entry("50", "102"),
            Map.entry("50", "103"),
            Map.entry("50", "104"),
            Map.entry("40", "105")
        );

        var indexer = mock(Indexer.class);

        pileForIndexer(indexer, QueryIndexViewImpl.PRIMARY_INDEX_NAME, idEntry);
        pileForIndexer(indexer, "firstName", firstNameEntry);
        pileForIndexer(indexer, "lastName", lastNameEntry);
        pileForIndexer(indexer, "email", emailEntry);
        pileForIndexer(indexer, "hireDate", hireDateEntry);
        pileForIndexer(indexer, "salary", salaryEntry);
        pileForIndexer(indexer, "managerId", managerIdEntry);
        pileForIndexer(indexer, "departmentId", departmentIdEntry);

        return new QueryIndexViewImpl(indexer);
    }

    /**
     * Create a {@link QueryIndexView} for post to test.
     * <pre>
     * | id  | title  | published | publishTime         | owner |
     * |-----|--------|-----------|---------------------|-------|
     * | 100 | title1 | true      | 2024-01-01T00:00:00 | jack  |
     * | 101 | title2 | true      | 2024-01-02T00:00:00 | rose  |
     * | 102 | title3 | false     | null                | smith |
     * | 103 | title4 | false     | null                | peter |
     * | 104 | title5 | false     | null                | john  |
     * | 105 | title6 | true      | 2024-01-05 00:00:00 | tom   |
     * | 106 | title7 | true      | 2024-01-05 13:00:00 | jerry |
     * | 107 | title8 | true      | 2024-01-05 12:00:00 | jerry |
     * | 108 | title9 | false     | null                | jerry |
     * </pre>
     *
     * @return a {@link QueryIndexView} for post to test
     */
    public static QueryIndexView createPostIndexViewWithNullCell() {
        final var idEntry = List.of(
            Map.entry("100", "100"),
            Map.entry("101", "101"),
            Map.entry("102", "102"),
            Map.entry("103", "103"),
            Map.entry("104", "104"),
            Map.entry("105", "105"),
            Map.entry("106", "106"),
            Map.entry("107", "107"),
            Map.entry("108", "108")
        );
        final var titleEntry = List.of(
            Map.entry("title1", "100"),
            Map.entry("title2", "101"),
            Map.entry("title3", "102"),
            Map.entry("title4", "103"),
            Map.entry("title5", "104"),
            Map.entry("title6", "105"),
            Map.entry("title7", "106"),
            Map.entry("title8", "107"),
            Map.entry("title9", "108")
        );
        final var publishedEntry = List.of(
            Map.entry("true", "100"),
            Map.entry("true", "101"),
            Map.entry("false", "102"),
            Map.entry("false", "103"),
            Map.entry("false", "104"),
            Map.entry("true", "105"),
            Map.entry("true", "106"),
            Map.entry("true", "107"),
            Map.entry("false", "108")
        );
        final var publishTimeEntry = List.of(
            Map.entry("2024-01-01T00:00:00", "100"),
            Map.entry("2024-01-02T00:00:00", "101"),
            Map.entry("2024-01-05 00:00:00", "105"),
            Map.entry("2024-01-05 13:00:00", "106"),
            Map.entry("2024-01-05 12:00:00", "107")
        );

        final var ownerEntry = List.of(
            Map.entry("jack", "100"),
            Map.entry("rose", "101"),
            Map.entry("smith", "102"),
            Map.entry("peter", "103"),
            Map.entry("john", "104"),
            Map.entry("tom", "105"),
            Map.entry("jerry", "106"),
            Map.entry("jerry", "107"),
            Map.entry("jerry", "108")
        );

        var indexer = mock(Indexer.class);
        pileForIndexer(indexer, QueryIndexViewImpl.PRIMARY_INDEX_NAME, idEntry);
        pileForIndexer(indexer, "title", titleEntry);
        pileForIndexer(indexer, "published", publishedEntry);
        pileForIndexer(indexer, "publishTime", publishTimeEntry);
        pileForIndexer(indexer, "owner", ownerEntry);

        return new QueryIndexViewImpl(indexer);
    }

    /**
     * Creates a fake comment index view for below data.
     * <pre>
     * | Name | Top   | Priority | Creation Time                    |
     * | ---- | ----- | -------- | -------------------------------- |
     * | 1    | True  | 0        | 2024-06-05 02:58:15.633165+00:00 |
     * | 2    | True  | 1        | 2024-06-05 02:58:16.633165+00:00 |
     * | 4    | True  | 2        | 2024-06-05 02:58:18.633165+00:00 |
     * | 3    | True  | 2        | 2024-06-05 02:58:17.633165+00:00 |
     * | 5    | True  | 3        | 2024-06-05 02:58:18.633165+00:00 |
     * | 6    | True  | 3        | 2024-06-05 02:58:18.633165+00:00 |
     * | 10   | False | 0        | 2024-06-05 02:58:17.633165+00:00 |
     * | 9    | False | 0        | 2024-06-05 02:58:17.633165+00:00 |
     * | 8    | False | 0        | 2024-06-05 02:58:16.633165+00:00 |
     * | 7    | False | 0        | 2024-06-05 02:58:15.633165+00:00 |
     * | 11   | False | 0        | 2024-06-05 02:58:14.633165+00:00 |
     * | 12   | False | 1        | 2024-06-05 02:58:14.633165+00:00 |
     * | 14   | False | 3        | 2024-06-05 02:58:17.633165+00:00 |
     * | 13   | False | 3        | 2024-06-05 02:58:14.633165+00:00 |
     * </pre>
     */
    public static QueryIndexView createCommentIndexView() {
        final var idEntry = List.of(
            Map.entry("1", "1"),
            Map.entry("2", "2"),
            Map.entry("3", "3"),
            Map.entry("4", "4"),
            Map.entry("5", "5"),
            Map.entry("6", "6"),
            Map.entry("7", "7"),
            Map.entry("8", "8"),
            Map.entry("9", "9"),
            Map.entry("10", "10"),
            Map.entry("11", "11"),
            Map.entry("12", "12"),
            Map.entry("13", "13"),
            Map.entry("14", "14")
        );
        final var creationTimeEntry = List.of(
            Map.entry("2024-06-05 02:58:15.633165", "1"),
            Map.entry("2024-06-05 02:58:16.633165", "2"),
            Map.entry("2024-06-05 02:58:17.633165", "3"),
            Map.entry("2024-06-05 02:58:18.633165", "4"),
            Map.entry("2024-06-05 02:58:18.633165", "5"),
            Map.entry("2024-06-05 02:58:18.633165", "6"),
            Map.entry("2024-06-05 02:58:15.633165", "7"),
            Map.entry("2024-06-05 02:58:16.633165", "8"),
            Map.entry("2024-06-05 02:58:17.633165", "9"),
            Map.entry("2024-06-05 02:58:17.633165", "10"),
            Map.entry("2024-06-05 02:58:14.633165", "11"),
            Map.entry("2024-06-05 02:58:14.633165", "12"),
            Map.entry("2024-06-05 02:58:14.633165", "13"),
            Map.entry("2024-06-05 02:58:17.633165", "14")
        );
        final var topEntry = List.of(
            Map.entry("true", "1"),
            Map.entry("true", "2"),
            Map.entry("true", "3"),
            Map.entry("true", "4"),
            Map.entry("true", "5"),
            Map.entry("true", "6"),
            Map.entry("false", "7"),
            Map.entry("false", "8"),
            Map.entry("false", "9"),
            Map.entry("false", "10"),
            Map.entry("false", "11"),
            Map.entry("false", "12"),
            Map.entry("false", "13"),
            Map.entry("false", "14")
        );
        final var priorityEntry = List.of(
            Map.entry("0", "1"),
            Map.entry("1", "2"),
            Map.entry("2", "3"),
            Map.entry("2", "4"),
            Map.entry("3", "5"),
            Map.entry("3", "6"),
            Map.entry("0", "7"),
            Map.entry("0", "8"),
            Map.entry("0", "9"),
            Map.entry("0", "10"),
            Map.entry("0", "11"),
            Map.entry("1", "12"),
            Map.entry("3", "13"),
            Map.entry("3", "14")
        );

        var indexer = mock(Indexer.class);
        pileForIndexer(indexer, QueryIndexViewImpl.PRIMARY_INDEX_NAME, idEntry);
        pileForIndexer(indexer, "spec.creationTime", creationTimeEntry);
        pileForIndexer(indexer, "spec.top", topEntry);
        pileForIndexer(indexer, "spec.priority", priorityEntry);

        return new QueryIndexViewImpl(indexer);
    }

    public static void pileForIndexer(Indexer indexer, String propertyName,
        Collection<Map.Entry<String, String>> entries) {
        var indexEntry = mock(IndexEntry.class);
        lenient().when(indexer.getIndexEntry(propertyName)).thenReturn(indexEntry);
        var sortedEntries = sortEntries(entries);

        lenient().when(indexEntry.entries()).thenReturn(sortedEntries);
        lenient().when(indexEntry.getIdPositionMap()).thenReturn(idPositionMap(sortedEntries));

        var indexedMap = toKeyObjectMap(sortedEntries);
        lenient().when(indexEntry.indexedKeys()).thenReturn(new TreeSet<>(indexedMap.keySet()));
        lenient().when(indexEntry.getObjectNamesBy(anyString())).thenAnswer(invocation -> {
            var key = (String) invocation.getArgument(0);
            return indexedMap.get(key);
        });
        lenient().when(indexEntry.entries()).thenReturn(entries);
    }

    public static List<Map.Entry<String, String>> sortEntries(
        Collection<Map.Entry<String, String>> entries) {
        return entries.stream()
            .sorted((a, b) -> KeyComparator.INSTANCE.compare(a.getKey(), b.getKey()))
            .toList();
    }

    public static Map<String, Integer> idPositionMap(
        Collection<Map.Entry<String, String>> sortedEntries) {
        var asMap = toKeyObjectMap(sortedEntries);
        int i = 0;
        var idPositionMap = new HashMap<String, Integer>();
        for (var valueIdsEntry : asMap.entrySet()) {
            var ids = valueIdsEntry.getValue();
            for (String id : ids) {
                idPositionMap.put(id, i);
            }
            i++;
        }
        return idPositionMap;
    }

    public static LinkedHashMap<String, List<String>> toKeyObjectMap(
        Collection<Map.Entry<String, String>> sortedEntries) {
        return sortedEntries.stream()
            .collect(Collectors.groupingBy(Map.Entry::getKey,
                LinkedHashMap::new,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
