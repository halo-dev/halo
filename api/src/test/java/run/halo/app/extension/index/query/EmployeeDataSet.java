package run.halo.app.extension.index.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EmployeeDataSet {

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
}
