package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.greaterThan;
import static run.halo.app.extension.index.query.QueryFactory.or;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link And} query.
 *
 * @author guqing
 * @since 2.12.0
 */
public class AndTest {

    @Test
    void testMatches() {
        Collection<Map.Entry<String, String>> deptEntry = List.of(Map.entry("A", "guqing"),
            Map.entry("A", "halo"),
            Map.entry("B", "lisi"),
            Map.entry("B", "zhangsan"),
            Map.entry("C", "ryanwang"),
            Map.entry("C", "johnniang")
        );
        Collection<Map.Entry<String, String>> ageEntry = List.of(Map.entry("19", "halo"),
            Map.entry("19", "guqing"),
            Map.entry("18", "zhangsan"),
            Map.entry("17", "lisi"),
            Map.entry("17", "ryanwang"),
            Map.entry("17", "johnniang")
        );
        var entries = Map.of("dept", deptEntry, "age", ageEntry);

        var indexView = new QueryIndexViewImpl(entries);
        var query = and(equal("dept", "B"), equal("age", "18"));
        var resultSet = query.matches(indexView);
        assertThat(resultSet).containsExactly("zhangsan");

        indexView = new QueryIndexViewImpl(entries);
        query = and(equal("dept", "C"), equal("age", "18"));
        resultSet = query.matches(indexView);
        assertThat(resultSet).isEmpty();

        indexView = new QueryIndexViewImpl(entries);
        query = and(
            // guqing, halo, lisi, zhangsan
            or(equal("dept", "A"), equal("dept", "B")),
            // guqing, halo, zhangsan
            or(equal("age", "19"), equal("age", "18"))
        );
        resultSet = query.matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder("guqing", "halo", "zhangsan");

        indexView = new QueryIndexViewImpl(entries);
        query = and(
            // guqing, halo, lisi, zhangsan
            or(equal("dept", "A"), equal("dept", "B")),
            // guqing, halo, zhangsan
            or(equal("age", "19"), equal("age", "18"))
        );
        resultSet = query.matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder("guqing", "halo", "zhangsan");

        indexView = new QueryIndexViewImpl(entries);
        query = and(
            // guqing, halo, lisi, zhangsan
            or(equal("dept", "A"), equal("dept", "C")),
            // guqing, halo, zhangsan
            and(equal("age", "17"), equal("age", "17"))
        );
        resultSet = query.matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder("ryanwang", "johnniang");
    }

    @Test
    void andMatch2() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var query = and(equal("lastName", "Fay"),
            and(
                equal("hireDate", "17"),
                and(greaterThan("salary", "1000"),
                    and(equal("managerId", "101"),
                        equal("departmentId", "50")
                    )
                )
            )
        );
        var resultSet = query.matches(indexView);
        assertThat(resultSet).containsExactly("100");
    }

    @Test
    void orAndMatch() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        // test the case when the data matched by the query does not intersect with the data
        // matched by the and query
        // or(query, and(otherQuery1, otherQuery2))
        var query = or(
            // matched with id 101
            and(equal("lastName", "Day"), equal("managerId", "102")),
            // matched with id 100, 103
            and(
                equal("hireDate", "17"),
                greaterThan("salary", "1800")
            )
        );
        var resultSet = query.matches(indexView);
        assertThat(resultSet).containsExactly("100", "101", "103");
    }
}
