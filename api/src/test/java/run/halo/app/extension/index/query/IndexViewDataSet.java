package run.halo.app.extension.index.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IndexViewDataSet {

    /**
     * Create a {@link QueryIndexView} for employee to test.
     *
     * @return a {@link QueryIndexView} for employee to test
     */
    public static QueryIndexView createEmployeeIndexView() {
        /*
         * id firstName lastName email hireDate salary managerId departmentId
         * 100 Pat Fay p 17 2600 101 50
         * 101 Lee Day l 17 2400 102 40
         * 102 William Jay w 19 2200 102 50
         * 103 Mary Day p 17 2000 103 50
         * 104 John Fay j 17 1800 103 50
         * 105 Gon Fay p 18 1900 101 40
         */
        Collection<Map.Entry<String, String>> idEntry = List.of(
            Map.entry("100", "100"),
            Map.entry("101", "101"),
            Map.entry("102", "102"),
            Map.entry("103", "103"),
            Map.entry("104", "104"),
            Map.entry("105", "105")
        );
        Collection<Map.Entry<String, String>> firstNameEntry = List.of(
            Map.entry("Pat", "100"),
            Map.entry("Lee", "101"),
            Map.entry("William", "102"),
            Map.entry("Mary", "103"),
            Map.entry("John", "104"),
            Map.entry("Gon", "105")
        );
        Collection<Map.Entry<String, String>> lastNameEntry = List.of(
            Map.entry("Fay", "100"),
            Map.entry("Day", "101"),
            Map.entry("Jay", "102"),
            Map.entry("Day", "103"),
            Map.entry("Fay", "104"),
            Map.entry("Fay", "105")
        );
        Collection<Map.Entry<String, String>> emailEntry = List.of(
            Map.entry("p", "100"),
            Map.entry("l", "101"),
            Map.entry("w", "102"),
            Map.entry("p", "103"),
            Map.entry("j", "104"),
            Map.entry("p", "105")
        );
        Collection<Map.Entry<String, String>> hireDateEntry = List.of(
            Map.entry("17", "100"),
            Map.entry("17", "101"),
            Map.entry("19", "102"),
            Map.entry("17", "103"),
            Map.entry("17", "104"),
            Map.entry("18", "105")
        );
        Collection<Map.Entry<String, String>> salaryEntry = List.of(
            Map.entry("2600", "100"),
            Map.entry("2400", "101"),
            Map.entry("2200", "102"),
            Map.entry("2000", "103"),
            Map.entry("1800", "104"),
            Map.entry("1900", "105")
        );
        Collection<Map.Entry<String, String>> managerIdEntry = List.of(
            Map.entry("101", "100"),
            Map.entry("102", "101"),
            Map.entry("102", "102"),
            Map.entry("103", "103"),
            Map.entry("103", "104"),
            Map.entry("101", "105")
        );
        Collection<Map.Entry<String, String>> departmentIdEntry = List.of(
            Map.entry("50", "100"),
            Map.entry("40", "101"),
            Map.entry("50", "102"),
            Map.entry("50", "103"),
            Map.entry("50", "104"),
            Map.entry("40", "105")
        );
        var entries = Map.of("id", idEntry,
            "firstName", firstNameEntry,
            "lastName", lastNameEntry,
            "email", emailEntry,
            "hireDate", hireDateEntry,
            "salary", salaryEntry,
            "managerId", managerIdEntry,
            "departmentId", departmentIdEntry);
        return new QueryIndexViewImpl(entries);
    }

    /**
     * Create a {@link QueryIndexView} for post to test.
     *
     * @return a {@link QueryIndexView} for post to test
     */
    public static QueryIndexView createPostIndexViewWithNullCell() {
        /*
         * id title published publishTime owner
         * 100 title1 true 2024-01-01T00:00:00 jack
         * 101 title2 true 2024-01-02T00:00:00 rose
         * 102 title3 false null smith
         * 103 title4 false null peter
         * 104 title5 false null john
         * 105 title6 true 2024-01-05 00:00:00 tom
         * 106 title7 true 2024-01-05 13:00:00 jerry
         * 107 title8 true 2024-01-05 12:00:00 jerry
         * 108 title9 false null jerry
         */
        Collection<Map.Entry<String, String>> idEntry = List.of(
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
        Collection<Map.Entry<String, String>> titleEntry = List.of(
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
        Collection<Map.Entry<String, String>> publishedEntry = List.of(
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
        Collection<Map.Entry<String, String>> publishTimeEntry = List.of(
            Map.entry("2024-01-01T00:00:00", "100"),
            Map.entry("2024-01-02T00:00:00", "101"),
            Map.entry("2024-01-05 00:00:00", "105"),
            Map.entry("2024-01-05 13:00:00", "106"),
            Map.entry("2024-01-05 12:00:00", "107")
        );

        Collection<Map.Entry<String, String>> ownerEntry = List.of(
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
        var entries = Map.of("id", idEntry,
            "title", titleEntry,
            "published", publishedEntry,
            "publishTime", publishTimeEntry,
            "owner", ownerEntry);
        return new QueryIndexViewImpl(entries);
    }
}
